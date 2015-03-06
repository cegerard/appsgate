package appsgate.lig.smartplug.actuator_sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for smart plug event notification
 *
 * @author Cédric Gérard version 1.0.0
 * @since August 13, 2013
 */
public class SmartPlugNotificationMsg extends CoreNotificationMsg{

    /**
     * Constructor of Smart Plug ApAM message
     *
     * @param source the abstract object source of this message
     * @param varName the variable that changed
     * @param value the new variable value
     */
    public SmartPlugNotificationMsg(String source, String varName, String value) {
        super(varName, value, source);
    }
}
