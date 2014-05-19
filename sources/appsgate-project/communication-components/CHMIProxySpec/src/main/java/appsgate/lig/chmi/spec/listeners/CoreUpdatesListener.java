package appsgate.lig.chmi.spec.listeners;

import org.json.JSONObject;

/**
 * This interface is a listener template  for core Updates notifications
 * @author Cédric Gérard
 * @since April 23, 2014
 * @version 1.0.0 
 */
public interface CoreUpdatesListener {
	
	/**
	 * Get the core type of the core object
	 * {device, service, simulated_device, simulated_service}
	 * @return the core type as a String
	 */
	public String getCoreType();
	
	/**
	 * Get the user type of the core object
	 * The user type is a string that code the device
	 * type like color light, switch, smart plug, contact, etc. 
	 * @return the user type as a coded string
	 */
	public String getUserType();
	
	/**
	 * Get the core object description
	 * @return the description as a JSONObject
	 */
	public JSONObject getObjectDescription();
	
	/**
	 * Get the core object behavior description
	 * @return the behavior description as a JSONObject
	 */
	public JSONObject getBehaviorDescription();
	
	/**
	 * Notify that a new update has come
	 */
	public void notifyUpdate(String coreType, String objectId, String userType, JSONObject descirption, JSONObject behavior);
	
}
