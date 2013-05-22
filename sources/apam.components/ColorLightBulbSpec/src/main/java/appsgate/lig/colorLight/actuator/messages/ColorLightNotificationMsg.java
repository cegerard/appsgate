package appsgate.lig.colorLight.actuator.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;
//TODO make notification for color light
public class ColorLightNotificationMsg implements NotificationMsg {

	@Override
	public AbstractObjectSpec getSource() {
		return null;
	}

	@Override
	public String getNewValue() {
		return null;
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		return null;
	}

}
