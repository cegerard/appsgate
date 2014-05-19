package appsgate.lig.ehmi.spec.messages;

import org.json.JSONObject;

/**
 * 
 * Java interface for all ApAM message with AppsGate.
 * 
 * @author Cédric Gérard
 * @since February 13, 2013
 * @version 1.0.0
 *
 */
public interface NotificationMsg {
	
	/**
	 * Get the source of this notification
	 * @return the source of the notificationas a String
	 */
	public String getSource();
	
	/**
	 * Get the new value.
	 * 
	 * @return a string that represent the new value.
	 */
	public String getNewValue();
	
	/**
	 * Get the variable that changed
	 * @return a string that contains the variable name.
	 */
	public String getVarName();
	
	/**
	 * transform the NotifcationMsg object to JSONSObject.
	 * 
	 * @return a JSONObject that represent the NotificationMsg java object
	 */
	public JSONObject JSONize();

}
