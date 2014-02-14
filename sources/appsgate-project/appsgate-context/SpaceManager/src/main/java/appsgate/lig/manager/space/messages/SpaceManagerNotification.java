package appsgate.lig.manager.space.messages;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for space notification
 * 
 * @author Cédric Gérard
 * version 1.0.0
 * @since February 12, 2014
 */
public class SpaceManagerNotification implements NotificationMsg {
	
	/**
	 * The location identifier
	 */
	private String spaceId;
	
	/**
	 * The specified type of this notification
	 */
	private String type;
	
	/**
	 * The new tags list
	 */
	private ArrayList<String> tags;
	
	/**
	 * The new properties list
	 */
	private HashMap<String, String> properties;
	
	/**
	 * New parent parent identifier
	 */
	private String parentId;
	
	/**
	 * New children list
	 */
	private ArrayList<String> childrenIds;
	
	/**
	 * The reason why we trigger a notification
	 */
	private String reason;

	/**
	 * Build a new place notification object
	 * @param spaceId the identifier of the location
	 * @param type the type of the notification (Add, Remove or Update)
	 * @param tags new tags list
	 * @param properties new properties list
	 * @param parentId the new parent identifier
	 * @param childrenIds the new children list
	 */
	public SpaceManagerNotification(String reason, String spaceId, String type, ArrayList<String> tags,
			HashMap<String, String> properties, String parentId, ArrayList<String> childrenIds) {
		super();
		this.spaceId = spaceId;
		this.type = type;
		this.tags = tags;
		this.properties = properties;
		this.parentId = parentId;
		this.childrenIds = childrenIds;
		this.reason = reason;
	}

	@Override
	public CoreObjectSpec getSource() {
		return null;
	}
	
	public String getCoreObjectId() {
		return properties.get("ref");
	}

	@Override
	public String getNewValue() {
		return "Space updated";
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		JSONObject notif 		  = new JSONObject();
		JSONObject content 		  = new JSONObject();
		JSONArray tagArray 		  = new JSONArray();
		JSONArray propertiesArray = new JSONArray();
		JSONArray childrenArray   = new JSONArray();
		
		content.put("id", spaceId);
		content.put("type", type);
		
		if(parentId != null) {
			content.put("parentId", parentId);
		}
		
		if(tags != null) {
			for(String tag : tags) {
				tagArray.put(tag);
			}
			content.put("tags", tagArray);
		}
		
		if(properties != null) {
			for(String key : properties.keySet()) {
				JSONObject prop = new JSONObject();
				prop.put(key, properties.get(key));
				propertiesArray.put(prop);
			}
			content.put("properties", propertiesArray);
		}
		
		if(childrenIds != null) {
			for(String child : childrenIds) {
				childrenArray.put(child);
			}
			content.put("childrenIds", childrenArray);
		}
		
		notif.put(reason, content);

		return notif;
	}

}
