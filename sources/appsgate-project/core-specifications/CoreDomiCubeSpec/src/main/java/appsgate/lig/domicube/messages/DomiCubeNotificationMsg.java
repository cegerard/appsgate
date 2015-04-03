package appsgate.lig.domicube.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for DomiCube state change notifications
 * 
 * @author Cédric Gérard
 * @since April 16, 2014
 *
 */
public class DomiCubeNotificationMsg extends CoreNotificationMsg {

    public DomiCubeNotificationMsg(String varName, String oldValue, String newValue, String source) {
        super(varName, oldValue, newValue , source);
    }

}
