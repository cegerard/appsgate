package appsgate.lig.temperature.sensor.enocean.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.enocean.ubikit.adapter.spec.UbikitAdapterService;
import appsgate.lig.temperature.sensor.messages.TemperatureNotificationMsg;
import appsgate.lig.temperature.sensor.spec.CoreTemperatureSensorSpec;

/**
 * This is the class that represent the EnOcean implementation of temperature sensor.
 * 
 * @author Cédric Gérard
 * @since December 1, 2012
 * @version 1.0.0
 * 
 * @see TemperatureSensorSpec
 * @see CoreObjectSpec
 */
public class EnoceanTemperatureSensorImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreTemperatureSensorSpec  {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(EnoceanTemperatureSensorImpl.class);

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
	private String currentTemperature;
	
	/**
	 * The last temperature value;
	 */
	private float lastTempRounded;

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

	@Override
	public TemperatureUnit getTemperatureUnit() {
		return TemperatureUnit.Celsius;
	}

	@Override
	public float getTemperature() {
		return (float)Math.rint(Float.valueOf(currentTemperature)*10)/10;
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
		descr.put("type", userType); //O for temperature sensor
		descr.put("status", status);
		descr.put("value", currentTemperature);
        descr.put("change", "true");
		descr.put("deviceType", sensorType);
		
		return descr;
	}

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New temperature sensor detected, "+sensorId);
		this.lastTempRounded = Float.valueOf(currentTemperature);
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
	
	/**
	 * Called by APAM when a new temperature value is received from the sensor.
	 * @param newTemperatureValue the new temperature
	 */
	public void currentTemperatureChanged (String newTemperatureValue) {
		logger.info("New temperature value from "+sensorId+"/"+sensorName+", "+newTemperatureValue);
		float curTemp = getTemperature();
		if(curTemp != lastTempRounded){
			lastTempRounded = curTemp;
			notifyChanges("value", Float.toString(curTemp));
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
		return new TemperatureNotificationMsg(Float.valueOf(currentTemperature), varName, value, this);
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}
	
}
