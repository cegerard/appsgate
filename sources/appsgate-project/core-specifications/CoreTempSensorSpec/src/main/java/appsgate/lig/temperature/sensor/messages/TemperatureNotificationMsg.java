package appsgate.lig.temperature.sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;

import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for temperature notifications.
 *
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 4, 2013
 *
 */
public class TemperatureNotificationMsg extends CoreNotificationMsg {


    /**
     * The new temperature value
     */
    private final float newTemperature;


    /**
     * Constructor for this ApAM message.
     *
     * @param newTemperature the new temperature value
     * @param varName
     * @param value
     * @param source
     */
    public TemperatureNotificationMsg(float newTemperature, String varName, String value, String source) {
        super(varName, value, source);
        this.newTemperature = newTemperature;
    }

    /**
     * Method that returns the value corresponding to this notification
     *
     * @return the new temperature as a float
     */
    public float getNotificationValue() {
        return newTemperature;
    }

    @Override
    public String getNewValue() {
        return String.valueOf(newTemperature);
    }

}
