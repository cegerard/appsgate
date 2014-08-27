package appsgate.lig.ard.badge.door.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

public class ARDBadgeDoorContactNotificationMsg extends CoreNotificationMsg {

    public ARDBadgeDoorContactNotificationMsg(String varName, String oldValue, String newValue,  CoreObjectSpec source) {
        super(varName, oldValue, newValue, source);
    }

}
