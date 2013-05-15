package appsgate.lig.agenda.core.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;

public class StartingEventNotificationMsg implements NotificationMsg {
	
	private String eventName;

	public StartingEventNotificationMsg(String name) {
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
		// TODO Auto-generated method stub
		return null;
	}

}
