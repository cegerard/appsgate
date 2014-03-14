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
     * The new CO2 value
     */
    private final float newCO2Concentration;


    /**
     * Constructor for this ApAM message
     *
     * @param newCo2Concentration
     * @param varName
     * @param value
     * @param source
     */
    public Co2NotificationMsg(float newCo2Concentration, String varName, String value, CoreObjectSpec source) {
        super(varName, value, source);
        this.newCO2Concentration = newCo2Concentration;
    }

    /**
     * Method that returns the value corresponding to this notification
     *
     * @return the new Co2 concentration as a float
     */
    public float getNotificationValue() {
        return newCO2Concentration;
    }


    @Override
    public String getNewValue() {
        return String.valueOf(newCO2Concentration);
    }
}
