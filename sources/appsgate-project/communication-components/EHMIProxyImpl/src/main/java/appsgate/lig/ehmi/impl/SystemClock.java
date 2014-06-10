package appsgate.lig.ehmi.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.chmi.spec.CHMIProxySpec;
import appsgate.lig.ehmi.impl.listeners.TimeObserver;
import appsgate.lig.ehmi.spec.messages.ClockAlarmNotificationMsg;

/**
 * This class hold the system clock.
 * It can be synchronized with a remote clock or just manage locally.
 * @author Cédric Gérard
 * @since April 29, 2014
 * @version 1.0.0
 *
 */
public class SystemClock {

	private static int currentAlarmId;
	public final static int defaultTimeFlowRate = 1;
	
	/**
	 * Act as primary key for single alarm event (removed after being fired)
	 */
	private long nextAlarmTime;

	/**
	 * Act as primary keys for periodic alarm events (might be not optimal)
	 */
	private Integer nextAlarmId;

	private Timer timer;
	
	private CHMIProxySpec coreProxy;
	private EHMIProxyImpl ehmiProxy;
	private String systemClockType = "21";
	private String appsgateServiceName = "SystemClock";
	private String objectID = "";
	
	/**
	 * In case we miss an alarm because of delay took for processing, we allow
	 * to fire alarms that that should have occurred until 50 ms before current
	 * time
	 */
	private long alarmLagTolerance = 50;
	
	/**
	 * A sorted map between the times in milliseconds at which are registered alarms
	 * and the corresponding alarm Id and observers
	 */
	private SortedMap<Long, Map<Integer, TimeObserver>> alarms;
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(SystemClock.class);
	
	/**
	 * Default constructor for a local system clock
	 */
	public SystemClock(EHMIProxyImpl ehmiProxy) {
		super();
		this.ehmiProxy = ehmiProxy;
		this.coreProxy = null;
		this.objectID = systemClockType + String.valueOf(appsgateServiceName.hashCode());
		alarms = new TreeMap<Long, Map<Integer, TimeObserver>>();
		currentAlarmId = 0;
		this.nextAlarmId = -1;
		this.nextAlarmTime = -1;
	}

	/**
	 * Start time synchronization from another service
	 * @param coreProxy the core service use to synchronize time
	 */
	public void startRemoteSync(CHMIProxySpec coreProxy) {
		this.coreProxy = coreProxy;
		if(isRemote()){
			objectID = coreProxy.getCoreClockObjectId();
			if (timer != null)
				timer.cancel();
			timer = null;
//			Set<Long> times = alarms.keySet();
//			TODO transfer alarm and their id to the remote alarm clock
//			for(long time : times){
//				Calendar cal  =  Calendar.getInstance();
//				cal.setTimeInMillis(time);
//				coreProxy.registerTimeAlarm(cal, "");
//			}
		} else {
			coreProxy = null;
		}
	}

	/**
	 * Stop time synchronization from another service
	 * @param coreProxy the core service use to synchronize time
	 */
	public void stopRemoteSync(CHMIProxySpec coreProxy) {
		this.coreProxy = null;
		objectID = systemClockType + String.valueOf(appsgateServiceName.hashCode());
	}

	/**
	 * Get the current abstract object identifier
	 * @return the object identifier as a String
	 */
	public String getAbstractObjectId() {
		return objectID;
	}
	
	/**
	 * Get the user type for the system clock
	 * @return the user type as a String
	 */
	public String getSystemClockType() {
		return systemClockType;
	}
	
	/**
	 * Get the current date in Java object
	 * @return the Calendar object of the current date
	 */
	public Calendar getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		if(isRemote()){
			cal.setTimeInMillis(getCurrentTimeInMillis());
		}
		return cal;	
	}
	
	/**
	 * Get the current time in milliseconds
	 * @return the time in milliseconds
	 */
	public long getCurrentTimeInMillis() {
		long timeInMillis;
		if(isRemote()){
			timeInMillis = coreProxy.getCurrentTimeInMillis();
		}else {
			timeInMillis = System.currentTimeMillis();
		}
		return timeInMillis;
	}
	
	/**
	 * Get the current time flow rate
	 * @return the flow rate as a double
	 */
	public double getTimeFlowRate() {
		if(isRemote()){
			return coreProxy.getTimeFlowRate();
		}else{
			return defaultTimeFlowRate;
		}
	}

	/**
	 * Register a new alarm notification
	 * @param calendar the time date to register
	 * @param timeObserver the call back to notify
	 * @return the registration identifier
	 */
	public int registerAlarm(Calendar calendar, TimeObserver timeObserver) {
		
		if(isRemote()){ //Register remotely
			return coreProxy.registerTimeAlarm(calendar, timeObserver.getMessage());
			
		} else { //Register locally
			if (calendar != null && timeObserver != null) {
				logger.debug("Alarm locally registration start");
				Long time = calendar.getTimeInMillis();
				if (alarms.containsKey(time)) {
					logger.debug("locally alarm events already registered for this time, adding this one");
					Map<Integer, TimeObserver> observers = alarms.get(time);
					observers.put(++currentAlarmId, timeObserver);
				}else {
					logger.debug("locally alarm events not registered for this time, creating a new local event");
					Map<Integer, TimeObserver> observers = new HashMap<Integer, TimeObserver>();
					observers.put(++currentAlarmId, timeObserver);
					alarms.put(time, observers);
				}
				logger.debug("local alarm events id created : "+currentAlarmId);
				calculateNextTimer();
				return currentAlarmId;
			}
		}
		return -1;
		
	}

	/**
	 * Unregister an existing alarm
	 * @param alarmId the registration identifier
	 */
	public void unregisterAlarm(Integer alarmId) {
		
		if(isRemote()){ //unregister remotely
			coreProxy.unregisterTimeAlarm(alarmId);
		}	
		else { // unregister locally
			logger.debug("Alarm locally unregistration start");
			Set<Long> timer = alarms.keySet();
			for(Long time : timer) {
				Map<Integer, TimeObserver> observers = alarms.get(time);
				if(observers.remove(alarmId) != null) {
					if (observers.size() < 1){
						alarms.remove(time);
					}
					logger.debug("local alarm events has been removed");
					break;
				}
			}
			calculateNextTimer();
		}
	}

	/**
	 * Determine if the clock remotely synchronize or not
	 * @return true if the clock is remotely synchronized, false otherwise
	 */
	public boolean isRemote() {
		if(coreProxy != null) {
			return coreProxy.getDevices("21") != null;
		}
		return false;
	}
	
	/**
	 * Calculate the next timer that will ring
	 * @return the next alarm delay
	 */
	private long calculateNextTimer() {
		Long currentTime = getCurrentTimeInMillis();

		long nextAlarmDelay = -1;
		nextAlarmId = -1;
		nextAlarmTime = -1;

		long nearestSingle = nearestSingleAlarmDelay(currentTime);

		logger.debug("calculateNextTimer(), nextAlarmId : " + nextAlarmId
				+ ", nextAlarmTime : " + nextAlarmTime
				+ ", nearestSingle :" + nearestSingle);

		if (nearestSingle >= 0) {
			nextAlarmDelay = nearestSingle;
			nextAlarmId = -1;
		}

		if (nextAlarmDelay >= 0) {
			nextAlarmDelay = (long) (nextAlarmDelay / defaultTimeFlowRate);

			logger.debug("calculateNextTimer(), next alarm should ring in : "+ nextAlarmDelay + "ms");
			AlarmFiringTask nextAlarm = new AlarmFiringTask();
			if (timer != null)
				timer.cancel();
			timer = new Timer();
			timer.schedule(nextAlarm, nextAlarmDelay);
			return nextAlarmDelay;
		}
		
		return -1;
	}
	
	/**
	 * Calculate the next single alarm delay
	 * @param time the current time
	 * @return the next single alarm delay
	 */
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
	
	/**
	 * Get the description of the EHMI clock
	 * @return the JSON description of the EHMI clock.
	 * 
	 * @throws JSONException
	 */
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", objectID);
		descr.put("type", systemClockType); // 21 for clock
		descr.put("status", 2);
		descr.put("sysName", appsgateServiceName);
		descr.put("remote", false);

		Calendar cal = Calendar.getInstance();
		long time = getCurrentTimeInMillis();
		cal.setTimeInMillis(time);
		descr.put("ClockSet", cal.getTime().toString());
		descr.put("clockValue", String.valueOf(time));
		descr.put("flowRate", String.valueOf(getTimeFlowRate()));

		return descr;
	}
	
	public void fireClockAlarms(Map<Integer, TimeObserver> observers) {
		if (observers != null && !observers.isEmpty()) {
			Set<Integer> removableObservers = new HashSet<Integer>();
			// Observers are removed if not periodic
			for (Integer i : observers.keySet()) {
				logger.debug("fireClockAlarms(...), AlarmEventId : " + i);
				TimeObserver obs = observers.get(i);
				removableObservers.add(i);
				if (obs != null) {
					logger.debug("fireClockAlarms(...), firing to " + obs);
					obs.alarmEventFired(i.intValue());
					fireClockAlarmNotificationMsg(i.intValue());
				}
			}
			
			for (Integer i : removableObservers) {
				logger.debug("fireClockAlarms(...), removing alarmEventId : "+ i);
				observers.remove(i);
			}
		}
	}
	
	/**
	 * Send clock notification
	 * @param alarmEventId the identifier of the alarm that ring
	 */
	public void fireClockAlarmNotificationMsg(int alarmEventId) {
		ehmiProxy.sendClockAlarmNotifcation( new ClockAlarmNotificationMsg(objectID, alarmEventId));
	}
	
	/*
	 * Inner class to manage timer task triggering
	 */
	class AlarmFiringTask extends TimerTask {
		@Override
		public void run() {
			logger.debug("Firing current clock alarms");
			if (nextAlarmTime > 0) {
				logger.debug("Firing current clock alarms to all single clock observers");
				long nextAlarmTimeClone = nextAlarmTime;
				Map<Integer, TimeObserver> observers = alarms.get(nextAlarmTimeClone);
				timer.cancel();
				timer = null;
				logger.debug("================================");
				fireClockAlarms(observers);
					if (observers.isEmpty()){
					alarms.remove(nextAlarmTimeClone);
				}
				nextAlarmTime = -1;
				logger.debug("================================");
			}
			if (nextAlarmId > 0) {
				logger.debug("Firing current clock alarms to one periodic event observer");
				fireClockAlarmNotificationMsg(nextAlarmId);
			}
			calculateNextTimer();
		}
	}
	
}
