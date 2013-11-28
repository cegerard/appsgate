package appsgate.lig.co2.sensor.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for Co2 concentration notifications
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since November 28, 2013
 */
public class Co2NotificationMsg implements NotificationMsg {
	
	/**
	 * The source sensor of this notification
	 */
	private CoreObjectSpec source;
	
	/**
	 * The new CO2 value
	 */
	private float newCO2Concentration;
	
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
	public Co2NotificationMsg (float newCo2Concentration, String varName, String value, CoreObjectSpec source) {
		this.newCO2Concentration = newCo2Concentration;
		this.source = source;
		this.varName = varName;
		this.value = value;
	}

	/**
	 * Method that returns the value corresponding to this notification
	 * @return the new Co2 concentration as a float
	 */
	public float getNotificationValue(){
		return newCO2Concentration;
	}
	
	@Override
	public CoreObjectSpec getSource() {
		return source;
	}

	@Override
	public String getNewValue() {
		return String.valueOf(newCO2Concentration);
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		JSONObject notif = new JSONObject();
		
		notif.put("objectId", source.getAbstractObjectId());
		notif.put("varName", varName);
		notif.put("value", value);
		
		return notif;
	}

}
