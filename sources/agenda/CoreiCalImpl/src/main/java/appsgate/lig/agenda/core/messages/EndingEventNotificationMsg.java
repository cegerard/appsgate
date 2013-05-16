package appsgate.lig.agenda.core.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;

public class EndingEventNotificationMsg implements NotificationMsg {

	private String eventName;
	
	public EndingEventNotificationMsg(String name) {
		eventName = name;
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
		obj.put("event", eventName);
		
		return obj;
	}

}
