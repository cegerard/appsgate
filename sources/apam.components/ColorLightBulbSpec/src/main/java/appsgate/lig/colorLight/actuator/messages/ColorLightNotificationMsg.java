package appsgate.lig.colorLight.actuator.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;

/**
 * This class is an ApAM message for color light event notification
 * 
 * @author Cédric Gérard
 * version 1.0.0
 * @since June 6, 2013
 */
public class ColorLightNotificationMsg implements NotificationMsg {

	/**
	 * The source sensor of this notification
	 */
	private AbstractObjectSpec source;

	/**
	 * The name of the change variable
	 */
	private String varName;

	/**
	 * The value corresponding to the varName variable
	 */
	private String value;

	/**
	 * Constructor of Color light ApAM message
	 * @param source the abstract object source of this message
	 * @param varName the variable that changed
	 * @param value the new variable value
	 */
	public ColorLightNotificationMsg(AbstractObjectSpec source, String varName, String value) {
		this.source = source;
		this.varName = varName;
		this.value = value;
	}

	@Override
	public AbstractObjectSpec getSource() {
		return source;
	}

	@Override
	public String getNewValue() {
		return value;
	}

	@Override
	public JSONObject JSONize() throws JSONException {
		JSONObject notif = new JSONObject();
		notif.put("objectId", source.getAbstractObjectId());
		notif.put("varName", varName);
		notif.put("value", value.equalsIgnoreCase("true"));
		
		return notif;
	}

}
