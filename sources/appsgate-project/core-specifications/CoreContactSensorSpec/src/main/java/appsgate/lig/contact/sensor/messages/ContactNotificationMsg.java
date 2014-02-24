package appsgate.lig.contact.sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for switch event notification
 *
 * @author Cédric Gérard version 1.0.0
 * @since February 5, 2013
 */
public class ContactNotificationMsg extends CoreNotificationMsg {

    /**
     * The new sensor status
     */
    private final boolean isContact;

    /**
     * Constructor for this ApAM message
     *
     * @param isContact the new contact sensor status
     * @param varName
     * @param value
     * @param source
     */
    public ContactNotificationMsg(boolean isContact, String varName, String value, CoreObjectSpec source) {
        super(varName, value, source);
        this.isContact = isContact;
    }

    /**
     * Method that returns the value corresponding to this notification
     *
     * @return the new contact sensor status
     */
    public boolean getNotificationValue() {
        return isContact;
    }

    @Override
    public String getNewValue() {
        return String.valueOf(isContact);
    }

}
