package appsgate.lig.clock.sensor.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message thrown when alarm occurs
 */

public class ClockAlarmNotificationMsg implements NotificationMsg{
    
	/**
	 * The source sensor of this notification
	 */
	private CoreObjectSpec source;
	
	private int alarmEventId;

	/**
	 * Constructor for this ApAM message.
	 * @param source the CoreObjectSource for this message 
	 * @param alarmEventId the Id that has been generated on alarm registration 
	 */
	public ClockAlarmNotificationMsg (CoreObjectSpec source, int alarmEventId) {
		this.source = source;
		this.alarmEventId = alarmEventId;
	}
	
	@Override
	public String getNewValue() {
		return String.valueOf(alarmEventId);
	}

	@Override
	public JSONObject JSONize() throws JSONException{
		
		JSONObject notif = new JSONObject();
		notif.put("objectId", source.getAbstractObjectId());
		notif.put("varName", "ClockAlarm");
		notif.put("value", String.valueOf(alarmEventId));		
		
		return notif;
	}

	@Override
	public CoreObjectSpec getSource() {
		return source;
	}

}
