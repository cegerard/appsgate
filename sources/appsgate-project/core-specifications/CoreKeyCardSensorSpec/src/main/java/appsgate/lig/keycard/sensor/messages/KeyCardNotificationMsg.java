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
     * The new key card status. True if a card has been inserted and false
     * otherwise
     */
    private final boolean isCardInserted;

    /**
     * The card number that as as been pass on or inserted.
     */
    private final int cardNumber;

    /**
     * Constructor for this ApAM message
     *
     * @param isCardInserted the new key card status
     * @param cardNumber the inserted card number (-1 if no id)
     * @param varName the variable that changed
     * @param value the new value for the variable that changed
     * @param source the source object reference
     */
    public KeyCardNotificationMsg(boolean isCardInserted, int cardNumber, String varName, String value, CoreObjectSpec source) {
        super(varName, value, source);
        this.isCardInserted = isCardInserted;
        this.cardNumber = cardNumber;
    }

    /**
     * Constructor for this ApAM message
     *
     * @param isCardInserted the new key card status
     * @param varName the variable that changed
     * @param value the new value for the variable that changed
     * @param source the source object reference
     */
    public KeyCardNotificationMsg(boolean isCardInserted, String varName, String value, CoreObjectSpec source) {
        super(varName, value, source);
        this.isCardInserted = isCardInserted;
        this.cardNumber = -1;
    }

    /**
     * Constructor for this ApAM message
     *
     * @param cardNumber the key card id
     * @param varName the variable that changed
     * @param value the new value for the variable that changed
     * @param source the source object reference
     */
    public KeyCardNotificationMsg(int cardNumber, String varName, String value, CoreObjectSpec source) {
        super(varName, value, source);
        this.isCardInserted = false;
        this.cardNumber = cardNumber;
    }

    /**
     * Method that returns if a card is inserted
     *
     * @return the new key card status
     */
    public boolean getInsertedValue() {
        return isCardInserted;
    }

    /**
     * Method that returns the card number that trigger the notification
     *
     * @return the card number
     */
    public int getCardNumber() {
        return cardNumber;
    }

    @Override
    public String getNewValue() {
        return String.valueOf(isCardInserted) + "/" + String.valueOf(cardNumber);
    }
}
