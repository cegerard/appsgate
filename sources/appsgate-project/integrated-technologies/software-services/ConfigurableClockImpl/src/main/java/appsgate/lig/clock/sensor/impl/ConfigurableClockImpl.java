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
	
	long alarmTolerance = 500;

	/**
	 * current flow rate, "1" is the default value
	 */
	double flowRate;
	long timeFlowBreakPoint;

	Timer timer;

	Object lock;
	
	AlarmRegistry alarmRegistry;

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(ConfigurableClockImpl.class);

	public ConfigurableClockImpl() {
		lock = new Object();
		alarmRegistry = new AlarmRegistry(this);
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
		return new ClockSetNotificationMsg(this.getAbstractObjectId(), oldTime.getTime().toString(),
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
				+ AlarmDescriptor.dateFormat.format(new Date(simulatedTime)) + ")");
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
				+ AlarmDescriptor.dateFormat.format(calendar.getTime()) + ")");

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
				+ " (representing " + AlarmDescriptor.dateFormat.format(new Date(millis))
				+ "))");

		Calendar oldCalendar = getCurrentDate();

		currentLag = millis - Calendar.getInstance().getTimeInMillis();
		setTimeFlowRate(flowRate);
		calculateNextTimer();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		fireClockSetNotificationMsg(oldCalendar, calendar);
	}

	private String appsgateObjectId;
	private String appsgateUserType;
	private String appsgateStatus;
	private String appsgateServiceName;

	protected void initAppsgateFields() {
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
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getUserType()
	 */
	@Override
	public String getUserType() {
		return appsgateUserType;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see appsgate.lig.clock.sensor.spec.CoreClockSpec#goAlongUntil(long)
	 */
	@Override
	public void goAlongUntil(long millis) {
		logger.trace("goAlongUntil(long millis : " + millis + ")");
		long currentTime = getCurrentTimeInMillis();
		long futureTime = currentTime+millis;
		
		Set<Integer> alarmsToFire = alarmRegistry.getFutureAlarms(currentTime, millis);
		for(Integer id : alarmsToFire) {
			AlarmDescriptor alarm = alarmRegistry.getAlarmFromId(id);
			logger.trace("goAlongUntil(), firing alarmId: {} ", id);
			
			long period = alarm.getPeriod();

			if(period > 0) {
				logger.trace("goAlongUntil(), alarmId: {} is periodic looping to fire it several times", id);
				long time = currentTime;
				while (time+alarm.getNextEventLapse(time) < futureTime) {
					time += period;
					alarm.getObserver().alarmEventFired(id);
					fireClockAlarmNotificationMsg(id);
				}
			} else {
				logger.trace("goAlongUntil(), alarmId: {} is not periodic, firing once and removing it", id);
				alarm.getObserver().alarmEventFired(id);
				fireClockAlarmNotificationMsg(id);
				alarmRegistry.removeAlarm(id);				
			}
		}
		setCurrentTimeInMillis(currentTime + millis);
		
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
				+ AlarmDescriptor.dateFormat.format(calendar.getTime())
				+ ", AlarmEventObserver observer : " + observer);
		if (calendar != null && observer != null) {
			Long time = calendar.getTimeInMillis();
			int id = alarmRegistry.addNewAlarm(time, 0, observer);

			logger.debug("registerAlarm(...), alarm events id created : "
					+ id);
			calculateNextTimer();
			return id;
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
		alarmRegistry.removeAlarm(alarmEventId);
		calculateNextTimer();
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



	@Override
	public long calculateNextTimer() {
		synchronized (lock) {
			Long currentTime = getCurrentTimeInMillis();
			
			//rearmPeriodicAlarms(currentTime);
			long nearest = alarmRegistry.getNextAlarmLapse(currentTime);

			logger.trace("calculateNextTimer(), nearest alarm in (real) milliseconds : " + nearest);
			
			if (nearest >= 0) {
				long nextAlarmDelay = (long) (nearest / flowRate);

				logger.trace("calculateNextTimer(), next alarm should ring in : "
						+ nextAlarmDelay + "ms" +
						", representing " + nextAlarmDelay/60000 + " min "
						+" and " + (nextAlarmDelay-((nextAlarmDelay/60000)*60000))%1000 +  "secs");
				AlarmFiringTask nextAlarm = new AlarmFiringTask(this, currentTime+nearest);
				if (timer != null)
					timer.cancel();
				timer = new Timer();
				timer.schedule(nextAlarm, nextAlarmDelay);
				return nextAlarmDelay;
			}
			return -1;
		}
	}

	public NotificationMsg fireClockAlarmNotificationMsg(int alarmEventId) {
		return new ClockAlarmNotificationMsg(this.getAbstractObjectId(), alarmEventId);
	}



	public FlowRateSetNotification fireFlowRateSetNotificationMsg(
			double oldFlowRate, double newFlowRate) {
		return new FlowRateSetNotification(this.getAbstractObjectId(), String.valueOf(oldFlowRate),
				String.valueOf(newFlowRate));
	}
	
	public void fireAlarms(long timeStamp) {
		logger.trace("fireAlarms(long timeStamp : {})", timeStamp);
		
		Set<Integer> alarmsToFire = alarmRegistry.getFutureAlarms(timeStamp, alarmTolerance);
		for(Integer id : alarmsToFire) {
			AlarmDescriptor alarm = alarmRegistry.getAlarmFromId(id);
			logger.trace("fireAlarms(), firing alarmId: {} ", id);
			alarm.getObserver().alarmEventFired(id);
			fireClockAlarmNotificationMsg(id);

			if(alarm.getPeriod() >0) {
				logger.trace("fireAlarms(), alarmId: {} is periodic, disabling it", id);
				alarmRegistry.disableAlarm(id, (long)(alarmTolerance*flowRate));
			} else {
				logger.trace("fireAlarms(), alarmId: {} is not periodic, removing it", id);
				alarmRegistry.removeAlarm(id);				
			}
		}

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
	public int registerPeriodicAlarm(Calendar calendar, long period,
			AlarmEventObserver observer) {
		logger.trace("registerPeriodicAlarm(Calendar calendar : "
				+ AlarmDescriptor.dateFormat.format(calendar == null ? Calendar.getInstance()
						.getTime() : calendar.getTime()) + ", long period : "
				+ period + ", AlarmEventObserver observer : " + observer);

		if (period > 0 && observer != null) {
			synchronized (lock) {
				Long time;
				if (calendar != null)
					time = calendar.getTimeInMillis();
				else
					time = getCurrentTimeInMillis();
				
				int id = alarmRegistry.addNewAlarm(time, period, observer);
				
				logger.debug("registerPeriodicAlarm(...), alarm events id created : "
						+ id);
				calculateNextTimer();
				return id;
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

		Set<Integer> alarmsIds = alarmRegistry.getFutureAlarms(0, 0);
		for (Integer i : alarmsIds) {
			AlarmDescriptor alarm = alarmRegistry.getAlarmFromId(i);
			if(alarm != null && alarm.getPeriod()<=0)
				unregisterAlarm(i);
		}
		calculateNextTimer();
	}

	@Override
	public void resetPeriodicAlarms() {
		logger.trace("resetPeriodicAlarms()");
		
		Set<Integer> alarmsIds = alarmRegistry.getFutureAlarms(0, 0);
		for (Integer i : alarmsIds) {
			AlarmDescriptor alarm = alarmRegistry.getAlarmFromId(i);
			if(alarm != null && alarm.getPeriod()>0)
				unregisterAlarm(i);
		}
		calculateNextTimer();
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
