package appsgate.lig.weather.extended.spec.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for switch event notification
 *
 * @author Cédric Gérard version 1.0.0
 * @since February 5, 2013
 */
public class DaylightNotificationMsg extends CoreNotificationMsg {

    /**
     * The new sensor status
     */
    private final boolean daylightStatus;

    /**
     * Constructor for this ApAM message
     *
     * @param daylightStatus the new contact sensor status
     * @param varName
     * @param value
     * @param source
     */
    public DaylightNotificationMsg(boolean daylightStatus, String varName, String value, CoreObjectSpec source) {
        super(varName, value, source);
        this.daylightStatus = daylightStatus;
    }

    /**
     * Method that returns the value corresponding to this notification
     *
     * @return the new contact sensor status
     */
    public boolean getNotificationValue() {
        return daylightStatus;
    }

    @Override
    public String getNewValue() {
        return String.valueOf(daylightStatus);
    }

}
