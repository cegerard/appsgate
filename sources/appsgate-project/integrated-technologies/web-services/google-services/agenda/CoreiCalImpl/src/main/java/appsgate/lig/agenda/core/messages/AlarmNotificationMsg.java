package appsgate.lig.agenda.core.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;

/**
 * Instance of the class are notifications concerning agenda event alert.
 * @author Cédric Gérard
 * @since May 14, 2013
 * @version 1.0.0
 * 
 * @see NotificationMsg
 *
 */
public class AlarmNotificationMsg implements NotificationMsg {

	/**
	 * Attach event name
	 */
	private String eventName;
	
	/**
	 * Triggering date
	 */
	private String alarmRingDate;
	
	/**
	 * Build a new alarm notification object
	 * @param eventName the name of the event which this alarm is attached
	 * @param alarmRingDate the date of when the alarm has to be triggered 
	 */
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
