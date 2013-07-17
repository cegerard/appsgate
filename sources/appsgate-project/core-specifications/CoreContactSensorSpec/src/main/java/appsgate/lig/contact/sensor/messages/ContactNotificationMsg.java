package appsgate.lig.contact.sensor.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for switch event notification
 * 
 * @author Cédric Gérard
 * version 1.0.0
 * @since February 5, 2013
 */
public class ContactNotificationMsg implements NotificationMsg {
	
	/**
	 * The source sensor of this notification
	 */
	private CoreObjectSpec source;
	
	/**
	 * The new sensor status
	 */
	private boolean isContact;
	
	/**
	 * The name of the change variable
	 */
	private String varName; 
	
	/**
	 * The value corresponding to the varName variable
	 */
	private String value;

	/**
	 * Constructor for this ApAM message
	 * @param isContact the new contact sensor status
	 */
	public ContactNotificationMsg(boolean isContact, String varName, String value, CoreObjectSpec source) {
		super();
		this.isContact = isContact;
		this.source = source;
		this.varName = varName;
		this.value = value;
	}
	
	/**
	 * Method that returns the value corresponding to this notification 
	 * @return the new contact sensor status
	 */
	public boolean getNotificationValue(){
		return isContact;
	}

	@Override
	public String getNewValue() {
		return String.valueOf(isContact);
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		JSONObject notif = new JSONObject();
//		JSONObject content = new JSONObject();
//		
//		content.put("id", source.getAbstractObjectId());
//		content.put("name", source.getUserObjectName());
//		content.put("locationId", source.getLocationId());
//		content.put("status", source.getObjectStatus());
//		content.put("contact", isContact);
//		
//		notif.put("updateContact", content);
		
		notif.put("objectId", source.getAbstractObjectId());
		notif.put("varName", varName);
		notif.put("value", value);
		
		return notif;
	}

	@Override
	public CoreObjectSpec getSource() {
		return source;
	}

}
