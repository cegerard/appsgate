package appsgate.lig.clock.sensor.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message thrown when alarm occurs
 */
public class ClockAlarmNotificationMsg extends CoreNotificationMsg {

    /**
     * Constructor for this ApAM message.
     *
     * @param source the CoreObjectSource for this message
     * @param alarmEventId the Id that has been generated on alarm registration
     */
    public ClockAlarmNotificationMsg(CoreObjectSpec source, int alarmEventId) {
        super("ClockAlarm", "", String.valueOf(alarmEventId), source);
    }

}
