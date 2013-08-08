package appsgate.lig.clock.sensor.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message thrown when current Time has been
 * set for the clock (TimeZone change, Delay reset, specific time setting, ...)
 */

public class ClockSetNotificationMsg implements NotificationMsg{
    
	/**
	 * The source sensor of this notification
	 */
	private CoreObjectSpec source;
	
	private String currentDate;

	/**
	 * Constructor for this ApAM message.
	 * @param source the CoreObjectSource for this message 
	 * @param currentDate a String representation of the current date and time 
	 */
	public ClockSetNotificationMsg (CoreObjectSpec source, String currentDate) {
		this.source = source;
		this.currentDate = currentDate;
	}
	
	@Override
	public String getNewValue() {
		return currentDate;
	}

	@Override
	public JSONObject JSONize() throws JSONException{
		
		JSONObject notif = new JSONObject();
		notif.put("objectId", source.getAbstractObjectId());
		notif.put("varName", "ClockSet");
		notif.put("value", currentDate);		
		
		return notif;
	}

	@Override
	public CoreObjectSpec getSource() {
		return source;
	}

}
