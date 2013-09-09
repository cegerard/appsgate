package appsgate.lig.clock.sensor.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.clock.sensor.messages.ClockAlarmNotificationMsg;
import appsgate.lig.clock.sensor.messages.ClockSetNotificationMsg;
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

    int currentAlarmId;

    /**
     * A sorted map between the times in millis at which are registered alarms
     * and the corresponding alarm Id and observers
     */
    SortedMap<Long, Map<Integer, AlarmEventObserver>> alarms;

    /**
     * Convenient to unregister alarms (avoid complexity)
     */
    Map<Integer, Long> reverseAlarmMap;

    long nextAlarm;
    Timer timer;

    /**
     * Static class member uses to log what happened in each instances
     */
    private static Logger logger = LoggerFactory
	    .getLogger(ConfigurableClockImpl.class);

    public ConfigurableClockImpl() {
	timer = new Timer();

	initAppsgateFields();

	resetClock();
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void start() {
	logger.info("New Configurable clock created");

    }

    public void stop() {
	logger.info("Configurable Clock removed");
	timer.cancel();
	timer=null;
	//frameClock.dispose();

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
	synchronized (appsgateObjectId) {
	    currentLag = 0;
	    flowRate = 1;
	    currentAlarmId = 0;
	    alarms = new TreeMap<Long, Map<Integer, AlarmEventObserver>>();
	    reverseAlarmMap = new HashMap<Integer, Long>();
	    nextAlarm = -1;
	    timer.purge();
	    timeFlowBreakPoint = -1;
	}
	fireClockSetNotificationMsg(Calendar.getInstance());
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
	// TODO with flow might be more difficult

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
	currentLag = millis - Calendar.getInstance().getTimeInMillis();
	setTimeFlowRate(flowRate);
	calculateNextTimer();
    }

    String appsgatePictureId;
    String appsgateObjectId;
    String appsgateUserType;
    String appsgateStatus;
    String appsgateServiceName;

    protected void initAppsgateFields() {
	appsgatePictureId = null;
	appsgateServiceName = "Swing Clock";
	appsgateUserType = "21";
	appsgateStatus = "2";
	appsgateObjectId = appsgateUserType + String.valueOf(this.hashCode());
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
	descr.put("name", appsgateServiceName);

	descr.put("varName", "ClockSet");
	Calendar cal = Calendar.getInstance();
	cal.setTimeInMillis(cal.getTimeInMillis() + currentLag);
	descr.put("value", cal.getTime().toString());

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
	synchronized (appsgateObjectId) {
	    Long currentTime = getCurrentTimeInMillis();
	    SortedMap<Long, Map<Integer, AlarmEventObserver>> alarmsToFire = alarms
		    .subMap(currentTime, currentTime + millis);
	    if (alarmsToFire != null)
		for (Map<Integer, AlarmEventObserver> obs : alarmsToFire
			.values())
		    fireClockAlarms(obs);

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
	if (calendar != null && observer != null) {
	    synchronized (appsgateObjectId) {
		Long time = getCurrentTimeInMillis();
		if (alarms.containsKey(time)) {
		    logger.debug("registerAlarm(...), alarm events already registered for this time, adding this one");
		    Map<Integer, AlarmEventObserver> observers = alarms
			    .get(calendar.getTimeInMillis());
		    observers.put(++currentAlarmId, observer);
		    reverseAlarmMap.put(currentAlarmId,
			    calendar.getTimeInMillis());
		} else {
		    logger.debug("registerAlarm(...), alarm events not registered for this time, creating a new one");
		    /**
		     * An map between the alarm event ID and the associated
		     * observers
		     */
		    Map<Integer, AlarmEventObserver> observers = new HashMap<Integer, AlarmEventObserver>();
		    observers.put(++currentAlarmId, observer);
		    alarms.put(calendar.getTimeInMillis(), observers);
		    reverseAlarmMap.put(currentAlarmId,
			    calendar.getTimeInMillis());
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
	synchronized (appsgateObjectId) {
	    Long time = reverseAlarmMap.get(alarmEventId);
	    Map<Integer, AlarmEventObserver> observers = alarms.get(time);
	    observers.remove(alarmEventId);
	    if (observers.size() < 1)
		alarms.remove(time);
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
	return flowRate;

    }

    protected void calculateNextTimer() {
	synchronized (appsgateObjectId) {
	    Long currentTime = getCurrentTimeInMillis();
	    if (alarms != null && !alarms.isEmpty()
		    && alarms.lastKey() >= (currentTime - alarmLagTolerance)) {
		logger.debug("calculateNextTimer(), alarms last : "
			+ alarms.lastKey() + ", current time : " + currentTime);
		nextAlarm = alarms.tailMap(currentTime - alarmLagTolerance)
			.firstKey();
		long nextAlarmDelay = nextAlarm - currentTime;
		if (nextAlarmDelay < 0)
		    nextAlarmDelay = 0;
		nextAlarmDelay = (long) (nextAlarmDelay / flowRate);
		logger.debug("calculateNextTimer(), next alarm should ring in : "
			+ nextAlarmDelay + "ms");
		AlarmFiringTask nextAlarm = new AlarmFiringTask();
		timer.schedule(nextAlarm, nextAlarmDelay);
	    }
	}

    }

    public void fireClockAlarms(Map<Integer, AlarmEventObserver> observers) {
	if (observers != null && !observers.isEmpty())
	    for (Integer i : observers.keySet()) {
		logger.debug("fireClockAlarms(...), AlarmEventId : " + i);
		AlarmEventObserver obs = observers.remove(i);
		if (obs != null) {
		    obs.alarmEventFired(i.intValue());
		    fireClockAlarmNotificationMsg(i.intValue());
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
	    if (nextAlarm > 0) {
		Map<Integer, AlarmEventObserver> observers = alarms
			.remove(nextAlarm);
		fireClockAlarms(observers);
		nextAlarm = -1;
		this.cancel();
		calculateNextTimer();
	    }
	}
    };

}
