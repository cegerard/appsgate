package appsgate.lig.calendar.core.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * instance of this class are ending agenda event notification.
 * @author Cédric Gérard
 * @since May 16, 2013
 * @version 1.0.0
 * 
 * @see NotificationMsg
 */
public class EndingEventNotificationMsg implements NotificationMsg {

	/**
	 * attach event name
	 */
	private String eventName;
	
	/**
	 * build a new ending event notification object 
	 * @param name the ending event name
	 */
	public EndingEventNotificationMsg(String name) {
		eventName = name;
	}

	@Override
	public CoreObjectSpec getSource() {
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
