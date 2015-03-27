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
     * Constructor for this ApAM message.
     *
     * @param newTemperature the new temperature value
     * @param varName
     * @param value
     * @param source
     */
    public TemperatureNotificationMsg(float newTemperature, String varName, String value, String source) {
        super(varName, value, source);
    }


}
