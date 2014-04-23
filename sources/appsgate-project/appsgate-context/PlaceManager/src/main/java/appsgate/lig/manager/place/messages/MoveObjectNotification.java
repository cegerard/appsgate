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
	 * The type of the object that moved
	 */
	private int moveType;
	
	/**
	 * Build a new notification message from place manager
	 * 
	 * @param oldLocationId
	 * @param newLocationId
	 * @param object
	 */
	public MoveObjectNotification(String oldLocationId, String newLocationId,
			String objId, int moveType) {
		super();
		this.oldLocationId = oldLocationId;
		this.newLocationId = newLocationId;
		this.objId = objId;
		this.moveType = moveType;
	}

	@Override
	public CoreObjectSpec getSource() {
		return null;
	}
	
	@Override
	public String getVarName() {
		return String.valueOf(moveType);
	}

	@Override
	public String getNewValue() {
		if(moveType == 0) {
			return "Place manager send : MoveDevice";
		}else {
			return "Place manager send : MoveService";
		}
	}

	@Override
	public JSONObject JSONize() {
		JSONObject notif = new JSONObject();
		JSONObject content = new JSONObject();
		
		try {
			content.put("srcLocationId", oldLocationId);
			content.put("destLocationId", newLocationId);
			if(moveType == 0) {
				content.put("deviceId", objId);
				notif.put("moveDevice", content);
			}else if(moveType == 1){
				content.put("serviceId", objId);
				notif.put("moveService", content);
			}
		} catch (JSONException e) {e.printStackTrace();}
		
		return notif;
	}

}
