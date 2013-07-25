package appsgate.lig.manager.place.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for move object notification
 * 
 * @author Cédric Gérard
 * version 1.0.0
 * @since February 26, 2013
 */
public class MoveObjectNotification implements NotificationMsg {
	
	/**
	 * The former location identifier
	 */
	private String oldLocationId;
	
	/**
	 * The new location identifier
	 */
	private String newLocationId;
	
	/**
	 * The abstract object it moved
	 */
	private String objId;
	
	/**
	 * Build a new notification message from place manager
	 * 
	 * @param oldLocationId
	 * @param newLocationId
	 * @param object
	 */
	public MoveObjectNotification(String oldLocationId, String newLocationId,
			String objId) {
		super();
		this.oldLocationId = oldLocationId;
		this.newLocationId = newLocationId;
		this.objId = objId;
	}

	@Override
	public CoreObjectSpec getSource() {
		return null;
	}

	@Override
	public String getNewValue() {
		return "Place manager send : MoveDevice";
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		JSONObject notif = new JSONObject();
		JSONObject content = new JSONObject();
		
		content.put("srcLocationId", oldLocationId);
		content.put("destLocationId", newLocationId);
		content.put("deviceId", objId);
		
		notif.put("moveDevice", content);
		
		return notif;
	}

}
