package appsgate.lig.agenda.core.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;

public class AlarmNotificationMsg implements NotificationMsg {

	private String eventName;
	private String alarmRingDate;
	
	public AlarmNotificationMsg(String eventName, String alarmRingDate) {
		this.eventName = eventName;
		this.alarmRingDate = alarmRingDate;
	}

	@Override
	public AbstractObjectSpec getSource() {
		return null;
	}

	@Override
	public String getNewValue() {
		return eventName;
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("source", eventName);
		obj.put("alarm", alarmRingDate);
		
		return obj;
	}

}
