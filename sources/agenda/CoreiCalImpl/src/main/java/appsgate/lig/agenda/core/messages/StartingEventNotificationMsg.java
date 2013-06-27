package appsgate.lig.agenda.core.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;

/**
 * instance of this class are starting agenda event notification.
 * @author Cédric Gérard
 * @since May 16, 2013
 * @version 1.0.0
 * 
 * @see NotificationMsg
 */
public class StartingEventNotificationMsg implements NotificationMsg {
	
	/**
	 * The attach event name
	 */
	private String eventName;

	/**
	 * build a new starting event notification object 
	 * @param name the starting event name
	 */
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
		JSONObject obj = new JSONObject();
		obj.put("event", eventName);
		
		return obj;
	}

}
