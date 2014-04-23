package appsgate.lig.chmi.spec;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import appsgate.lig.chmi.spec.listeners.CoreEventsListener;
import appsgate.lig.chmi.spec.listeners.CoreUpdatesListener;

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
	 * Execute a command from the outside to a specific device
	 * @param objectId the target object
	 * @param methodName the method to call
	 * @param args argument of the method
	 * @param paramType type of those arguments
	 * @return a Runnable object that can be execute everywhere.
	 */
	@SuppressWarnings("rawtypes")
	public GenericCommand executeCommand(String objectId, String methodName, ArrayList<Object> args, ArrayList<Class> paramType);

	/**
	 * Execute command from outside to a specific device
	 * @param objectId the targeted object
	 * @param methodName the method to call
	 * @param args arguments and their type as an JSONArray
	 * @return a Runnable object that can be execute everywhere.
	 */
	public GenericCommand executeCommand(String objectId, String methodName, JSONArray args);
	
	/**
	 * Get all the devices description as JSONArray
	 */
	public JSONArray getDevices();
	
	/**
	 * Get the device description
	 * @param objectId the object identifier
	 * @return the object information as a JSONObject
	 */
	public JSONObject getDevice(String objectId);
	
	/**
	 * Get all the device that fit the type parameter
	 * @param type
	 * 			the type parameter
	 * @return the device list of the same "type"
	 */
	public JSONArray getDevices(String type);
}
