package appsgate.lig.on_off.actuator.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;

/**
 * This class is an ApAM message for temperature notifications.
 * 
 * @author Cédric Gérard
 * version 1.0.0
 * @since February 4, 2013
 *
 */
public class OnOffActuatorNotificationMsg implements NotificationMsg{
	
	/**
	 * The source sensor of this notification
	 */
	private AbstractObjectSpec source;
	
	/**
	 * The current virtual state
	 */
	private boolean isOn;
	
	/**
	 * The name of the change variable
	 */
	private String varName; 
	
	/**
	 * The value corresponding to the varName variable
	 */
	private String value;
	
	/**
	 * Constructor for this ApAM message.
	 * @param newTemperature, the new temperature value 
	 */
	public OnOffActuatorNotificationMsg (String isOn, String varName, String value, AbstractObjectSpec source) {
		this.isOn = Boolean.valueOf(isOn);
		this.varName = varName;
		this.value = value;
		this.source = source;
	}
	
	/**
	 * Method that returns the value corresponding to this notification 
	 * @return the new temperature as a float
	 */
	public boolean getNotificationValue(){
		return isOn;
	}

	@Override
	public String getNewValue() {
		return String.valueOf(isOn);
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		
		JSONObject notif = new JSONObject();
		
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
