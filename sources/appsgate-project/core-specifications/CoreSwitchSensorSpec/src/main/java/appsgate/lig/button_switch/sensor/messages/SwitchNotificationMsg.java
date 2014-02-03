package appsgate.lig.button_switch.sensor.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

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
	private CoreObjectSpec source;
	
	/**
	 * The number of the pressed switch
	 */
	private int switchNumber;	
	
	/**
	 * The button state On/Off = Up/Down
	 */
	private String state;
	
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
	 * @param state the button state
	 */
	public SwitchNotificationMsg (int switchNumber, String state, String varName, String value, CoreObjectSpec source){
		this.switchNumber = switchNumber;
		this.state = state;
		this.source = source;
		this.varName = varName;
		this.value = value;
	}
	
	/**
	 * Method that returns the value corresponding to this notification 
	 * @return a descriptive string of the button state.
	 */
	public String getNotificationValue(){
		return switchNumber+" "+state;
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
		return (state.contentEquals("true"));
	}
	
	/**
	 * Get the current switch state
	 * @return the state as a string. (true, false, none)
	 */
	public String getState() {
		return state;
	}

	@Override
	public String getNewValue() {
		return new Integer(switchNumber)+"/"+String.valueOf(state);
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
	public CoreObjectSpec getSource() {
		return source;
	}

}
