package appsgate.lig.co2.sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for Co2 concentration notifications
 *
 * @author Cédric Gérard
 * @version 1.0.0
 * @since November 28, 2013
 */
public class Co2NotificationMsg extends CoreNotificationMsg {

    /**
     * Constructor for this ApAM message
     *
     * @param varName
     * @param source
     */
    public Co2NotificationMsg(String varName, String oldValue, String newValue,String source) {
        super(varName, oldValue, newValue, source);
    }
}
