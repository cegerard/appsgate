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
     * Constructor for this ApAM message
     *

     */
    public ContactNotificationMsg(String varName, String oldValue, String newValue,  CoreObjectSpec source) {
        super(varName, oldValue, newValue, source);
    }

}
