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
     * The new illumination value
     */
    private final int newIllumination;


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
        this.newIllumination = newIllumination;
    }

    /**
     * Method that returns the value corresponding to this notification
     *
     * @return the new illumination as an integer
     */
    public int getNotificationValue() {
        return newIllumination;
    }

    @Override
    public String getNewValue() {
        return String.valueOf(newIllumination);
    }

}
