package appsgate.lig.context.device.name.table.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for object name notification
 * 
 * @author Cédric Gérard
 * version 1.0.0
 * @since June 1, 2013
 */
public class TableNameNotificationMsg implements NotificationMsg {

	/**
	 * The objectId
	 */
	private String objectID;
	
	/**
	 * The user concern for the object name
	 */
	private String userId;
	
	/**
	 * The object name for the user
	 */
	private String objectName;
	
	/**
	 * Constructor for an object name notification
	 * @param objectID the object identifier
	 * @param userId the user identifier
	 * @param objectName the name of this object for this user
	 */
	public TableNameNotificationMsg(String objectID, String userId,
			String objectName) {
		super();
		this.objectID = objectID;
		this.userId = userId;
		this.objectName = objectName;
	}

	@Override
	public CoreObjectSpec getSource() {
		return null;
	}

	@Override
	public String getNewValue() {
		return objectName;
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		JSONObject notif = new JSONObject();
		
		notif.put("objectId", objectID);
		notif.put("userId", userId);
		notif.put("varName", "name");
		notif.put("value",objectName);
		
		return notif;
	}

}
