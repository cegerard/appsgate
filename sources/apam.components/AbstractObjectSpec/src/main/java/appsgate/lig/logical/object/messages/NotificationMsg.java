package appsgate.lig.logical.object.messages;

import org.json.simple.JSONObject;

import appsgate.lig.logical.object.spec.AbstractObjectSpec;

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
	 * @return the AbstractObject view of the source object or service
	 */
	public AbstractObjectSpec getSource();
	
	/**
	 * Get the new value.
	 * 
	 * @return a string that represent the new value.
	 */
	public String getNewValue();
	
	/**
	 * transform the NotifcationMsg object to JSONSObject.
	 * 
	 * @return a JSONObject that represent the NotificationMsg java object
	 */
	public JSONObject JSONize();

}
