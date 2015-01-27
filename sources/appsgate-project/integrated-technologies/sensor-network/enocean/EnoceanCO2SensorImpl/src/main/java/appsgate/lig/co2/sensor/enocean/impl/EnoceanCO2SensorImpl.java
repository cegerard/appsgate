package appsgate.lig.co2.sensor.enocean.impl;

import appsgate.lig.co2.sensor.messages.Co2NotificationMsg;
import appsgate.lig.co2.sensor.spec.CoreCO2SensorSpec;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.enocean.ubikit.adapter.spec.UbikitAdapterService;

/**
 * This is the class that represent the EnOcean implementation of temperature sensor.
 * 
 * @author Jander Nascimento
 * @since Janvier 26, 2015
 * @version 1.0.0
 * 
 * @see CoreCO2SensorSpec
 * @see CoreObjectSpec
 */
public class EnoceanCO2SensorImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreCO2SensorSpec {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(EnoceanCO2SensorImpl.class);

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
	private String sensorType;

	/**
	 * True if the device is paired with EnOcean proxy false otherwise
	 */
	private String isPaired;
	
	/**
	 * Hold the last signal strength in DBM
	 */
	private String signal;

	/**
	 * The current temperature = the last value received from this sensor
	 */
	private Integer concentration;
	
	/**
	 * The last temperature value;
	 */
	private float lastConcentration;

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
	 * The current picture identifier
	 */
	private String pictureId;
	
	/**
	 * EnOcean proxy service uses to validate the sensor configuration with the
	 * EnOcean proxy (pairing phase)
	 */
	UbikitAdapterService enoceanProxy;

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

	/**
	 * Get the kind of sensor
	 * @return String that represent the sensor type from EnOcean
	 */
	public String getSensoreType() {
		return sensorType;
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
		descr.put("type", userType);
		descr.put("status", status);
		descr.put("value", concentration);
        descr.put("change", "true");
		descr.put("deviceType", sensorType);
		
		return descr;
	}

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New temperature sensor detected, "+sensorId);
		this.lastConcentration = Float.valueOf(concentration);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("Temperature sensor desapeared, "+sensorId);
	}
	
	public void isPairedChanged(String newPairedState){
		logger.info("New Paired status, "+newPairedState+", for "+sensorId);
	}
	
	/**
	 * Called by ApAM when the signal strength changed
	 * @param newSignalValue the new singal value
	 */
	public void signalChanged(String newSignalValue) {
		logger.info(newSignalValue+" dbm signal strength for "+sensorId);
		notifyChanges("signal", newSignalValue);
	}

	public void concentrationChanged (String newCO2Value) {
		logger.info("New CO2 value obtained from {} with the name {} value {}",sensorId,sensorName,newCO2Value);
		Integer currentConcentration = getCO2Concentration();
		if(currentConcentration != lastConcentration){
			lastConcentration = currentConcentration;
			notifyChanges("value", Integer.toString(currentConcentration));
			notifyChanges("change", "true");
		}
	}
	
	/**
	 * Called by ApAM when the status value changed
	 * @param newStatus the new status value.
	 * its a string the represent a integer value for the status code.
	 */
	public void statusChanged(String newStatus) {
		logger.info("The sensor, "+ sensorId+" status changed to "+newStatus);
		notifyChanges("status", newStatus);
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
		return new Co2NotificationMsg(varName, value, value, this);
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}

	@Override
	public int getCO2Concentration() {
		return concentration;
	}
}
