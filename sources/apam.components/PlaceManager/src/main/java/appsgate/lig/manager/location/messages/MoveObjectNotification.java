package appsgate.lig.manager.location.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;

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
	private AbstractObjectSpec object;
	
	/**
	 * Build a new notification message from place manager
	 * 
	 * @param oldLocationId
	 * @param newLocationId
	 * @param object
	 */
	public MoveObjectNotification(String oldLocationId, String newLocationId,
			AbstractObjectSpec object) {
		super();
		this.oldLocationId = oldLocationId;
		this.newLocationId = newLocationId;
		this.object = object;
	}

	@Override
	public AbstractObjectSpec getSource() {
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
		content.put("deviceId", object.getAbstractObjectId());
		
		notif.put("moveDevice", content);
		
		return notif;
	}

}
