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
     * @param value the new variable value
     */
    public ColorLightNotificationMsg(CoreObjectSpec source, String varName, String value) {
        super(varName, value, source);

    }
}
