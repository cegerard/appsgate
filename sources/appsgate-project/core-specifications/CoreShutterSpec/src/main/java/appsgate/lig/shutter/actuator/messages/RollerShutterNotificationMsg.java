package appsgate.lig.shutter.actuator.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;


public class RollerShutterNotificationMsg extends CoreNotificationMsg {


    public RollerShutterNotificationMsg(String varName, String oldValue, String newValue, CoreObjectSpec source) {
        super(varName, oldValue, newValue, source);

    }
}
