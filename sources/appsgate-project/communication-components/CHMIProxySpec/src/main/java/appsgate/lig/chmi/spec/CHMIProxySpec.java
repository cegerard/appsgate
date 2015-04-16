package appsgate.lig.chmi.spec;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import appsgate.lig.chmi.spec.listeners.CoreEventsListener;
import appsgate.lig.chmi.spec.listeners.CoreUpdatesListener;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * Specification of the CHMI proxy that offer services about
 * core objects
 * 
 * @author Cedric Gérard
 * @version 1.0.0
 *
 */
public interface CHMIProxySpec {
	
	/**
	 * Subscribe to core updates (add/remove object)
	 * @param coreUpdatesListener the listener for subscription
	 * @return true if the listener is registered, false otherwise
	 */
	public boolean CoreUpdatesSubscribe(CoreUpdatesListener coreUpdatesListener);
	
	/**
	 * Disable subscription for core updates notification
	 * @param coreUpdatesListener the listener to unregister
	 * @return true if the listener is unregistered, false otherwise
	 */
	public boolean CoreUpdatesUnsubscribe(CoreUpdatesListener coreUpdatesListener);
	
	/**
	 * Subscribe to core events notification
	 * @param coreEventListener the listener for event subscription
	 * @return true if the listener is registered, false otherwise
	 */
	public boolean CoreEventsSubscribe(CoreEventsListener coreEventsListener);
	
	/**
	 * Disable subscription for core events notification
	 * @param coreEventsListener the listener to unregister
	 * @return true if the listener is unregistered, false otherwise
	 */
	public boolean CoreEventsUnsubscribe(CoreEventsListener coreEventsListener);
	
	/**
	 * Execute command from outside to a specific device
	 * 
     * @param clientId client identifier
     * @param objectId abstract object identifier
     * @param methodName method to call on objectId
	 * @param jsonArgs arguments and their type as an JSONArray
     * @param callId the remote call identifier
     * @return a Runnable object that can be execute everywhere.
	 */
    public GenericCommand executeCommand(int clientId, String objectId, String methodName, JSONArray jsonArgs, String callId,
    		AsynchronousCommandResponseListener listener);	
	
	 
	/**
	 * Get all the devices description as JSONArray
	 */
	public JSONArray getDevicesDescription();
	
	/**
	 * Get all the devices description as JSONArray
	 */
	public JSONArray getDevicesId();	
	
	/**
	 * Get the device description
	 * @param objectId the object identifier
	 * @return the object information as a JSONObject
	 */
	public JSONObject getDeviceDescription(String objectId);
	
	/**
	 * Get all the device that fit the type parameter
	 * @param type the type parameter
	 * @return the device list of the same "type"
	 */
	public JSONArray getDevicesIdFromType(String type);
	
	/**
	 * Get all the device that fit the type parameter
	 * @param type the type parameter
	 * @return the device list of the same "type"
	 */
	public JSONArray getDevicesDescriptionFromType(String type);
	

    /**
     * Get the grammar behavior of a particular device type
     * @param type the type parameter
     * @return the JSON representation of the Grammar
     */
    public JSONObject getDeviceBehaviorFromType(String type);
    
    /**
     * Get the core object from its identifier
     * @param objectId the object identifier
     * @return the core object instance
     */
    public CoreObjectSpec getCoreDevice(String objectId);
    
	
	/**
	 * Get the identifier of the core clock
	 * @return the core clock identifier as a string or null if there is no core clock
	 */
	public String getCoreClockObjectId();
	
	/************************************/
	/**       Core clock commands      **/
	/************************************/
	
	/**
	 * Register a time alarm
	 * @param calendar the date when the alarm will ring
	 * @param message a message from the requester
	 * @return the alarm identifier
	 */
	public int registerTimeAlarm(Calendar calendar, String message);

	/**
	 * Unregister a alarm from its identifier
	 * @param alarmId the alarm identifier
	 */
	public void unregisterTimeAlarm(Integer alarmId);
	
	/**
	 * Get the current system time in milliseconds
	 * @return the time in milliseconds as a long
	 */
	public long getCurrentTimeInMillis();
	
	/**
	 * Get the current time flow rate
	 * @return the current time flow
	 */
	public double getTimeFlowRate();
	
	/************************************/
	/**      General core commands     **/
	/************************************/
	
	/**
	 * Shutdown the system
	 * (Shutdown the OSGi distribution)
	 */
	public void shutdown();
	
	/**
	 * restart the system
	 * (Restart the system bundle from OSGi)
	 */
	public void restart();

}
