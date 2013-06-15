package appsgate.lig.light.actuator.philips.HUE.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.colorLight.actuator.messages.ColorLightNotificationMsg;
import appsgate.lig.colorLight.actuator.spec.ColorLightSpec;
import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;
import appsgate.lig.proxy.PhilipsHUE.interfaces.PhilipsHUEServices;

/**
 * This class is the AppsGate implementation of ColorLightSpec for Philips HUE technology 
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since May 22, 2013
 * 
 */
public class PhilipsHUEImpl implements ColorLightSpec, AbstractObjectSpec {
	
	private static long HUE_RED     = 0;
	private static long HUE_BLUE    = 46920;
	private static long HUE_GREEN   = 25500;
	private static long HUE_YELLOW  = 18456;
	private static long HUE_ORANGE  = 12750;
	private static long HUE_PURPLE  = 48765;
	private static long HUE_PINK    = 54332;
	
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(PhilipsHUEImpl.class);
	
	private PhilipsHUEServices PhilipsBridge;
	
	private String actuatorName;
	private String actuatorId;
	private String actuatorType;
	
	private String pictureId;
	private String userType;
	
	private String lightBridgeId;
	
	/**
	 * The current sensor status.
	 * 
	 * 0 = Off line or out of range
	 * 1 = In validation mode (test range for sensor for instance)
	 * 2 = In line or connected
	 */
	private String status;
	
	@Override
	public JSONObject getLightStatus() {
		return PhilipsBridge.getLightState(lightBridgeId);
	}

	@Override
	public long getLightColor() {
		Long colorCode = Long.valueOf(-1);
		JSONObject jsonResponse = getLightStatus();
		if(jsonResponse != null) {
			JSONObject state;
			try {
				state = jsonResponse.getJSONObject("state");
				colorCode = state.getLong("hue");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return colorCode;
	}

	@Override
	public int getLightBrightness() {
		int brightnessCode = -1;
		JSONObject jsonResponse = getLightStatus();
		if(jsonResponse != null) {
			JSONObject state;
			try {
				state = jsonResponse.getJSONObject("state");
				brightnessCode = state.getInt("bri");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return brightnessCode;
	}
	
	@Override
	public int getLightColorSaturation() {
		int saturationCode = -1;
		JSONObject jsonResponse = getLightStatus();
		if(jsonResponse != null) {
			JSONObject state;
			try {
				state = jsonResponse.getJSONObject("state");
				saturationCode = state.getInt("sat");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return saturationCode;
	}
	
	public String getLightEffect() {
		String effect = "";
		JSONObject jsonResponse = getLightStatus();
		if(jsonResponse != null) {
			JSONObject state;
			try {
				state = jsonResponse.getJSONObject("state");
				effect = state.getString("effect");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return effect;
	}
	
	public String getLightAlert() {
		String alert = "";
		JSONObject jsonResponse = getLightStatus();
		if(jsonResponse != null) {
			JSONObject state;
			try {
				state = jsonResponse.getJSONObject("state");
				alert = state.getString("alert");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return alert;
	}
	
	public long getTransitionTime() {
		long transistion = -1;
		JSONObject jsonResponse = getLightStatus();
		if(jsonResponse != null) {
			JSONObject state;
			try {
				state = jsonResponse.getJSONObject("state");
				transistion = state.getLong("transitiontime");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return transistion;
	}

	@Override
	public boolean getCurrentState() {
		boolean lightState = false;
		JSONObject jsonResponse = getLightStatus();
		if(jsonResponse != null) {
			JSONObject state;
			try {
				state = jsonResponse.getJSONObject("state");
				lightState = state.getBoolean("on");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return lightState;
	}

	@Override
	public JSONObject getManufacturerDetails() {
		JSONObject manufacturerState = new JSONObject();
		JSONObject jsonResponse = getLightStatus();
		if(jsonResponse != null) {
			try {
				manufacturerState.put("type", jsonResponse.get("type"));
				manufacturerState.put("name", jsonResponse.get("name"));
				manufacturerState.put("model", jsonResponse.get("modelid"));
				manufacturerState.put("version", jsonResponse.get("swversion"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return manufacturerState;
	}

	@Override
	public boolean setStatus(JSONObject newStatus) {
		return PhilipsBridge.setAttribute(lightBridgeId, newStatus);
	}

	@Override
	public boolean On() {
		return PhilipsBridge.setAttribute(lightBridgeId, "on", true);
	}

	@Override
	public boolean Off() {
		return PhilipsBridge.setAttribute(lightBridgeId, "on", false);
	}

	@Override
	public boolean setColor(long color) {
		return PhilipsBridge.setAttribute(lightBridgeId, "hue", color);
	}

	@Override
	public boolean setBrightness(long brightness) {
		return PhilipsBridge.setAttribute(lightBridgeId, "bri", brightness);
	}
	
	@Override
	public boolean setSaturation(int saturation) {
		return PhilipsBridge.setAttribute(lightBridgeId, "sat", saturation);
	}
	
	public boolean setEffect(String effect) {
		return PhilipsBridge.setAttribute(lightBridgeId, "effect", effect);
	}
	
	public boolean setAlert(String alert) {
		return PhilipsBridge.setAttribute(lightBridgeId, "alert", alert);
	}
	
	public boolean setTransitionTime(long transition) {
		return PhilipsBridge.setAttribute(lightBridgeId, "transitiontime", transition);
	}

	@Override
	public boolean setRed() {
		return PhilipsBridge.setAttribute(lightBridgeId, "hue", HUE_RED);
	}

	@Override
	public boolean setBlue() {
		return PhilipsBridge.setAttribute(lightBridgeId, "hue", HUE_BLUE);
	}

	@Override
	public boolean setGreen() {
		return PhilipsBridge.setAttribute(lightBridgeId, "hue", HUE_GREEN);
	}

	@Override
	public boolean setYellow() {
		return PhilipsBridge.setAttribute(lightBridgeId, "hue", HUE_YELLOW);
	}

	@Override
	public boolean setOrange() {
		return PhilipsBridge.setAttribute(lightBridgeId, "hue", HUE_ORANGE);
	}

	@Override
	public boolean setPurple() {
		return PhilipsBridge.setAttribute(lightBridgeId, "hue", HUE_PURPLE);
	}

	@Override
	public boolean setPink() {
		return PhilipsBridge.setAttribute(lightBridgeId, "hue", HUE_PINK);
	}

	@Override
	public boolean increaseBrightness(int step) {
		return PhilipsBridge.setAttribute(lightBridgeId, "bri", getLightBrightness()+step);
	}

	@Override
	public boolean decreaseBrightness(int step) {
		return PhilipsBridge.setAttribute(lightBridgeId, "bri", getLightBrightness()-step);
	}
	
	public String getSensorName() {
		return actuatorName;
	}

	public void setSensorName(String actuatorName) {
		this.actuatorName = actuatorName;
	}
	
	@Override
	public String getAbstractObjectId() {
		return actuatorId;
	}

	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public int getObjectStatus() {
		return Integer.valueOf(status);
	}

	@Override
	public String getPictureId() {
		return pictureId;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
	
		JSONObject descr = new JSONObject();
		descr.put("id", actuatorId);
		descr.put("type", userType); // 7 for color light
		descr.put("status", status);
		descr.put("value", getCurrentState());
		descr.put("color", getLightColor());
		
		return descr;
	}

	@Override
	public void setPictureId(String pictureId) {
		this.pictureId = pictureId;
		notifyChanges("pictureId", pictureId);
	}
	
	public String getActuatorType() {
		return actuatorType;
	}

	public void setActuatorType(String actuatorType) {
		this.actuatorType = actuatorType;
	}

	/**
	 * Called by ApAM when the status value changed
	 * @param newStatus the new status value.
	 * its a string the represent a integer value for the status code.
	 */
	public void statusChanged(String newStatus) {
		logger.info("The actuator, "+ actuatorId+" status changed to "+newStatus);
		notifyChanges("status", newStatus);
	}
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New color light actuator detected, "+actuatorId);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("A color light actuator desapeared, "+actuatorId);
	}

	/**
	 * This method uses the ApAM message model. Each call produce a
	 * TemperatureNotificationMsg object and notifies ApAM that a new message has
	 * been released.
	 * 
	 * @return nothing, it just notifies ApAM that a new message has been
	 *         posted.
	 */
	public NotificationMsg notifyChanges(String varName, String value) {
		return new ColorLightNotificationMsg(this, varName, value);
	}

}


