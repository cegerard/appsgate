package appsgate.lig.on_off.actuator.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for temperature notifications.
 *
 * @author Cédric Gérard version 1.0.0
 * @since February 4, 2013
 *
 */
public class OnOffActuatorNotificationMsg extends CoreNotificationMsg {
	
    /**
     * Constructor for this ApAM message.
     *
     * @param isOn the current state
     * @param varName the name of the variable that changed
     * @param value the value corresponding to the variable
     * @param source the source of this message
     */
    public OnOffActuatorNotificationMsg(String isOn, String varName, String value, String source) {
        super(varName, value, source);
    }

}
