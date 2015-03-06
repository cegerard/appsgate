package appsgate.lig.mobile.device.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for mobilde device data notifications
 * @author Cédric Gérard
 * @version 1.0.0
 * @since March 31, 2014
 *
 */
public class MobileDeviceNotificationMsg extends CoreNotificationMsg {

	/**
	 * Build a new mobile device notification message
	 * @param varName the name of the variable that changed
	 * @param value the new value of the variable
	 * @param source the instance that trigger this notification message
	 */
	public MobileDeviceNotificationMsg(String varName, String value, String source) {
		super(varName, value, source);
		// TODO Auto-generated constructor stub
	}
	
	@Override
    public String getNewValue() {
        return String.valueOf("");
    }

}
