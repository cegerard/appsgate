package appsgate.lig.clock.sensor.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

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
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This java interface is an ApAM specification shared by all ApAM AppsGate
 * application to provide current Time and Date information
 */
public class ConfigurableClockImpl implements CoreClockSpec, CoreObjectSpec {

    /**
     * Lag between the real current Date and the one setted
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
    SortedMap<Long, Map<Integer, AlarmEventObserver>> alarms;

    /**
     * A map between the periodic alarms, matches the same alarm id as an alarm
     * registered at current time
     */
    Map<Integer, AlarmEventObserver> periodicAlarmObservers;

    /**
     * A map between the periods in millis of periodic alarms, matches the same
     * alarm id as an alarm registered at current time
     */
    Map<Integer, Long> alarmPeriods;

    /**
     * As single time alarms are removed from the list they do not fire events
     * two times But periodic events aren't removed, so we need to disarm them
     */
    Set<Integer> disarmedAlarms;

    /**
     * Convenient to unregister alarms (avoid complexity)
     */
    Map<Integer, Long> reverseAlarmMap;

    Timer timer;

    Object lock;
    boolean runningArmTimer;

    /**
     * Static class member uses to log what happened in each instances
     */
    private static Logger logger = LoggerFactory
	    .getLogger(ConfigurableClockImpl.class);

    public ConfigurableClockImpl() {
	lock = new Object();
	resetClock();
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void start() {
	logger.info("New Configurable clock created");
	initAppsgateFields();
    }

    public void stop() {
	logger.info("Configurable Clock removed");
	if (timer != null)
	    timer.cancel();
	timer = null;

    }

    public NotificationMsg fireClockSetNotificationMsg(Calendar currentTime) {
	return new ClockSetNotificationMsg(this, currentTime.getTime()
		.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.clock.sensor.spec.CoreClockSpec#resetClock()
     */
    @Override
    public void resetClock() {
	synchronized (lock) {
	    currentLag = 0;
	    flowRate = 1;
	    currentAlarmId = 0;
	    alarms = new TreeMap<Long, Map<Integer, AlarmEventObserver>>();
	    reverseAlarmMap = new HashMap<Integer, Long>();
	    periodicAlarmObservers = new HashMap<Integer, AlarmEventObserver>();
	    alarmPeriods = new HashMap<Integer, Long>();
	    disarmedAlarms = new HashSet<Integer>();

	    nextAlarmId = -1;
	    nextAlarmTime = -1;
	    runningArmTimer = false;
	    if (timer != null) {
		timer.cancel();
		timer = null;
	    }

	    timeFlowBreakPoint = -1;
	}
	fireClockSetNotificationMsg(Calendar.getInstance());
	fireFlowRateSetNotificationMsg(flowRate);
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
	    logger.debug("getCurrentTimeInMillis(), system time : "
		    + systemTime + ", time flow breakpoint : "
		    + timeFlowBreakPoint + ", elasped time : " + elapsedTime);

	    simulatedTime += timeFlowBreakPoint + elapsedTime;
	} else
	    simulatedTime += systemTime;
	logger.debug("getCurrentTimeInMillis(), simulated time : "
		+ simulatedTime);
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
	logger.debug("setCurrentTimeInMillis(long millis : "
		+ millis+")");
	
	currentLag = millis - Calendar.getInstance().getTimeInMillis();
	setTimeFlowRate(flowRate);
	calculateNextTimer();
	Calendar calendar = Calendar.getInstance();
	calendar.setTimeInMillis(millis);
	fireClockSetNotificationMsg(calendar);
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
	logger.debug("goAlongUntil(long millis : "
		+ millis+")");
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
		for (Integer i : alarmPeriods.keySet()) {
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
		    if (futureTime - time < alarmLagTolerance)
			disarmedAlarms.add(i);
		    else
			disarmedAlarms.remove(i);
		}

	    setCurrentTimeInMillis(currentTime + millis);
	}
	calculateNextTimer();
	if (!disarmedAlarms.isEmpty() && !runningArmTimer) {
	    RearmingPeriodicAlarmTask arming = new RearmingPeriodicAlarmTask();
	    if (timer == null)
		timer = new Timer();
	    timer.schedule(arming, alarmLagTolerance + 1);
	    runningArmTimer = true;

	}

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
	if (calendar != null && observer != null) {
	    synchronized (lock) {
		Long time = calendar.getTimeInMillis();
		if (alarms.containsKey(time)) {
		    logger.debug("registerAlarm(...), alarm events already registered for this time, adding this one");
		    Map<Integer, AlarmEventObserver> observers = alarms
			    .get(time);
		    observers.put(++currentAlarmId, observer);
		    reverseAlarmMap.put(currentAlarmId, time);
		} else {
		    logger.debug("registerAlarm(...), alarm events not registered for this time, creating a new one");
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
		return currentAlarmId;
	    }
	} else {
	    logger.debug("registerAlarm(...), calendar or oberver is null, does not register the alarm");
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

	    reverseAlarmMap.remove(alarmEventId);
	    calculateNextTimer();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.clock.sensor.spec.CoreClockSpec#setTimeFlowRate(double)
     */
    @Override
    public double setTimeFlowRate(double rate) {
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
	fireFlowRateSetNotificationMsg(rate);
	return flowRate;

    }

    void rearmPeriodicAlarms(Long time) {
	if (time == null)
	    time = getCurrentTimeInMillis();
	if (disarmedAlarms != null && !disarmedAlarms.isEmpty()
		&& reverseAlarmMap != null && !reverseAlarmMap.isEmpty()
		&& alarmPeriods != null && !alarmPeriods.isEmpty())
	    for (Integer i : disarmedAlarms) {
		if (Math.abs((reverseAlarmMap.get(i).longValue() - time
			.longValue()) % alarmPeriods.get(i).longValue()) > alarmLagTolerance)
		    disarmedAlarms.remove(i);
	    }
    }

    long nearestSingleAlarmDelay(long time) {
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
	if (time < 0)
	    time = getCurrentTimeInMillis();
	boolean initMin = false;
	long minPeriodValue = -1;

	for (Integer i : alarmPeriods.keySet())
	    if (!disarmedAlarms.contains(i)) {
		Long nextPeriodic = (time - reverseAlarmMap.get(i))
			% alarmPeriods.get(i);
		if (nextPeriodic >= 0) {
		    nextPeriodic = alarmPeriods.get(i) - nextPeriodic;

		    logger.debug("next periodic : " + nextPeriodic);
		    if (!initMin || nextPeriodic < minPeriodValue) {
			initMin = true;
			minPeriodValue = nextPeriodic;
			nextAlarmId = i;
		    }
		}
	    }
	return minPeriodValue;
    }

    long calculateNextTimer() {
	synchronized (lock) {
	    Long currentTime = getCurrentTimeInMillis();
	    rearmPeriodicAlarms(currentTime);

	    long nextAlarmDelay = -1;
	    nextAlarmId = -1;
	    nextAlarmTime = -1;

	    long nearestSingle = nearestSingleAlarmDelay(currentTime);
	    long nearestPeriodic = nearestPeriodicAlarmDelay(currentTime);

	    logger.debug("calculateNextTimer(), nextAlarmId : " + nextAlarmId
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

		logger.debug("calculateNextTimer(), next alarm should ring in : "
			+ nextAlarmDelay + "ms");
		AlarmFiringTask nextAlarm = new AlarmFiringTask();
		if (timer == null)
		    timer = new Timer();
		timer.schedule(nextAlarm, nextAlarmDelay);
		return nextAlarmDelay;
	    } else // TODO : Check or fix  as it creates a polling if there are alarms in the queue, but no current timer
		    if (!runningArmTimer && !reverseAlarmMap.isEmpty()) {
			RearmingPeriodicAlarmTask arming = new RearmingPeriodicAlarmTask();
			if (timer == null)
			    timer = new Timer();
			timer.schedule(arming, alarmLagTolerance + 1);
			runningArmTimer = true;
		    }

	    return -1;
	}
    }

    public void fireClockAlarms(Map<Integer, AlarmEventObserver> observers) {
	if (observers != null && !observers.isEmpty()) {
	    Set<Integer> removableObservers = new HashSet<Integer>(); // Observers
								      // are
								      // removed
								      // if not
								      // periodic
	    for (Integer i : observers.keySet()) {
		logger.debug("fireClockAlarms(...), AlarmEventId : " + i);
		AlarmEventObserver obs = observers.get(i);
		removableObservers.add(i);

		if (obs != null) {
		    logger.debug("fireClockAlarms(...), firing to " + obs);

		    obs.alarmEventFired(i.intValue());
		    fireClockAlarmNotificationMsg(i.intValue());
		}

	    }
	    for (Integer i : removableObservers) {
		logger.debug("fireClockAlarms(...), removing alarmEventId : "
			+ i);
		observers.remove(i);
		reverseAlarmMap.remove(i);
	    }
	}
    }

    public NotificationMsg fireClockAlarmNotificationMsg(int alarmEventId) {
	return new ClockAlarmNotificationMsg(this, alarmEventId);
    }

    class AlarmFiringTask extends TimerTask {
	@Override
	public void run() {
	    logger.debug("Firing current clock alarms");
	    long next = -1;
	    if (nextAlarmTime > 0) {
		logger.debug("Firing current clock alarms to all single clock observers");
		Map<Integer, AlarmEventObserver> observers = alarms
			.get(nextAlarmTime);
		fireClockAlarms(observers);
		if (observers.isEmpty())
		    alarms.remove(nextAlarmTime);
		nextAlarmTime = -1;
		timer.cancel();
		timer = null;
		next = calculateNextTimer();
	    }
	    if (nextAlarmId > 0) {
		logger.debug("Firing current clock alarms to one periodic event observer");

		periodicAlarmObservers.get(nextAlarmId).alarmEventFired(
			nextAlarmId);
		fireClockAlarmNotificationMsg(nextAlarmId);
		disarmedAlarms.add(nextAlarmId);
		long next2 = calculateNextTimer();
		if (next<0 ||(next2>=0 && next2<next))
		    next=next2;
	    }
	    if (next < 0 && !runningArmTimer && !disarmedAlarms.isEmpty()  && !reverseAlarmMap.isEmpty()) {
		RearmingPeriodicAlarmTask arming = new RearmingPeriodicAlarmTask();
		if (timer == null)
		    timer = new Timer();
		timer.schedule(arming, alarmLagTolerance + 1);
		runningArmTimer = true;
	    }

	}
    }

    public NotificationMsg fireFlowRateSetNotificationMsg(double newFlowRate) {
	return new FlowRateSetNotification(this, String.valueOf(newFlowRate));
    }

    class RearmingPeriodicAlarmTask extends TimerTask {
	@Override
	public void run() {
	    logger.debug("trying to rearm periodic clock alarms");
	    runningArmTimer = false;
	    rearmPeriodicAlarms(null);
	    calculateNextTimer();
	}
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

	if (millis > 0 && observer != null) {
	    synchronized (lock) {
		Long time;
		if (calendar != null)
		    time = calendar.getTimeInMillis();
		else
		    time = getCurrentTimeInMillis();

		logger.debug("registerPeriodicAlarm(...), creating a new one");
		periodicAlarmObservers.put(++currentAlarmId, observer);
		reverseAlarmMap.put(currentAlarmId, time);
		alarmPeriods.put(currentAlarmId, millis);

		logger.debug("registerPeriodicAlarm(...), alarm events id created : "
			+ currentAlarmId);
		calculateNextTimer();
		return currentAlarmId;
	    }
	} else {
	    logger.debug("registerPeriodicAlarm(...), millis incorrect or observer is null, does not register the alarm");
	    return -1;
	}
    }

}
