package appsgate.lig.ehmi.spec.messages;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is an ApAM message thrown when alarm occurs
 */
public class ClockAlarmNotificationMsg implements NotificationMsg {

	private String source;
	private String varName;
	private String value;
	
    /**
     * Constructor for this ApAM message.
     *
     * @param source the CoreObjectSource for this message
     * @param alarmEventId the Id that has been generated on alarm registration
     */
    public ClockAlarmNotificationMsg(String source, int alarmEventId) {
    	this.source = source;
    	this.varName = "ClockAlarm";
    	this.value = String.valueOf(alarmEventId);
    }

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public String getNewValue() {
		return value;
	}

	@Override
	public String getVarName() {
		return varName;
	}

	@Override
	public JSONObject JSONize() {
		JSONObject notif = new JSONObject();
        try {
            notif.put("objectId", source);
            notif.put("varName", varName);
            notif.put("value", value);
        } catch (JSONException ex) {
                    // Will never be thrown
        }

        return notif;
	}

}
