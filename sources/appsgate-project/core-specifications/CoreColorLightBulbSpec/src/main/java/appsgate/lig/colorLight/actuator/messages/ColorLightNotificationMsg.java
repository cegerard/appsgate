package appsgate.lig.colorLight.actuator.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for color light event notification
 *
 * @author Cédric Gérard version 1.0.0
 * @since June 6, 2013
 */
public class ColorLightNotificationMsg extends CoreNotificationMsg {

    /**
     * Constructor of Color light ApAM message
     *
     * @param source the abstract object source of this message
     * @param varName the variable that changed
     * @param oldValue the old variable value
     * @param newValue the new variable value
     */
    public ColorLightNotificationMsg(String varName, String oldValue, String newValue, CoreObjectSpec source) {
        super(varName, oldValue, newValue, source);

    }
}
