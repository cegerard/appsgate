package appsgate.lig.occupancy.sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for occupancy notifications.
 *
 * @author Cédric Gérard
 * @version 1.0.0
 * @since November 25, 2013
 *
 */
public class OccupancyNotificationMsg extends CoreNotificationMsg {

    /**
     * The new occupancy value
     */
    private final boolean newOccupancy;

    /**
     * Constructor or this ApAM message.
     *
     * @param source the source instance of this notification
     * @param newOccupancy the new occupancy state
     * @param varName the property name that change
     * @param value the new property value
     */
    public OccupancyNotificationMsg(String source, boolean newOccupancy, String varName, String value) {
        super(varName, value, source);
        this.newOccupancy = newOccupancy;
    }

    /**
     * Method that returns the value corresponding to this notification
     *
     * @return the new occupancy as a boolean
     */
    public boolean getNotificationValue() {
        return newOccupancy;
    }


    @Override
    public String getNewValue() {
        return String.valueOf(newOccupancy);
    }
}
