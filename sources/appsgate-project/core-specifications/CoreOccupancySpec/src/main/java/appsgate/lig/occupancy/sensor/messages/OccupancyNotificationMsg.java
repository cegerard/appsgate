package appsgate.lig.occupancy.sensor.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for occupancy notifications.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since November 25, 2013
 *
 */
public class OccupancyNotificationMsg implements NotificationMsg {

	/**
	 * The source sensor of this notification
	 */
	private CoreObjectSpec source;
	
	/**
	 * The new occupancy value
	 */
	private boolean newOccupancy;
	
	/**
	 * The name of the change variable
	 */
	private String varName; 
	
	/**
	 * The value corresponding to the varName variable
	 */
	private String value;
	
	/**
	 * Constructor or this ApAM message.
	 * @param source the source instance of this notification
	 * @param newOccupancy the new occupancy state
	 * @param varName the property name that change
	 * @param value the new property value
	 */
	public OccupancyNotificationMsg(CoreObjectSpec source, boolean newOccupancy, String varName, String value) {
		super();
		this.source = source;
		this.newOccupancy = newOccupancy;
		this.varName = varName;
		this.value = value;
	}
	
	/**
	 * Method that returns the value corresponding to this notification 
	 * @return the new occupancy as a boolean
	 */
	public boolean getNotificationValue(){
		return newOccupancy;
	}

	@Override
	public CoreObjectSpec getSource() {
		return source;
	}

	@Override
	public String getNewValue() {
		return String.valueOf(newOccupancy);
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
