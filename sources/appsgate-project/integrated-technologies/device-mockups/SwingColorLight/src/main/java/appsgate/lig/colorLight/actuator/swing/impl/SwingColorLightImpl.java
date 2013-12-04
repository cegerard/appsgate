package appsgate.lig.colorLight.actuator.swing.impl;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.colorLight.actuator.messages.ColorLightNotificationMsg;
import appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

public class SwingColorLightImpl implements CoreObjectSpec, CoreColorLightSpec {

    private static long HUE_RED = 0;
    private static long HUE_BLUE = 46920;
    private static long HUE_GREEN = 25500;
    private static long HUE_YELLOW = 18456;
    private static long HUE_ORANGE = 8900;
    private static long HUE_PURPLE = 48765;
    private static long HUE_PINK = 54332;
    private static long HUE_DEFAULT = 14922;

    /**
     * Static class member uses to log what happened in each instances
     */
    private static Logger logger = LoggerFactory
	    .getLogger(SwingColorLightImpl.class);


    private JLabel lightStatus;
    private JFrame frameLight;

    /**
     * On/Off state of the light. On=true, Off=false
     */
    private boolean isOn;

    /**
     * Brightness of the light. This is a scale from the minimum brightness the
     * light is capable of, 0, to the maximum capable brightness, 255. Note a
     * brightness of 0 is not off.
     */
    private int brightness;

    /**
     * Saturation of the light. 255 is the most saturated (colored) and 0 is the
     * least saturated (white).
     */
    private int saturation;

    /**
     * Hue of the light. This is a wrapping value between 0 and 65535. Both 0
     * and 65535 are red, 25500 is green and 46920 is blue.
     */
    private long color;

    
    private String appsgateDeviceName;
    private String appsgateObjectId;
    private String appsgateSensorType;
    private String appsgateUserType;
    private String appsgateStatus;
    private String appsgatePictureId;

    private void initAppsgateFields() {
	    appsgatePictureId=null;
	    appsgateDeviceName="Swnig Color Lamp";
	    appsgateUserType="7";
	    appsgateStatus="2";
	    appsgateObjectId=appsgateUserType+String.valueOf(this.hashCode());
	    appsgateSensorType="PHILIPS_HUE_LIGHT";
	    
}
    
    @Override
    public JSONObject getDescription() throws JSONException {
	JSONObject descr = new JSONObject();
	descr.put("id", appsgateObjectId);
	descr.put("type", appsgateUserType); // 7 for color light
	descr.put("status", appsgateStatus);
	descr.put("value", getCurrentState());
	descr.put("color", getLightColor());
	descr.put("saturation", getLightColorSaturation());
	descr.put("brightness", getLightBrightness());

	return descr;
    }

    public String getSensorName() {
	return appsgateDeviceName;
    }

    public void setSensorName(String sensorName) {
	this.appsgateDeviceName = sensorName;
    }

    public String getSensorId() {
	return appsgateObjectId;
    }

    @Override
    public String getAbstractObjectId() {
	return getSensorId();
    }

    public String getSensorType() {
	return appsgateSensorType;
    }

    @Override
    public String getUserType() {
	return appsgateUserType;
    }

    @Override
    public int getObjectStatus() {
	return Integer.valueOf(appsgateStatus);
    }

    @Override
    public String getPictureId() {
	return appsgatePictureId;
    }

    @Override
    public void setPictureId(String pictureId) {
	this.appsgatePictureId = pictureId;
	notifyChanges("pictureId", pictureId);
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void show() {
	initAppsgateFields();
	isOn = false;
	brightness = 0;
	saturation = 0;
	color = 0;

	logger.info("New swing Light added, " + appsgateObjectId);
	frameLight = new JFrame("AppsGate Swing ColorLight");
	lightStatus = new JLabel();
	lightStatus.setPreferredSize(new Dimension(260, 60));
	refreshLight();
	frameLight.add(lightStatus);
	frameLight.pack();
	frameLight.setVisible(true);
    }

    private void refreshLight() {
	lightStatus.setText("Light " + appsgateObjectId);

	lightStatus.setOpaque(true);
	if (isOn) {
	    logger.debug("Applying following color : "+(float)color/65535
		    +", saturation : "+(float)saturation/255
		    +", brightness : "+(float)brightness/255);
	    lightStatus.setBackground(Color.getHSBColor((float)color/65535, (float)saturation/255, (float)brightness/255));
	} else
	    lightStatus.setBackground(Color.GRAY);
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void hide() {
	logger.info("Swing Light removed, " + appsgateObjectId);
	frameLight.dispose();
	frameLight = null;
    }

    /**
     * This method uses the ApAM message model. Each call produce a
     * SwitchNotificationMsg object and notifies ApAM that a new message has
     * been released.
     * 
     * @return nothing, it just notifies ApAM that a new message has been
     *         posted.
     */
    public NotificationMsg notifyChanges(String varName, String value) {
	return new ColorLightNotificationMsg(this, varName, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#getLightStatus()
     */
    @Override
    public JSONObject getLightStatus() {
	JSONObject jsonResponse = new JSONObject();
	try {
	    jsonResponse.put("type", "Swing Living Colors");
	    jsonResponse.put("name", "SLC 1");
	    jsonResponse.put("model", "SLC0001");
	    jsonResponse.put("version", "1.0.0");
	} catch (JSONException e) {
	    e.printStackTrace();
	}

	return jsonResponse;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#getLightColor()
     */
    @Override
    public long getLightColor() {
	return color;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#getLightBrightness
     * ()
     */
    @Override
    public int getLightBrightness() {
	return brightness;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#
     * getLightColorSaturation()
     */
    @Override
    public int getLightColorSaturation() {
	return saturation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#getCurrentState
     * ()
     */
    @Override
    public boolean getCurrentState() {
	return isOn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#
     * getManufacturerDetails()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#setStatus(org
     * .json.JSONObject)
     */
    @Override
    public boolean setStatus(JSONObject newStatus) {
	notifyChanges("status", newStatus.toString());
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#On()
     */
    @Override
    public boolean On() {
	isOn = true;
	notifyChanges("value", "true");
	refreshLight();
	return true;

    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#Off()
     */
    @Override
    public boolean Off() {
	isOn = false;
	notifyChanges("value", "false");
	refreshLight();
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#setColor(long)
     */
    @Override
    public boolean setColor(long color) {
	if (color >= 0 && color <= 65535) {
	    this.color = color;
	    notifyChanges("color", String.valueOf(color));
	    refreshLight();
	    return true;
	} else
	    return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#setBrightness
     * (long)
     */
    @Override
    public boolean setBrightness(long brightness) {
	if (brightness >= 0 && brightness <= 255) {
	    this.brightness = (int) brightness;
	    notifyChanges("brightness", String.valueOf(brightness));
	    refreshLight();
	    return true;
	} else
	    return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#setSaturation
     * (int)
     */
    @Override
    public boolean setSaturation(int saturation) {
	if (saturation >= 0 && saturation <= 255) {
	    this.saturation = (int) saturation;
	    notifyChanges("saturation", String.valueOf(saturation));
	    refreshLight();
	    return true;
	} else
	    return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#setRed()
     */
    @Override
    public boolean setRed() {
	color = HUE_RED;
	notifyChanges("color", String.valueOf(color));
	refreshLight();
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#setBlue()
     */
    @Override
    public boolean setBlue() {
	color = HUE_BLUE;
	notifyChanges("color", String.valueOf(color));
	refreshLight();
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#setGreen()
     */
    @Override
    public boolean setGreen() {
	color = HUE_GREEN;
	notifyChanges("color", String.valueOf(color));
	refreshLight();
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#setYellow()
     */
    @Override
    public boolean setYellow() {
	color = HUE_YELLOW;
	notifyChanges("color", String.valueOf(color));
	refreshLight();
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#setOrange()
     */
    @Override
    public boolean setOrange() {
	color = HUE_ORANGE;
	notifyChanges("color", String.valueOf(color));
	refreshLight();
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#setPurple()
     */
    @Override
    public boolean setPurple() {
	color = HUE_PURPLE;
	notifyChanges("color", String.valueOf(color));
	refreshLight();
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#setPink()
     */
    @Override
    public boolean setPink() {
	color = HUE_PINK;
	notifyChanges("color", String.valueOf(color));
	refreshLight();
	return true;
    }
    
	@Override
	public boolean setDefault() {
		color = HUE_DEFAULT;
		notifyChanges("color", String.valueOf(color));
		refreshLight();
		return true;
	}


    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#increaseBrightness
     * (int)
     */
    @Override
    public boolean increaseBrightness(int step) {
	if ((brightness + step) <= 255) {
	    this.brightness += step;
	    notifyChanges("brightness", String.valueOf(brightness));
	    refreshLight();
	    return true;
	} else {
	    brightness = 255;
	    notifyChanges("brightness", String.valueOf(brightness));
	    refreshLight();
	    return false;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#decreaseBrightness
     * (int)
     */
    @Override
    public boolean decreaseBrightness(int step) {
	if ((brightness - step) >= 0) {
	    this.brightness -= step;
	    notifyChanges("brightness", String.valueOf(brightness));
	    refreshLight();
	    return true;
	} else {
	    brightness = 0;
	    notifyChanges("brightness", String.valueOf(brightness));
	    refreshLight();
	    return false;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec#toggle()
     */
    @Override
    public boolean toggle() {
	if (isOn)
	    return Off();
	else
	    return On();
    }

	@Override
	public boolean blink() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean blink30() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean colorLoop() {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean setWhite() {
		color = HUE_DEFAULT;
		notifyChanges("color", String.valueOf(color));
		refreshLight();
		return true;
    }
}
