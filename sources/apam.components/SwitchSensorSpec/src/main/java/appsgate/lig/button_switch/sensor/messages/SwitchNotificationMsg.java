package appsgate.lig.button_switch.sensor.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;

/**
 * This class is an ApAM message for switch event notifications.
 * 
 * @author Cédric Gérard
 * version 1.0.0
 * @since February 5, 2013
 */
public class SwitchNotificationMsg implements NotificationMsg{
	
	/**
	 * The source sensor of this notification
	 */
	private AbstractObjectSpec source;
	
	/**
	 * The number of the pressed switch
	 */
	private int switchNumber;	
	
	/**
	 * The button state On/Off = Up/Down
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
	 * Constructor for this ApAM message
	 * @param switchNumber the button pressed (for multiple buttons switch)
	 * @param isOn the button state
	 */
	public SwitchNotificationMsg (int switchNumber, boolean isOn, String varName, String value, AbstractObjectSpec source){
		this.switchNumber = switchNumber;
		this.isOn = isOn;
		this.source = source;
		this.varName = varName;
		this.value = value;
	}
	
	/**
	 * Method that returns the value corresponding to this notification 
	 * @return a descriptive string of the button state.
	 */
	public String getNotificationValue(){
		return switchNumber+" "+isOn;
	}

	/**
	 * Get the switch number
	 * @return switchNumber, the number of the pressed button
	 */
	public int getSwitchNumber() {
		return switchNumber;
	}

	/**
	 * Get the button state
	 * @return isOn, the state of the button
	 */
	public boolean isOn() {
		return isOn;
	}

	@Override
	public String getNewValue() {
		return new Integer(switchNumber)+"/"+String.valueOf(isOn);
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		
		JSONObject notif = new JSONObject();
//		JSONObject content = new JSONObject();
//		content.put("id", source.getAbstractObjectId());
//		content.put("name", source.getUserObjectName());
//		content.put("locationId", String.valueOf(source.getLocationId()));
//		content.put("status", source.getObjectStatus());
//		content.put("switchNumber", switchNumber);
//		if(isOn){
//			content.put("buttonStatus", 1);
//		}else{
//			content.put("buttonStatus", 0);
//		}
//		notif.put("updateSwitch", content);
		
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
