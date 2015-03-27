package appsgate.lig.keycard.sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for key card event notifications.
 *
 * @author Cédric Gérard version 1.0.0
 * @since February 5, 2013
 */
public class KeyCardNotificationMsg extends CoreNotificationMsg {

    /**
     * Constructor for this ApAM message
     *
     */
    public KeyCardNotificationMsg(boolean isCardInserted, int cardNumber, String varName, String value, String source) {
        super(varName, value, source);
    }

    /**
     * Constructor for this ApAM message
     *
     * @param isCardInserted the new key card status
     * @param varName the variable that changed
     * @param value the new value for the variable that changed
     * @param source the source object reference
     */
    public KeyCardNotificationMsg(boolean isCardInserted, String varName, String value, String source) {
        super(varName, value, source);
    }

    /**
     * Constructor for this ApAM message
     *
     * @param cardNumber the key card id
     * @param varName the variable that changed
     * @param value the new value for the variable that changed
     * @param source the source object reference
     */
    public KeyCardNotificationMsg(int cardNumber, String varName, String value, String source) {
        super(varName, value, source);
    }
}
