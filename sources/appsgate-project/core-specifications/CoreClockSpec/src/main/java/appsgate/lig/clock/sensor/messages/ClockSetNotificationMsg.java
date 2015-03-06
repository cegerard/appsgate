package appsgate.lig.clock.sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message thrown when current Time has been set for the
 * clock (TimeZone change, Delay reset, specific time setting, ...)
 */
public class ClockSetNotificationMsg extends CoreNotificationMsg {

    /**
     * Constructor for this ApAM message.
     *
     * @param source the CoreObjectSource for this message
     * @param currentDate a String representation of the current date and time
     */
    public ClockSetNotificationMsg(String source, String oldDate, String currentDate) {
        super("ClockSet", oldDate, currentDate, source);

    }

}
