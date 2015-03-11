package appsgate.lig.button_switch.sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for switch event notifications.
 *
 * @author Cédric Gérard version 1.0.0
 * @since February 5, 2013
 */
public class SwitchNotificationMsg extends CoreNotificationMsg {


    /**
     * The number of the pressed switch
     */
    private final Integer switchNumber;

    /**
     * The button state On/Off = Up/Down
     */
    private final String state;


    /**
     * Constructor for this ApAM message
     *
     * @param switchNumber the button pressed (for multiple buttons switch)
     * @param state the button state
     * @param varName
     * @param value
     * @param source
     */
    public SwitchNotificationMsg(int switchNumber, String state, String varName, String value, String source) {
        super(varName, value, source);
        this.switchNumber = switchNumber;
        this.state = state;
    }

    /**
     * Method that returns the value corresponding to this notification
     *
     * @return a descriptive string of the button state.
     */
    public String getNotificationValue() {
        return switchNumber + " " + state;
    }

    /**
     * Get the switch number
     *
     * @return switchNumber, the number of the pressed button
     */
    public int getSwitchNumber() {
        return switchNumber;
    }

    /**
     * Get the button state
     *
     * @return isOn, the state of the button
     */
    public boolean isOn() {
        return (state.contentEquals("true"));
    }

    /**
     * Get the current switch state
     *
     * @return the state as a string. (true, false, none)
     */
    public String getState() {
        return state;
    }

    @Override
    public String getNewValue() {
        return switchNumber.toString();
    }

}
