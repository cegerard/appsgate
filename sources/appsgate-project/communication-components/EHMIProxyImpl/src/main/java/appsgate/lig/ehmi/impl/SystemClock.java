package appsgate.lig.ehmi.impl;

import java.util.Calendar;
import java.util.Map;
import java.util.SortedMap;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.chmi.spec.CHMIProxySpec;
import appsgate.lig.ehmi.impl.listeners.TimeObserver;

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

	private CHMIProxySpec coreProxy;
	private String systemClockType = "21";
	private String appsgateServiceName = "SystemClock";
	private String objectID = "";
	private boolean remote;
	
	/**
	 * A sorted map between the times in millis at which are registered alarms
	 * and the corresponding alarm Id and observers
	 */
	SortedMap<Long, Map<Integer, TimeObserver>> alarms;
	
	public SystemClock() {
		super();
		objectID = systemClockType + String.valueOf(appsgateServiceName.hashCode());
		remote = false;
		currentAlarmId = -1;
	}

	/**
	 * Start time synchronization from another service
	 * @param coreProxy the core service use to synchronize time
	 */
	public void startRemoteSync(CHMIProxySpec coreProxy) {
		this.coreProxy = coreProxy;
		this.remote = true;
		objectID = coreProxy.getCoreClockObjectId();
	}

	/**
	 * Stop time synchronization from another service
	 * @param coreProxy the core service use to synchronize time
	 */
	public void stopRemoteSync(CHMIProxySpec coreProxy) {
		this.coreProxy = null;
		this.remote = false;
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
		if(coreProxy != null){
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
		if(coreProxy != null){
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
		if(coreProxy != null){
			return coreProxy.getTimeFlowRate();
		}else{
			return 1;
		}
	}

	/**
	 * Register a new alarm notification
	 * @param calendar the time date to register
	 * @param timeObserver the call back to notify
	 * @return the registration identifier
	 */
	public int registerAlarm(Calendar calendar, TimeObserver timeObserver) {
		
		if(coreProxy != null){ //Register remotely
			return coreProxy.registerTimeAlarm(calendar, timeObserver.getMessage());
		}
//		else { //Register locally
//			Long time = calendar.getTimeInMillis();
//			if (alarms.containsKey(time)) {
//				Map<Integer, TimeObserver> observers = alarms.get(time);
//				observers.put(++currentAlarmId, timeObserver);
//			}else {
//				Map<Integer, TimeObserver> observers = new HashMap<Integer, TimeObserver>();
//				observers.put(++currentAlarmId, timeObserver);
//				alarms.put(time, observers);
//			}
//		}
//		
//		calculateNextTimer();
//
		return currentAlarmId;
	}

	/**
	 * Unregister an existing alarm
	 * @param alarmId the registration identifier
	 */
	public void unregisterAlarm(Integer alarmId) {
		
		if(coreProxy != null){ //unregister remotely
			coreProxy.unregisterTimeAlarm(alarmId);
		}	
//		else { // unregister locally
//			Set<Long> timer = alarms.keySet();
//			
//			for(Long time : timer) {
//				Map<Integer, TimeObserver> observers = alarms.get(time);
//				if(observers.remove(alarmId) != null) {
//					if (observers.size() < 1){
//						alarms.remove(time);
//					}
//					break;
//				}
//			}
//			calculateNextTimer();
//		}
	}

	/**
	 * Determine if the clock remotely synchronize or not
	 * @return true if the clock is remotely synchronized, false otherwise
	 */
	public boolean isRemote() {
		return remote;
	}
	
	
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", objectID);
		descr.put("type", systemClockType); // 21 for clock
		descr.put("status", 2);
		descr.put("sysName", appsgateServiceName);

		Calendar cal = Calendar.getInstance();
		long time = getCurrentTimeInMillis();
		cal.setTimeInMillis(time);
		descr.put("ClockSet", cal.getTime().toString());
		descr.put("clockValue", String.valueOf(time));
		descr.put("flowRate", String.valueOf(getTimeFlowRate()));

		return descr;
	}
	
}
