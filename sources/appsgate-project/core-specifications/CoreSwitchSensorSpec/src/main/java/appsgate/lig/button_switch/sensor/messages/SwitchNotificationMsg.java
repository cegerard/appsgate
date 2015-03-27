package appsgate.lig.button_switch.sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for switch event notifications.
 *
 * @author Cédric Gérard version 1.0.0
 * @since February 5, 2013
 */
public class SwitchNotificationMsg extends CoreNotificationMsg {

    /**
     * Constructor for this ApAM message
     *
     * @param switchNumber the button pressed (for multiple buttons switch)
     * @param state the button state
     * @param varName
     * @param value
     * @param source
     */
    public SwitchNotificationMsg(int switchNumber, String state, String varName, String value, String source) {
        super(varName, value, source);
    }


}
