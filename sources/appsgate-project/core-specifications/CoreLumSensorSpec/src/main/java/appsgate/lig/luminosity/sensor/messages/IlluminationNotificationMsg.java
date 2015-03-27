package appsgate.lig.luminosity.sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for illumination notifications
 *
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 5, 2013
 */
public class IlluminationNotificationMsg extends CoreNotificationMsg {

    /**
     * Constructor for this ApAM message
     *
     * @param newIllumination the new illumination value
     * @param varName
     * @param source
     * @param value
     */
    public IlluminationNotificationMsg(int newIllumination, String varName, String value, String source) {
        super(varName, value, source);
    }


}
