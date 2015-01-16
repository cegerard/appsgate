package appsgate.lig.clock.sensor.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.clock.sensor.messages.ClockAlarmNotificationMsg;
import appsgate.lig.clock.sensor.messages.ClockSetNotificationMsg;
import appsgate.lig.clock.sensor.messages.FlowRateSetNotification;
import appsgate.lig.clock.sensor.spec.AlarmEventObserver;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This java interface is an ApAM specification shared by all ApAM AppsGate
 * application to provide current Time and Date information
 */
public class ConfigurableClockImpl extends CoreObjectBehavior implements
		CoreClockSpec, CoreObjectSpec {

	/**
	 * Lag between the real current Date and the one set
	 */
	long currentLag;

	/**
	 * In case we miss an alarm because of delay took for processing, we allow
	 * to fire alarms that that should have occurred until 50 ms before current
	 * time
	 */
	long alarmLagTolerance = 50;

	/**
	 * current flow rate, "1" is the default value
	 */
	double flowRate;
	long timeFlowBreakPoint;

	private static int currentAlarmId;

	/**
	 * Act as primary key for single alarm event (removed after being fired)
	 */
	long nextAlarmTime;

	/**
	 * Act as primary keys for periodic alarm events (might be not optimal)
	 */
	Integer nextAlarmId;

	/**
	 * A sorted map between the times in millis at which are registered alarms
	 * and the corresponding alarm Id and observers
	 */
	SortedMap<Long, Map<Integer, AlarmEventObserver>> alarms = new TreeMap<Long, Map<Integer,AlarmEventObserver>>();

	/**
	 * A map between the periodic alarms, matches the same alarm id as an alarm
	 * registered at current time
	 */
	Map<Integer, AlarmEventObserver> periodicAlarmObservers = new ConcurrentHashMap<Integer, AlarmEventObserver>();

	/**
	 * A map between the periods in millis of periodic alarms, matches the same
	 * alarm id as an alarm registered at current time
	 */
	Map<Integer, Long> alarmPeriods = new ConcurrentHashMap<Integer, Long>();

	/**
	 * As single time alarms are removed from the list they do not fire events
	 * two times But periodic events aren't removed, so we need to disarm them
	 */
	Set<Integer> disarmedAlarms = new HashSet<Integer>();

	/**
	 * Convenient to unregister alarms (avoid complexity)
	 */
	Map<Integer, Long> reverseAlarmMap = new ConcurrentHashMap<Integer, Long>();

	Timer timer;

	Object lock;
	boolean runningArmTimer;

	SimpleDateFormat dateFormat;

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(ConfigurableClockImpl.class);

	public ConfigurableClockImpl() {
		lock = new Object();
		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		initAppsgateFields();
		fullResetClock();
	}

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void start() {
		logger.debug("New Configurable clock created");
	}

	public void stop() {
		logger.debug("Configurable Clock removed");
		if (timer != null)
			timer.cancel();
		timer = null;

	}

	public ClockSetNotificationMsg fireClockSetNotificationMsg(
			Calendar oldTime, Calendar newTime) {
		return new ClockSetNotificationMsg(this, oldTime.getTime().toString(),
				newTime.getTime().toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.clock.sensor.spec.CoreClockSpec#getCurrentDate()
	 */
	@Override
	public Calendar getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getCurrentTimeInMillis());
		return cal;
	}

	/*
	 * if
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.clock.sensor.spec.CoreClockSpec#getCurrentTimeInMillis()
	 */
	@Override
	public long getCurrentTimeInMillis() {
		long systemTime = System.currentTimeMillis();
		long simulatedTime = currentLag;
		if (timeFlowBreakPoint > 0 && timeFlowBreakPoint <= systemTime) {
			long elapsedTime = (long) ((systemTime - timeFlowBreakPoint) * flowRate);
			logger.trace("getCurrentTimeInMillis(), system time : "
					+ systemTime + ", time flow breakpoint : "
					+ timeFlowBreakPoint + ", elasped time : " + elapsedTime);

			simulatedTime += timeFlowBreakPoint + elapsedTime;
		} else
			simulatedTime += systemTime;
		logger.debug("getCurrentTimeInMillis(), (simulated) time : "
				+ simulatedTime + " (representing "
				+ dateFormat.format(new Date(simulatedTime)) + ")");
		return simulatedTime;
	}

	@Override
	public double getTimeFlowRate() {
		return flowRate;
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.clock.sensor.spec.CoreClockSpec#setCurrentDate(java.util
	 * .Calendar)
	 */
	@Override
	public void setCurrentDate(Calendar calendar) {
		logger.trace("setCurrentDate(Calendar calendar : "
				+ dateFormat.format(calendar.getTime()) + ")");

		setCurrentTimeInMillis(calendar.getTimeInMillis());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.clock.sensor.spec.CoreClockSpec#setCurrentTimeInMillis(long)
	 */
	@Override
	public void setCurrentTimeInMillis(long millis) {
		logger.trace("setCurrentTimeInMillis(long millis : " + millis
				+ " (representing " + dateFormat.format(new Date(millis))
				+ "))");

		Calendar oldCalendar = getCurrentDate();

		currentLag = millis - Calendar.getInstance().getTimeInMillis();
		setTimeFlowRate(flowRate);
		calculateNextTimer();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		fireClockSetNotificationMsg(oldCalendar, calendar);
	}

	private String appsgatePictureId;
	private String appsgateObjectId;
	private String appsgateUserType;
	private String appsgateStatus;
	private String appsgateServiceName;

	protected void initAppsgateFields() {
		appsgatePictureId = null;
		appsgateServiceName = "SystemClock";
		appsgateUserType = "21";
		appsgateStatus = "2";
		appsgateObjectId = appsgateUserType
				+ String.valueOf(appsgateServiceName.hashCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getAbstractObjectId()
	 */
	@Override
	public String getAbstractObjectId() {
		return appsgateObjectId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getDescription()
	 */
	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		// mandatory appsgate properties
		descr.put("id", appsgateObjectId);
		descr.put("type", appsgateUserType); // 21 for clock
		descr.put("status", appsgateStatus);
		descr.put("sysName", appsgateServiceName);
		descr.put("remote", true);

		if (currentLag == 0 && flowRate == 1 && timeFlowBreakPoint == -1) {
			descr.put("simulated", false);
		} else {
			descr.put("simulated", true);
		}

		Calendar cal = Calendar.getInstance();
		long time = cal.getTimeInMillis() + currentLag;
		cal.setTimeInMillis(time);
		descr.put("ClockSet", cal.getTime().toString());
		descr.put("clockValue", String.valueOf(time));
		descr.put("flowRate", String.valueOf(flowRate));

		return descr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getObjectStatus()
	 */
	@Override
	public int getObjectStatus() {
		return Integer.parseInt(appsgateStatus);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getPictureId()
	 */
	@Override
	public String getPictureId() {
		return appsgatePictureId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getUserType()
	 */
	@Override
	public String getUserType() {
		return appsgateUserType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.core.object.spec.CoreObjectSpec#setPictureId(java.lang.String
	 * )
	 */
	@Override
	public void setPictureId(String pictureId) {
		this.appsgatePictureId = pictureId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.clock.sensor.spec.CoreClockSpec#goAlongUntil(long)
	 */
	@Override
	public void goAlongUntil(long millis) {
		logger.trace("goAlongUntil(long millis : " + millis + ")");
		synchronized (lock) {
			Long currentTime = getCurrentTimeInMillis();
			long futureTime = currentTime + millis;
			SortedMap<Long, Map<Integer, AlarmEventObserver>> alarmsToFire = alarms
					.subMap(currentTime - alarmLagTolerance, futureTime
							+ alarmLagTolerance);
			// Firing single alarms
			if (alarmsToFire != null)
				for (Map<Integer, AlarmEventObserver> obs : alarmsToFire
						.values())
					fireClockAlarms(obs);

			// firing periodic alarms
			if (alarmPeriods != null && !alarmPeriods.isEmpty())
				for (Integer i : new ArrayList<Integer>(alarmPeriods.keySet()) ) {
					long time = currentTime;
					if (disarmedAlarms.contains(i))
						time += alarmLagTolerance + 1;

					Long nextPeriodic = (time - reverseAlarmMap.get(i))
							% alarmPeriods.get(i);
					if (nextPeriodic >= 0)
						time += alarmPeriods.get(i) - nextPeriodic;

					if (nextPeriodic >= 0)
						while (time < futureTime) {
							time += alarmPeriods.get(i);
							periodicAlarmObservers.get(i).alarmEventFired(i);
						}
					
					if(i < 0)
						System.out.println("ERREUR");
					
					if (futureTime - time < alarmLagTolerance)
						disarmedAlarms.add(i);
					else
						disarmedAlarms.remove(i);
				}

			setCurrentTimeInMillis(currentTime + millis);
		}
		calculateNextTimer();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.clock.sensor.spec.CoreClockSpec#registerAlarm(java.util.
	 * Calendar, appsgate.lig.clock.sensor.spec.AlarmEventObserver)
	 */
	@Override
	public int registerAlarm(Calendar calendar, AlarmEventObserver observer) {
		logger.trace("registerAlarm(Calendar calendar : "
				+ dateFormat.format(calendar.getTime())
				+ ", AlarmEventObserver observer : " + observer);
		if (calendar != null && observer != null) {
			synchronized (lock) {
				Long time = calendar.getTimeInMillis();
				if (alarms.containsKey(time)) {
					logger.trace("registerAlarm(...), alarm events already registered for this time, adding this one");
					Map<Integer, AlarmEventObserver> observers = alarms
							.get(time);
					observers.put(++currentAlarmId, observer);
					reverseAlarmMap.put(currentAlarmId, time);
				} else {
					logger.trace("registerAlarm(...), alarm events not registered for this time, creating a new one");
					/**
					 * An map between the alarm event ID and the associated
					 * observers
					 */
					Map<Integer, AlarmEventObserver> observers = new HashMap<Integer, AlarmEventObserver>();
					observers.put(++currentAlarmId, observer);
					alarms.put(time, observers);
					reverseAlarmMap.put(currentAlarmId, time);

				}
				logger.debug("registerAlarm(...), alarm events id created : "
						+ currentAlarmId);
				calculateNextTimer();
			}
			return currentAlarmId;
		} else {
			logger.warn("registerAlarm(...), calendar or oberver is null, does not register the alarm");
			return -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.clock.sensor.spec.CoreClockSpec#unregisterAlarm(int)
	 */
	@Override
	public void unregisterAlarm(int alarmEventId) {
		logger.trace("unregisterAlarm(int alarmEventId : " + alarmEventId + ")");
		synchronized (lock) {
			if (periodicAlarmObservers.containsKey(alarmEventId)) {
				periodicAlarmObservers.remove(alarmEventId);
				alarmPeriods.remove(alarmEventId);
				disarmedAlarms.remove(alarmEventId);
			} else {
				Long time = reverseAlarmMap.remove(alarmEventId);
				Map<Integer, AlarmEventObserver> observers = alarms.get(time);
				observers.remove(alarmEventId);
				if (observers.size() < 1)
					alarms.remove(time);
			}

			if (reverseAlarmMap.containsKey(alarmEventId)) {
				reverseAlarmMap.remove(alarmEventId);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.clock.sensor.spec.CoreClockSpec#setTimeFlowRate(double)
	 */
	@Override
	public double setTimeFlowRate(double rate) {
		logger.trace("setTimeFlowRate(double rate : " + rate + ")");

		double oldRate = flowRate;
		if (rate > 0 && rate != 1) {
			// avoid value that could lead to strange behavior
			timeFlowBreakPoint = System.currentTimeMillis();
			if (flowRate < 0.01)
				flowRate = 0.01;
			if (flowRate > 100)
				flowRate = 100;
			else
				flowRate = rate;
		} else {
			flowRate = 1;
			timeFlowBreakPoint = -1;
		}
		logger.debug("setTimeFlowRate(double rate), new time flow rate : "
				+ flowRate);
		if (oldRate != flowRate) {
			fireFlowRateSetNotificationMsg(oldRate, flowRate);
		}
		calculateNextTimer();
		return flowRate;

	}

	void rearmPeriodicAlarms(Long time) {
		logger.trace("rearmPeriodicAlarms(Long time : "
				+ (time == null ? null : dateFormat.format(new Date(time)))
				+ ")");

		if (time == null)
			time = getCurrentTimeInMillis();

		if (disarmedAlarms != null && !disarmedAlarms.isEmpty()
				&& reverseAlarmMap != null && !reverseAlarmMap.isEmpty()
				&& alarmPeriods != null && !alarmPeriods.isEmpty())
			for (Integer i : new ArrayList<Integer>(disarmedAlarms) ) {

				logger.trace("rearmPeriodicAlarms(...), checking alarmId :  "
						+ i);
				long time_from_epoch = time % alarmPeriods.get(i).longValue();

				if (Math.abs(reverseAlarmMap.get(i).longValue()
						- time_from_epoch) > alarmLagTolerance) {
					logger.trace("rearmPeriodicAlarms(...), re-arming alarm id : "
							+ i);
					disarmedAlarms.remove(i);
				}
			}
	}

	long nearestSingleAlarmDelay(long time) {
		logger.trace("nearestSingleAlarmDelay(long time : "
				+ dateFormat.format(new Date(time)) + ")");
		nextAlarmTime = -1;
		if (time < 0)
			time = getCurrentTimeInMillis();
		if (alarms != null && !alarms.isEmpty()
				&& alarms.lastKey() >= (time - alarmLagTolerance)) {
			nextAlarmTime = alarms.tailMap(time - alarmLagTolerance).firstKey()
					.longValue();
			return Math.abs(nextAlarmTime - time);
		} else
			return nextAlarmTime;
	}

	long nearestPeriodicAlarmDelay(long time) {
		logger.trace("nearestPeriodicAlarmDelay(long time : "
				+ dateFormat.format(new Date(time)) + ")");
		if(alarmPeriods==null || alarmPeriods.isEmpty()) {
			return -1;
		}
		
		if (time < 0)
			time = getCurrentTimeInMillis();
		boolean initMin = false;
		long minPeriodValue = -1;

		for (Integer i : alarmPeriods.keySet()) {
			long time_from_epoch = time % alarmPeriods.get(i);
			if (!disarmedAlarms.contains(i)) {

				long nextPeriodic;
				if (reverseAlarmMap.get(i) > time_from_epoch) {
					nextPeriodic = reverseAlarmMap.get(i) - time_from_epoch;
				} else {
					nextPeriodic = (reverseAlarmMap.get(i) + alarmPeriods
							.get(i)) - time_from_epoch;
				}

				logger.trace("nearestPeriodicAlarmDelay(...), next periodic : "
						+ nextPeriodic);
				if (!initMin || nextPeriodic < minPeriodValue) {
					initMin = true;
					minPeriodValue = nextPeriodic;
					nextAlarmId = i;
				}
			}
		}
		return minPeriodValue;
	}

	@Override
	public long calculateNextTimer() {
		synchronized (lock) {
			Long currentTime = getCurrentTimeInMillis();
			rearmPeriodicAlarms(currentTime);

			long nextAlarmDelay = -1;
			nextAlarmId = -1;
			nextAlarmTime = -1;

			long nearestSingle = nearestSingleAlarmDelay(currentTime);
			long nearestPeriodic = nearestPeriodicAlarmDelay(currentTime);

			logger.trace("calculateNextTimer(), nextAlarmId : " + nextAlarmId
					+ ", nextAlarmTime : " + nextAlarmTime
					+ ", nearestSingle :" + nearestSingle + ", nearestPeriodic"
					+ nearestPeriodic);

			if (nearestSingle >= 0
					&& (nearestSingle < nearestPeriodic || nearestPeriodic < 0)) {
				nextAlarmDelay = nearestSingle;
				nextAlarmId = -1;
			} else if (nearestPeriodic >= 0) {
				nextAlarmDelay = nearestPeriodic;
				nextAlarmTime = -1;
			}

			if (nextAlarmDelay >= 0) {
				nextAlarmDelay = (long) (nextAlarmDelay / flowRate);

				logger.trace("calculateNextTimer(), next alarm should ring in : "
						+ nextAlarmDelay + "ms");
				AlarmFiringTask nextAlarm = new AlarmFiringTask(this);
				if (timer != null)
					timer.cancel();
				timer = new Timer();
				timer.schedule(nextAlarm, nextAlarmDelay);
				return nextAlarmDelay;
			} else if (!runningArmTimer && !disarmedAlarms.isEmpty()) {
				RearmingPeriodicAlarmTask arming = new RearmingPeriodicAlarmTask(this);
				if (timer == null)
					timer = new Timer();
				timer.schedule(arming, alarmLagTolerance + 1);
				runningArmTimer = true;
			}
			return -1;
		}
	}

	public void fireClockAlarms(Map<Integer, AlarmEventObserver> observers) {
		logger.trace("fireClockAlarms(Map<Integer, AlarmEventObserver> observers)");

		if (observers != null && !observers.isEmpty()) {
			Set<Integer> removableObservers = new HashSet<Integer>(); // Observers

			for (Integer i : observers.keySet()) {
				logger.trace("fireClockAlarms(...), AlarmEventId : " + i);
				AlarmEventObserver obs = observers.get(i);
				removableObservers.add(i);

				if (obs != null) {
					logger.debug("fireClockAlarms(...), firing to " + obs);

					obs.alarmEventFired(i.intValue());
					fireClockAlarmNotificationMsg(i.intValue());
				}

			}
			for (Integer i : new ArrayList<Integer>(removableObservers)) {
				logger.trace("fireClockAlarms(...), removing alarmEventId : "
						+ i);
				observers.remove(i);
			}
		}
	}

	public NotificationMsg fireClockAlarmNotificationMsg(int alarmEventId) {
		return new ClockAlarmNotificationMsg(this, alarmEventId);
	}



	public FlowRateSetNotification fireFlowRateSetNotificationMsg(
			double oldFlowRate, double newFlowRate) {
		return new FlowRateSetNotification(this, String.valueOf(oldFlowRate),
				String.valueOf(newFlowRate));
	}
	
	public void fireAlarms() {
		logger.trace("fireAlarms(), AlarmFiringTask starting");

		if (nextAlarmTime > 0) {
			logger.debug("Firing current clock alarms to all single clock observers");
			long nextAlarmTimeClone = nextAlarmTime;
			Map<Integer, AlarmEventObserver> observers = alarms
					.get(nextAlarmTimeClone);
			synchronized (lock) {
				timer.cancel();
				timer = null;
				fireClockAlarms(observers);
				if (observers.isEmpty()) {
					alarms.remove(nextAlarmTimeClone);
				}
				nextAlarmTime = -1;
			}
		}
		int id = nextAlarmId; // Trick to avoid the change nextAlarmId by a calculateNextTimer
		if (id > 0) {
			logger.debug("Firing current clock alarms to one periodic event observer");
			
			if(periodicAlarmObservers.containsKey(id)) {
				periodicAlarmObservers.get(id).alarmEventFired(
					id);
				fireClockAlarmNotificationMsg(id);
			}
			if(id <0)
				System.out.println("ERREUR, c'est là");
			if(disarmedAlarms.contains(id)) {
				disarmedAlarms.add(id);
			}
		}
		calculateNextTimer();
	}
	
	public void rearm() {
		logger.trace("rearm(), trying to rearm periodic clock alarms");
		runningArmTimer = false;
		rearmPeriodicAlarms(null);
		calculateNextTimer();
	}
	
	


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * appsgate.lig.clock.sensor.spec.CoreClockSpec#registerPeriodicAlarm(long,
	 * appsgate.lig.clock.sensor.spec.AlarmEventObserver)
	 */
	@Override
	public int registerPeriodicAlarm(Calendar calendar, long millis,
			AlarmEventObserver observer) {
		logger.trace("registerPeriodicAlarm(Calendar calendar : "
				+ dateFormat.format(calendar == null ? Calendar.getInstance()
						.getTime() : calendar.getTime()) + ", long millis : "
				+ millis + ", AlarmEventObserver observer : " + observer);

		if (millis > 0 && observer != null) {
			synchronized (lock) {
				Long time;
				if (calendar != null)
					time = calendar.getTimeInMillis();
				else
					time = getCurrentTimeInMillis();

				logger.debug("registerPeriodicAlarm(...), creating a new one");
				periodicAlarmObservers.put(++currentAlarmId, observer);
				reverseAlarmMap.put(currentAlarmId, time.longValue() % millis);
				alarmPeriods.put(currentAlarmId, millis);

				logger.debug("registerPeriodicAlarm(...), alarm events id created : "
						+ currentAlarmId);
				calculateNextTimer();
				return currentAlarmId;
			}
		} else {
			logger.warn("registerPeriodicAlarm(...), millis incorrect or observer is null, does not register the alarm");
			return -1;
		}
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SERVICE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.clock.sensor.spec.CoreClockSpec#fullResetClock()
	 */
	@Override
	public void fullResetClock() {
		logger.trace("fullResetClock()");
		
		resetSystemTime();
		resetSingleAlarms();
		resetPeriodicAlarms();
		currentAlarmId = 0;
	}

	@Override
	public void resetSystemTime() {
		logger.trace("resetSystemTime()");

		Calendar oldCalendar;
		synchronized (lock) {
			oldCalendar = getCurrentDate();

			currentLag = 0;
			timeFlowBreakPoint = -1;
			setTimeFlowRate(1);
		}
		fireClockSetNotificationMsg(oldCalendar, Calendar.getInstance());
	}

	@Override
	public void resetSingleAlarms() {
		logger.trace("resetSingleAlarms()");

		synchronized (lock) {
			for (Integer i : new ArrayList<Integer>(reverseAlarmMap.keySet())) {
				unregisterAlarm(i);
				
			}
			calculateNextTimer();
		}
	}

	@Override
	public void resetPeriodicAlarms() {
		logger.trace("resetPeriodicAlarms()");

		synchronized (lock) {
			for (Integer i : new ArrayList<Integer>(alarmPeriods.keySet())) {
				unregisterAlarm(i);
			}
			calculateNextTimer();
		}
	}
	
	/**
	 * @return the time in millisecond from midnight today
	 */
	public long getCurrentTimeOfDay() {
		logger.trace("getCurrentTimeOfDay()");
		long currentTime = getCurrentTimeInMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentTime);
		cal.set(Calendar.YEAR,1970);
		cal.set(Calendar.DAY_OF_YEAR,1);
		cal.set(Calendar.ZONE_OFFSET,0);
		
		logger.trace("getCurrentTimeOfDay(), returning "+cal.getTimeInMillis());
		return cal.getTimeInMillis();
	}

	@Override
	public boolean checkCurrentTimeOfDay(long isAfter, long isBefore) {
		logger.trace("checkCurrentTimeOfDay(long isAfter : {}, long isBefore : {})",isAfter,isBefore);
		if(isAfter >= 0 && isAfter > getCurrentTimeOfDay()) {
			return false;
		}
		if(isBefore >= 0 && isBefore < getCurrentTimeOfDay()) {
			return false;
		}				
		return true;
	}

}
