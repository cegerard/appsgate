package appsgate.lig.keycard.sensor.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;

/**
 * This class is an ApAM message for key card event notifications.
 * 
 @author Cédric Gérard
 * version 1.0.0
 * @since February 5, 2013
 */
public class KeyCardNotificationMsg implements NotificationMsg{
	
	/**
	 * The source sensor of this notification
	 */
	private AbstractObjectSpec source;
	
	/**
	 * The new key card status.
	 * True if a card has been inserted and false otherwise
	 */
	private boolean isCardInserted;
	
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
	 * @param isCardInserted, the new key card status
	 */
	public KeyCardNotificationMsg(boolean isCardInserted, String varName, String value, AbstractObjectSpec source) {
		super();
		this.isCardInserted = isCardInserted;
		this.source = source;
		this.varName = varName;
		this.value = value;
	}
	
	/**
	 * Method that returns the value corresponding to this notification 
	 * @return  the new key card status
	 */
	public boolean getNotificationValue(){
		return isCardInserted;
	}

	@Override
	public String getNewValue() {
		return String.valueOf(isCardInserted);
	}

	@Override
	public JSONObject JSONize() throws JSONException{
		
		JSONObject notif = new JSONObject();
//		JSONObject content = new JSONObject();
//		
//		content.put("id", source.getAbstractObjectId());
//		content.put("name", source.getUserObjectName());
//		content.put("locationId", source.getLocationId());
//		content.put("status", source.getObjectStatus());
//		content.put("inserted", isCardInserted);
//		
//		notif.put("updateKeyCard", content);
		
		notif.put("objectId", source.getAbstractObjectId());
		notif.put("varName", varName);
		notif.put("value", value);
		
		return notif;
	}

	@Override
	public AbstractObjectSpec getSource() {
		return source;
	}
	
}
