package appsgate.lig.mobile.device.spec;

import org.json.JSONObject;

/**
 * This java interface is an ApAM specification shared by all ApAM AppsGate application to handle
 * mobile device specification.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since March 31, 2014
 *
 */
public interface CoreMobileDeviceSpec {
	
	/**
	 * Get all capabilities of the device
	 * @return capabilities as a JSONObject
	 * {
	 * 	"gps":true
	 * 	"accelerometer": true
	 * 	...
	 * }
	 */
	public JSONObject getCapabilites();
	

	/**
	 * Get the list of activated capabilities
	 * @return activated capabilities as a JSONObject
	 * {
	 * 	"gps":true
	 * 	"accelerometer": true
	 * 	...
	 * }
	 */
	public JSONObject getActivatedCapabilites();
	
	/**
	 * Test if the mobile device has as specified capability
	 * @param capability the capability to test
	 * @return true if the device has this capability, false otherwise
	 */
	public boolean hasCapability( String capability);
	
	/**
	 * Test is a capability is activated on the mobile device
	 * @param capability the capability to test
	 * @return true if the capability is activated, false otherwise.
	 */
	public boolean isCapabilityAsctivated(String capability);
	
	
	/**
	 * Send a notification to this device. 
	 * @param msg the textual message of the notification
	 * @param flag the flag (important, critical, info, etc.) of the notification
	 * @return true if the notification has been received
	 */
	public boolean sendNotifcation(String msg, int flag);
	
	/**
	 * Display something on the remote device
	 * @param message the message to display on the remote screen 
	 * @return true if the message can be displayed, false otherwise
	 */
	public boolean display(String message);
	
}
