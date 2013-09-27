package appsgate.lig.clock.sensor.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message trigger when the flow rate is changed
 * @author Cédric Gérard
 * @since September 27, 2013
 * @version 1.0.0
 *
 */
public class FlowRateSetNotification implements NotificationMsg {
	
	/**
	 * The source sensor of this notification
	 */
	private CoreObjectSpec source;
	
	/**
	 * The new time flow rate
	 */
	private String flowRate;
	
	/**
	 * Build a new Time flow rate notification
	 * @param source the core object source of this notification
	 * @param flowRate the new time flow rate
	 */
	public FlowRateSetNotification(CoreObjectSpec source, String flowRate) {
		super();
		this.source = source;
		this.flowRate = flowRate;
	}

	@Override
	public CoreObjectSpec getSource() {
		return source;
	}

	@Override
	public String getNewValue() {
		return flowRate;
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		
		JSONObject notif = new JSONObject();
		notif.put("objectId", source.getAbstractObjectId());
		notif.put("varName", "flowRate");
		notif.put("value", flowRate);		
		
		return notif;
	}

}
