package appsgate.lig.light.actuator.philips.HUE.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.colorLight.actuator.messages.ColorLightNotificationMsg;
import appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.light.actuator.philips.HUE.impl.utils.HSBColor;
import appsgate.lig.proxy.PhilipsHUE.interfaces.PhilipsHUEServices;

/**
 * This class is the AppsGate implementation of ColorLightSpec for Philips HUE
 * technology
 *
 * @author Cédric Gérard
 * @version 1.0.0
 * @since May 22, 2013
 *
 */
public class PhilipsHUEImpl extends CoreObjectBehavior implements CoreColorLightSpec, CoreObjectSpec {

    /**
     * Default values
     */
    private static final long HUE_RED = 0;
    private static final long HUE_BLUE = 46920;
    private static final long HUE_GREEN = 25500;
    private static final long HUE_YELLOW = 18456;
    private static final long HUE_ORANGE = 12750;
    private static final long HUE_PURPLE = 48765;
    private static final long HUE_PINK = 54332;
    private static final long HUE_DEFAULT = 14922;

    private static final int SAT_DEFAULT = 254;
    private static final long BRI_DEFAULT = 180;

    /**
     * Static class member uses to log what happened in each instances
     */
    private static final Logger logger = LoggerFactory.getLogger(PhilipsHUEImpl.class);

    private PhilipsHUEServices PhilipsBridge;

    private String actuatorName;
    private String actuatorId;
    private String actuatorType;

    private String pictureId;
    private String userType;

    private String lightBridgeId;
    private String lightBridgeIP;
    private String reachable;

    private String on = "false";
    private String hue = "";
    private String sat = "";
    private String bri = "";
    private String x = "";
    private String y = "";
    private String ct = "";
    private String speed = "";
    private String alert = "";
    private String mode = "";
    private String effect = "";
    private String trans = "";
    private JSONObject state;
    private PhilipsHueBlinkManager blinkController = new PhilipsHueBlinkManager();
    /**
     * The current sensor status.
     *
     * 0 = Off line or out of range 1 = In validation mode (test range for
     * sensor for instance) 2 = In line or connected
     */
    private String status;

    @Override
    public JSONObject getLightStatus() {
        try {

            JSONObject obj = PhilipsBridge.getLightState(lightBridgeIP, lightBridgeId);
            status = "2";

            return obj;
        } catch (Exception exc) {
            logger.error("PhilipsBridge.getLightState(....), not available : " + exc);
            logger.error("Ip: {}, Id: {}", lightBridgeIP, lightBridgeId);
            status = "1";

            return null;
        }
    }

    @Override
    public long getLightColor() {
        Long colorCode = Long.valueOf(-1);
        JSONObject jsonResponse = getLightStatus();
        if (jsonResponse != null) {
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
        if (jsonResponse != null) {
            try {
                state = jsonResponse.getJSONObject("state");
                brightnessCode = state.getInt("bri");
                bri = String.valueOf(brightnessCode);
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
        if (jsonResponse != null) {
            try {
                state = jsonResponse.getJSONObject("state");
                saturationCode = state.getInt("sat");
                sat = String.valueOf(saturationCode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return saturationCode;
    }

    public String getLightEffect() {

        JSONObject jsonResponse = getLightStatus();
        if (jsonResponse != null) {
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
        JSONObject jsonResponse = getLightStatus();
        if (jsonResponse != null) {
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
        long transition = -1;
        JSONObject jsonResponse = getLightStatus();
        if (jsonResponse != null) {
            try {
                state = jsonResponse.getJSONObject("state");
                transition = state.getLong("transitiontime");
                trans = String.valueOf(transition);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return transition;
    }

    @Override
    public boolean getCurrentState() {
        boolean lightState = false;
        JSONObject jsonResponse = getLightStatus();
        if (jsonResponse != null) {
            try {
                state = jsonResponse.getJSONObject("state");
                lightState = state.getBoolean("on");
                //               status = String.valueOf(lightState);
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
        if (jsonResponse != null) {
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

        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, newStatus)) {
            state = newStatus;
            return true;
        }

        return false;
    }

    /**
     * Turns the lamp On with a given transition time
     *
     * @param transitionTime by default its 4 (which means 400ms from the
     * initial state until arrive into goal state)
     * @return
     */
    public boolean on(Integer transitionTime) {
        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "on", true, transitionTime)) {
            on = String.valueOf(true);
            return true;
        }

        return false;
    }

    @Override
    public boolean on() {
        blinkController.stopBlinking();
        return on(null);
    }

    /**
     * Turns the lamp Off with a given transition time
     *
     * @param transitionTime by default its 4 (which means 400ms from the
     * initial state until arrive into goal state)
     * @return
     */
    public boolean off(Integer transitionTime) {
        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "on", false, transitionTime)) {
            on = String.valueOf(false);
            return true;
        }

        return false;
    }

    @Override
    public boolean off() {
        blinkController.stopBlinking();
        return off(null);
    }

    @Override
    public boolean toggle() {
        if (getCurrentState()) {
            return off();
        } else {
            return on();
        }
    }

    @Override
    public boolean setColor(long color) {
        JSONObject oldColor = getJSONColor();

        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "hue", color)) {
            hue = String.valueOf(color);
            notifyChanges("colorChanged", oldColor.toString(), getJSONColor().toString());
            return true;
        }

        return false;
    }

    @Override
    public boolean setColorJson(JSONObject color) {

        JSONObject oldColor = getJSONColor();

        JSONObject state=PhilipsBridge.getLightState(lightBridgeIP, lightBridgeId);
        state.put("bri", String.valueOf(color.get("bri")));
        state.put("hue", String.valueOf(color.get("hue")));
        state.put("sat", String.valueOf(color.get("sat")));

        HSBColor hsb = new HSBColor(color.getInt("hue"),color.getInt("sat"), color.getInt("bri"));
        state.put("rgbcolor", hsb.toRGB().getHTMLColor());

        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, state)) {
            on = String.valueOf(true);
            hue = String.valueOf(color.get("hue"));
            bri = String.valueOf(color.get("bri"));
            sat = String.valueOf(color.get("sat"));
            notifyChanges("colorChanged", oldColor.toString(), state.toString());

            return true;
        }

        return false;

    }

    /**
     * Change the HUe color value and set the brightness and the saturation to
     * default.
     *
     * @param color the HUE color value
     * @return true if the color is set with default saturation and brightness
     * values, false otherwise
     */
    public boolean setSaturatedColor(long color) {

        JSONObject state=PhilipsBridge.getLightState(lightBridgeIP, lightBridgeId);
        state.put("bri", String.valueOf(BRI_DEFAULT));
        state.put("hue", String.valueOf(color));
        state.put("sat", String.valueOf(SAT_DEFAULT));

        return setColorJson(state);


    }

    private JSONObject getJSONColor() {
        JSONObject t =  getLightStatus().getJSONObject("state");
        t.put("rgbcolor", getHTMLColor());
        return t;
    }
    
    @Override
    public boolean setBrightness(long brightness) {
        JSONObject state=PhilipsBridge.getLightState(lightBridgeIP, lightBridgeId).getJSONObject("state");
        state.put("bri", String.valueOf(brightness));
        return setColorJson(state);
    }

    /**
     * Set the default brightness value
     *
     * @return true if the brightness return to default, false otherwise
     */
    public boolean setDefaultBrightness() {
        return setBrightness(BRI_DEFAULT);
    }

    @Override
    public boolean setSaturation(int saturation) {

        JSONObject oldColor = getJSONColor();

        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "sat", saturation)) {
            sat = String.valueOf(saturation);
            notifyChanges("colorChanged", oldColor.toString(), getJSONColor().toString());
            return true;
        }

        return false;
    }

    /**
     * Turn the HUE light saturation to default value
     *
     * @return true if the default saturation is correctly set, false otherwise
     */
    public boolean setDefaultSaturation() {
        return setSaturation(SAT_DEFAULT);
    }

    /**
     * Change the HUE light effect, ie color loop or not
     *
     * @param effect the desired effect conform to the REST call from Philips
     * HUE API (none or colorloop)
     * @return true if the HUE effect is correctly set, false otherwise
     */
    public boolean setEffect(String effect) {

        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "effect", effect)) {
            effect = String.valueOf(effect);
            return true;
        }

        return false;
    }

    /**
     * Turn the HUE light in alert mode
     *
     * @param alert the alert mode: one blink (select), blink for 30 seconds
     * (lselect), or return to previous settings (none)
     * @return true of the alert mode turns on, false otherwise
     */
    public boolean setAlert(String alert) {

        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "alert", alert)) {
            alert = String.valueOf(alert);
            return true;
        }

        return false;
    }

    /**
     * Set the color transition time
     *
     * @return true if the transition time is modified, false otherwise
     */
    public boolean setTransitionTime(long transition) {

        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "transitiontime", transition)) {
            trans = String.valueOf(transition);

            return true;
        }

        return false;
    }

    @Override
    public boolean setRed() {
        return setSaturatedColor(HUE_RED);
    }

    @Override
    public boolean setBlue() {
        return setSaturatedColor(HUE_BLUE);
    }

    @Override
    public boolean setGreen() {
        return setSaturatedColor(HUE_GREEN);
    }

    @Override
    public boolean setYellow() {
        return setSaturatedColor(HUE_YELLOW);
    }

    @Override
    public boolean setOrange() {
        return setSaturatedColor(HUE_ORANGE);
    }

    @Override
    public boolean setPurple() {
        return setSaturatedColor(HUE_PURPLE);
    }

    @Override
    public boolean setPink() {
       return setSaturatedColor(HUE_PINK);
    }

    @Override
    public boolean setWhite() {

        JSONObject JSONAttribute = new JSONObject();
        JSONObject oldColor;
        try {
            oldColor = getLightStatus();
            JSONAttribute.put("bri", 220);
            JSONAttribute.put("sat", 0);
            JSONAttribute.put("on", true);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, JSONAttribute)) {
            on = String.valueOf(true);
            sat = String.valueOf(0);
            bri = String.valueOf(220);

            notifyChanges("colorChanged", oldColor.toString(), getJSONColor().toString());

            return true;
        }

        return false;
    }

    @Override
    public boolean setDefault() {
        return setSaturatedColor(HUE_DEFAULT);
    }

    @Override
    public boolean increaseBrightness(int step) {
        JSONObject oldColor = getJSONColor();
        int newBri = getLightBrightness() + step;
        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "bri", newBri)) {
            bri = String.valueOf(newBri);
            notifyChanges("colorChanged", oldColor.toString(), getJSONColor().toString());
            return true;
        }
        return false;
    }

    @Override
    public boolean decreaseBrightness(int step) {
        JSONObject oldColor = getJSONColor();

        int newBri = getLightBrightness() - step;
        if (PhilipsBridge.setAttribute(lightBridgeIP, lightBridgeId, "bri", newBri)) {
            bri = String.valueOf(newBri);
            notifyChanges("colorChanged", oldColor.toString(), getJSONColor().toString());
            return true;
        }
        return false;
    }

    
    
    @Override
    public boolean blink() {
        return setAlert("select");
    }

    public boolean blink(long seconds, long frequency) {
        blinkController.blink(this, seconds, frequency);
        return true;
    }

    @Override
    public boolean blink30() {
        blink(30l, 1000l);
        return true;
    }

    @Override
    public boolean colorLoop() {
        return setEffect("colorloop");
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
        descr.put("deviceType", actuatorType);
        if (getLightStatus() == null) {
            descr.put("status", status);
            return descr;
        }
        descr.put("status", status);
        descr.put("value", getCurrentState());
        descr.put("color", getLightColor());
        descr.put("saturation", getLightColorSaturation());
        descr.put("brightness", getLightBrightness());
        descr.put("rgbcolor", getHTMLColor());
        //Entry added for configuration GUI

        return descr;
    }

    @Override
    public void setPictureId(String pictureId) {
        notifyChanges("pictureId", this.pictureId, pictureId);
        this.pictureId = pictureId;

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
     *
     * @param newStatus the new status value. its a string the represent a
     * integer value for the status code.
     */
    public void statusChanged(String newStatus) {
        logger.info("The actuator, " + actuatorId + " status changed to " + newStatus);
        notifyChanges("status", status, newStatus);
        status = newStatus;
    }

    /**
     * Called by ApAM when the attribute value changed
     *
     * @param newReachable the new reachable value. its a string the represent a
     * boolean value for the reachable status.
     */
    public void reachableChanged(String newReachable) {
        logger.info("The actuator, " + actuatorId + " reachable changed to " + newReachable);
        notifyChanges("reachable", reachable, newReachable);
        reachable = newReachable;
    }

    /**
     * Called by ApAM when the attribute value changed
     *
     * @param newState the new state value. its a string the represent a boolean
     * value for the state(On/Off) of the light.
     */
    public void stateChanged(String newState) {
        logger.info("The actuator, " + actuatorId + " state changed to " + newState);
        notifyChanges("value", on, newState);
        on = newState;
    }

    /**
     * Called by ApAM when the attribute value changed
     *
     * @param newSpeed the new transition time value. its a string the represent
     * a integer value for the speed status.
     */
    public void speedChanged(String newSpeed) {
        logger.info("The actuator, " + actuatorId + " speed changed to " + newSpeed);
        notifyChanges("speed", speed, newSpeed);
        speed = newSpeed;
    }

    /**
     * Called by ApAM when the attribute value changed
     *
     * @param newAlert the new alert value.
     */
    public void alertChanged(String newAlert) {
        logger.info("The actuator, " + actuatorId + " alert changed to " + newAlert);
        notifyChanges("alert", alert, newAlert);
        alert = newAlert;
    }

    /**
     * Called by ApAM when the attribute value changed
     *
     * @param newMode the new color mode value.
     */
    public void modeChanged(String newMode) {
        logger.info("The actuator, " + actuatorId + " mode changed to " + newMode);
        notifyChanges("mode", mode, newMode);
        mode = newMode;
    }

    /**
     * Called by ApAM when the attribute value changed
     *
     * @param newEffect the new effect value.
     */
    public void effectChanged(String newEffect) {
        logger.info("The actuator, " + actuatorId + " effect changed to " + newEffect);
        notifyChanges("effect", effect, newEffect);
        effect = newEffect;
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        logger.info("New color light actuator detected, " + actuatorId);
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        logger.info("A color light actuator desapeared, " + actuatorId);
    }

    /**
     * This method uses the ApAM message model. Each call produce a
     * TemperatureNotificationMsg object and notifies ApAM that a new message
     * has been released.
     *
     * @return nothing, it just notifies ApAM that a new message has been
     * posted.
     */
    public NotificationMsg notifyChanges(String varName, String oldValue, String newValue) {
        return new ColorLightNotificationMsg(varName, oldValue, newValue, this);
    }

    /**
     * ***********************************
     */
    /**
     * Getter and setter for attributes *
     */
    /**
     * ***********************************
     */
    public boolean getReachable() {
        return Boolean.valueOf(reachable);
    }

    public boolean getState() {
        return Boolean.valueOf(on);
    }

    public int getHue() {
        if (hue.isEmpty()) {
            return 0;
        }
        return Integer.valueOf(hue);
    }

    public int getSat() {
        if (sat.isEmpty()) {
            return 0;
        }
        return Integer.valueOf(sat);
    }

    public int getBri() {
        if (sat.isEmpty()) {
            return 0;
        }
        return Integer.valueOf(bri);
    }

    public float getX() {
        return Float.valueOf(x);
    }

    public float getY() {
        return Float.valueOf(y);
    }

    public int getCt() {
        return Integer.valueOf(ct);
    }

    public long getSpeed() {
        return Long.valueOf(speed);
    }

    public String getAlert() {
        return alert;
    }

    public String getMode() {
        return mode;
    }

    public String getEffect() {
        return effect;
    }

    @Override
    public CORE_TYPE getCoreType() {
        return CORE_TYPE.DEVICE;
    }

    /**
     * @return html value of the color
     */
    protected String getHTMLColor() {
    	
    	HSBColor hsb = new HSBColor((int)this.getLightColor(), this.getSat(), this.getBri());
    	return hsb.toRGB().getHTMLColor();
    	
    	/* Removed to avoid java.awt dependency
        float h = (float) Math.max(0.0, Math.min(this.getLightColor() / 65535.0, 1.0));
        float s = (float) Math.max(0.0, Math.min(this.getSat() / 254.0, 1.0));
        float b = (float) Math.max(0.0, Math.min(this.getBri() / 254.0, 1.0));
        Color c = Color.getHSBColor(h, s, b);
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
        */
    }

    /**
     * Helper function to do some unit tests
     * @param bridge 
     */
    protected void setBridge(PhilipsHUEServices bridge) {
        PhilipsBridge = bridge;
    }
}
