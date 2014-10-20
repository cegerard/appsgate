package appsgate.lig.luminosity.sensor.enocean.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.luminosity.sensor.messages.IlluminationNotificationMsg;
import appsgate.lig.luminosity.sensor.spec.CoreLuminositySensorSpec;
import appsgate.lig.enocean.ubikit.adapter.spec.UbikitAdapterService;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the class that represent the EnOcean implementation of illumination
 * sensor.
 *
 * @author Cédric Gérard
 * @since December 1, 2012
 * @version 1.0.0
 *
 * @see LuminositySensorSpec
 * @see CoreObjectSpec
 */
public class EnoceanLuminositySensorImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreLuminositySensorSpec {

    /**
     * Static class member uses to log what happened in each instances
     */
    private static Logger logger = LoggerFactory.getLogger(EnoceanLuminositySensorImpl.class);

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
     * The current illumination = the last value received from this sensor
     */
    private String currentIllumination;

    /**
     * The type for user of this sensor
     */
    private String userType;

    /**
     * The current sensor status.
     *
     * 0 = Off line or out of range 1 = In validation mode (test range for
     * sensor for instance) 2 = In line or connected
     */
    private String status;

    /**
     * The current picture identifier
     */
    private String pictureId;

    /**
     * EnOcean proxy service uses to validate the sensor configuration with the
     * EnOcean proxy (pairing phase)
     */
    UbikitAdapterService enoceanProxy;

    public enum ScaleLuminosity {

        low("low", 0, 300),
        medium("medium", 301, 500),
        high("high", 501, 1000),
        veryHigh("veryHigh", 1001, 2000),
        highest("highest", 2001, 30000);

        public String label;
        public int minValue;
        public int maxValue;

        ScaleLuminosity(String label, int minValue, int maxValue) {
            this.label = label;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

    }

    @Override
    public LuminosityUnit getLuminosityUnit() {
        return LuminosityUnit.Lux;
    }

    @Override
    public int getIllumination() {
        return Integer.valueOf(currentIllumination);
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

    @Override
    public String getPictureId() {
        return pictureId;
    }

    @Override
    public void setPictureId(String pictureId) {
        this.pictureId = pictureId;
        notifyChanges("pictureId", pictureId);
    }

    @Override
    public JSONObject getDescription() throws JSONException {
        JSONObject descr = new JSONObject();
        descr.put("id", sensorId);
        descr.put("type", userType); //1 for illumination sensor
        descr.put("status", status);
        descr.put("value", currentIllumination);
        descr.put("label", getCurrentIlluminationLabel());
        descr.put("deviceType", sensoreType);

        return descr;
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        logger.info("New illumination sensor detected, " + sensorId);
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        logger.info("Illumination sensor desapeared, " + sensorId);
    }

    public void isPairedChanged(String newPairedState) {
        logger.info("New Paired status, " + newPairedState + ", for " + sensorId);
    }

    /**
     * Called by ApAM when the signal strength changed
     *
     * @param newSignalValue the new singal value
     */
    public void signalChanged(String newSignalValue) {
        logger.info(newSignalValue + " dbm signal strength for " + sensorId);
        notifyChanges("signal", newSignalValue);
    }

    /**
     * Called by APAM when a new illumination value is received from the sensor.
     *
     * @param newIlluminationValue the new illumination
     */
    public void currentIlluminationChanged(String newIlluminationValue) {
        logger.info("New illumination value from " + sensorId + "/" + sensorName + ", " + newIlluminationValue);
        notifyChanges("value", newIlluminationValue);
        notifyChanges("label", getIlluminationLabel(Integer.valueOf(newIlluminationValue)));
    }

    /**
     * Called by ApAM when the status value changed
     *
     * @param newStatus the new status value. its a string the represent a
     * integer value for the status code.
     */
    public void statusChanged(String newStatus) {
        logger.info("The sensor, " + sensorId + " status changed to " + newStatus);
        notifyChanges("status", newStatus);
    }

    /**
     * This method uses the ApAM message model. Each call produce a
     * IlluminationNotificationMsg object and notifies ApAM that a new message
     * has been released.
     *
     * @return nothing, it just notifies ApAM that a new message has been
     * posted.
     */
    public NotificationMsg notifyChanges(String varName, String value) {
        return new IlluminationNotificationMsg(Integer.valueOf(currentIllumination), varName, value, this);
    }

    @Override
    public CORE_TYPE getCoreType() {
        return CORE_TYPE.DEVICE;
    }
    
    @Override
    public String getCurrentIlluminationLabel() {
        int currentValue = getIllumination();
        for (ScaleLuminosity sl : ScaleLuminosity.values()) {
            if (sl.minValue <= currentValue && currentValue <= sl.maxValue) {
                return sl.label;
            }
        }
        return "Invalid Value";
    }

    @Override
    public String getIlluminationLabel(int value) {
        for (ScaleLuminosity sl : ScaleLuminosity.values()) {
            if (sl.minValue <= value && value <= sl.maxValue) {
                return sl.label;
            }
        }
        return "Invalid Value";
    }

    @Override
    public List<String> getListScaleLabel() {
        List listScaleLbl = new ArrayList<String>();
        for (ScaleLuminosity sl : ScaleLuminosity.values()) {
            listScaleLbl.add(sl.label);
        }
        return listScaleLbl;
    }

    @Override
    public int getMinValue(String labelIllumination) {
        for (ScaleLuminosity sl : ScaleLuminosity.values()) {
            if (sl.label.equals(labelIllumination)) {
                return sl.minValue;
            }
        }
        return -99999;
    }

    @Override
    public int getMaxValue(String labelIllumination) {
        for (ScaleLuminosity sl : ScaleLuminosity.values()) {
            if (sl.label.equals(labelIllumination)) {
                return sl.maxValue;
            }
        }
        return 99999;
    }

}
