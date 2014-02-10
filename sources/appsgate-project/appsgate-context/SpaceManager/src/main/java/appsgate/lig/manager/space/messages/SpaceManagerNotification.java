package appsgate.lig.manager.space.messages;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for place notification
 * 
 * @author Cédric Gérard
 * version 1.0.0
 * @since February 26, 2013
 */
public class SpaceManagerNotification implements NotificationMsg {
	
	/**
	 * The location identifier
	 */
	private String locationId;
	
	/**
	 * The location name
	 */
	private String name;
	
	/**
	 * The specified type of this notification
	 */
	private String type;

	/**
	 * Build a new place notification object
	 * @param locationId the identifier of the location
	 * @param name the name of the location
	 * @param type the type of the notification (Add, Remove or Update)
	 */
	public SpaceManagerNotification(String locationId, String name, String type) {
		super();
		this.locationId = locationId;
		this.name = name;
		this.type = type;
	}

	@Override
	public CoreObjectSpec getSource() {
		return null;
	}

	@Override
	public String getNewValue() {
		return "Place manager send : newLocation";
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		JSONObject notif = new JSONObject();
		JSONObject content = new JSONObject();
		
		content.put("id", locationId);
		content.put("name", name);
		content.put("devices", new JSONArray());
		
		notif.put(type, content);

		return notif;
	}

}
