package appsgate.lig.luminosity.sensor.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;

/**
 * This class is an ApAM message for illumination notifications
 * 
 * @author Cédric Gérard
 * version 1.0.0
 * @since February 5, 2013
 */
public class IlluminationNotificationMsg implements NotificationMsg {
	
	/**
	 * The source sensor of this notification
	 */
	private AbstractObjectSpec source;
	
	/**
	 * The new illumination value
	 */
	private int newIllumination;
	
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
	 * @param newIllumination the new illumination value
	 */
	public IlluminationNotificationMsg (int newIllumination, String varName, String value, AbstractObjectSpec source) {
		this.newIllumination = newIllumination;
		this.source = source;
		this.varName = varName;
		this.value = value;
	}
	
	/**
	 * Method that returns the value corresponding to this notification
	 * @return the new temperature as an integer
	 */
	public int getNotificationValue(){
		return newIllumination;
	}

	@Override
	public String getNewValue() {
		return String.valueOf(newIllumination);
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
//		content.put("value", newIllumination);
//		
//		notif.put("updateIllumination", content);
		
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
