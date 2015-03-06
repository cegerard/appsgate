package appsgate.lig.button_switch.sensor.enocean.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.button_switch.sensor.messages.SwitchNotificationMsg;
import appsgate.lig.button_switch.sensor.spec.CoreSwitchSensorSpec;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.enocean.ubikit.adapter.spec.UbikitAdapterService;

/**
 * This is the class that represent the EnOcean implementation of switch sensor.
 *
 * @author Cédric Gérard
 * @since December 1, 2012
 * @version 1.0.0
 *
 * @see SwitchSensorSpec
 * @see CoreObjectSpec
 */
public class EnoceanSwitchSensorImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreSwitchSensorSpec {

    /**
     * Static class member uses to log what happened in each instances
     */
    private static Logger logger = LoggerFactory.getLogger(EnoceanSwitchSensorImpl.class);

    /**
     * the system name of this sensor.
     */
    private String sensorName;

    /**
     * The network sensor id
     */
    private String sensorId;

    /**
     * The sensor type (Actuator or Sensor)
     */
    private String sensoreType;

    /**
     * True if the device is paired with EnOcean proxy false otherwise
     */
    private String isPaired;

    /**
     * Hold the last signal strength in DBM
     */
    private String signal;

    /**
     * the switch number
     */
    private String switchNumber;

    /**
     * the button last status (On=true / Off=false / neutral="none")
     */
    private String buttonStatus;

    /**
     * Attribute use to indicate that the status change
     */
    private String switchState;

    /**
     * The type for user of this sensor
     */
    private String userType;

    /**
     * The current sensor status.
     *
     * 0 = Off line or out of range 
     * 1 = In validation mode (test range for sensor for instance)
     * 2 = In line or connected
     */
    private String status;

    /**
     * EnOcean proxy service uses to validate the sensor configuration with the
     * EnOcean proxy (pairing phase)
     */
    UbikitAdapterService enoceanProxy;

    @Override
    public JSONObject getDescription() throws JSONException {
        JSONObject descr = new JSONObject();
        descr.put("id", sensorId);
        descr.put("type", userType); //2 for switch sensor
        descr.put("status", status);
        descr.put("deviceType", sensoreType);
        descr.put("switchNumber", switchNumber);

        if (buttonStatus.contentEquals("true")) {
            descr.put("buttonStatus", 1);
        } else if (buttonStatus.contentEquals("false")) {
            descr.put("buttonStatus", 0);
        } else {
            descr.put("buttonStatus", -1);
        }
        return descr;
    }

    @Override
    public Action getLastAction() {
        Integer switchButton = new Integer(switchNumber);
        return new Action(switchButton.byteValue(), buttonStatus);
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public boolean isPaired() {
        return Boolean.valueOf(isPaired);
    }

    public void setPaired(boolean isPaired) {
        this.isPaired = String.valueOf(isPaired);
    }

    public String getSignal() {
        return signal;
    }

    public String getSensorId() {
        return sensorId;
    }

    @Override
    public String getAbstractObjectId() {
        return getSensorId();
    }

    public String getSensoreType() {
        return sensoreType;
    }

    @Override
    public String getUserType() {
        return userType;
    }

    @Override
    public int getObjectStatus() {
        return Integer.valueOf(status);
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        logger.info("New switch sensor detected, " + sensorId);

    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        logger.info("Switch sensor desapeared, " + sensorId);
    }

    public void isPairedChanged(String newPairedState) {
        logger.info("New Paired status, " + newPairedState);
    }

    /**
     * Called by ApAM when the signal strength changed
     *
     * @param newSignalValue the new signal value
     */
    public void signalChanged(String newSignalValue) {
        logger.info(newSignalValue + " dbm signal strength for " + sensorId);
        notifyChanges("signal", newSignalValue);
    }

    /**
     * Called by APAM when a switch state changed.
     *
     * @param justuse to trigger the state change
     */
    public void switchChanged(String status) {
        if (Boolean.valueOf(switchState)) {
            // Calculate the number of the button to return
            Integer number = 1;
            if (this.switchNumber.equals("1")) {
                number = 5;
            }
            if (buttonStatus.equalsIgnoreCase("false")) {
                number += 2;
            }

            notifyChanges("switchNumber", number.toString());
            this.switchNumber = "-1";
            this.buttonStatus = "none";
            switchState = "false";
        }
    }

    /**
     * Called by ApAM when the status value changed
     *
     * @param newStatus the new status value. Its a string the represent a
     * integer value for the status code.
     */
    public void statusChanged(String newStatus) {
        logger.info("The sensor, " + sensorId + " status changed to " + newStatus);
        notifyChanges("status", newStatus);
    }

    /**
     * This method uses the ApAM message model. Each call produce a
     * SwitchNotificationMsg object and notifies ApAM that a new message has
     * been released.
     *
     * @return nothing, it just notifies ApAM that a new message has been
     * posted.
     */
    public NotificationMsg notifyChanges(String varName, String value) {
        return new SwitchNotificationMsg(new Integer(switchNumber), "true", varName, value, this);
    }

    @Override
    public CORE_TYPE getCoreType() {
        return CORE_TYPE.DEVICE;
    }

}
