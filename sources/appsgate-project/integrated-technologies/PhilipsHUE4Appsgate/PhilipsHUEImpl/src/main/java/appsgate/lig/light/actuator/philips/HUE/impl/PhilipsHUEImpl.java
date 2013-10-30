package appsgate.lig.light.actuator.philips.HUE.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.colorLight.actuator.messages.ColorLightNotificationMsg;
import appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.proxy.PhilipsHUE.interfaces.PhilipsHUEServices;

/**
 * This class is the AppsGate implementation of ColorLightSpec for Philips HUE technology 
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since May 22, 2013
 * 
 */
public class PhilipsHUEImpl implements CoreColorLightSpec, CoreObjectSpec {
	
	private static long HUE_RED     = 0;
	private static long HUE_BLUE    = 46920;
	private static long HUE_GREEN   = 25500;
	private static long HUE_YELLOW  = 18456;
	private static long HUE_ORANGE  = 12750;
	private static long HUE_PURPLE  = 48765;
	private static long HUE_PINK    = 54332;
	private static long HUE_DEFAULT = 14922;
	
	
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
	private String lightBridgeIP;
	private String reachable;
	
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
		return PhilipsBridge.getLightState(lightBridgeIP, lightBridgeId);
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
		
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, newStatus)) {
			notifyChanges("status", newStatus.toString());
			return true;
		}
		
		return false;
	}

	@Override
	public boolean On() {
		
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "on", true)) {
			notifyChanges("value", "true");
			return true;
		}
		
		return false;
	}

	@Override
	public boolean Off() {		
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "on", false)) {
			notifyChanges("value", "false");
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean toggle() {
		if(getCurrentState()) {
			return Off();
		} else {
			return On();
		}
	}

	@Override
	public boolean setColor(long color) {
		
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "hue", color)) {
			notifyChanges("color", String.valueOf(color));
			return true;
		}
		
		return false;
	}

	@Override
	public boolean setBrightness(long brightness) {
		
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "bri", brightness)) {
			notifyChanges("brightness", String.valueOf(brightness));
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean setSaturation(int saturation) {
		
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "sat", saturation)) {
			notifyChanges("saturation", String.valueOf(saturation));
			return true;
		}
		
		return false;
	}
	
	public boolean setEffect(String effect) {
		
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "effect", effect)) {
			notifyChanges("effect", String.valueOf(effect));
			return true;
		}
		
		return false;
	}
	
	public boolean setAlert(String alert) {
		
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "alert", alert)) {
			notifyChanges("alert", String.valueOf(alert));
			return true;
		}
		
		return false;
	}
	
	public boolean setTransitionTime(long transition) {
		
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "transitiontime", transition)) {
			notifyChanges("transitiontime", String.valueOf(transition));
			return true;
		}
		
		return false;
	}

	@Override
	public boolean setRed() {
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "hue", HUE_RED)) {
			notifyChanges("color", String.valueOf(HUE_RED));
			return true;
		}
		
		return false;
	}

	@Override
	public boolean setBlue() {
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "hue", HUE_BLUE)) {
			notifyChanges("color", String.valueOf(HUE_BLUE));
			return true;
		}
		
		return false;
	}

	@Override
	public boolean setGreen() {
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "hue", HUE_GREEN)) {
			notifyChanges("color", String.valueOf(HUE_GREEN));
			return true;
		}
		
		return false;
	}

	@Override
	public boolean setYellow() {
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "hue", HUE_YELLOW)) {
			notifyChanges("color", String.valueOf(HUE_YELLOW));
			return true;
		}
		
		return false;
	}

	@Override
	public boolean setOrange() {
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "hue", HUE_ORANGE)) {
			notifyChanges("color", String.valueOf(HUE_ORANGE));
			return true;
		}
		
		return false;
	}

	@Override
	public boolean setPurple() {
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "hue", HUE_PURPLE)) {
			notifyChanges("color", String.valueOf(HUE_PURPLE));
			return true;
		}
		
		return false;
		
	}

	@Override
	public boolean setPink() {
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "hue", HUE_PINK)) {
			notifyChanges("color", String.valueOf(HUE_PINK));
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean setDefault() {
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "hue", HUE_DEFAULT)) {
			notifyChanges("color", String.valueOf(HUE_DEFAULT));
			return true;
		}
		
		return false;
	}

	@Override
	public boolean increaseBrightness(int step) {
		int newBri = getLightBrightness()+step;
		if(PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "bri", newBri)) {
			notifyChanges("brightness", String.valueOf((newBri)));
			return true;
		}
		return false;
	}

	@Override
	public boolean decreaseBrightness(int step) {
		int newBri = getLightBrightness()-step;
		if( PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "bri", newBri)) {
			notifyChanges("brightness", String.valueOf((newBri)));
			return true;
		}
		return false;
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
		descr.put("saturation", getLightColorSaturation());
		descr.put("brightness", getLightBrightness());
		
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
	
	public boolean isReachable() {
		return Boolean.valueOf(reachable);
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


