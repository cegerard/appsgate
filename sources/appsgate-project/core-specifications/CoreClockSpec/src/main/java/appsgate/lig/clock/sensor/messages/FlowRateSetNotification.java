package appsgate.lig.clock.sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message trigger when the flow rate is changed
 *
 * @author Cédric Gérard
 * @since September 27, 2013
 * @version 1.0.0
 *
 */
public class FlowRateSetNotification extends CoreNotificationMsg {

    /**
     * Build a new Time flow rate notification
     *
     * @param source the core object source of this notification
     * @param flowRate the new time flow rate
     */
    public FlowRateSetNotification(CoreObjectSpec source, String flowRate) {
        super("flowRate", flowRate, source);
    }

}
