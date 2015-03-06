package appsgate.lig.keycard.sensor.enocean.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.keycard.sensor.messages.KeyCardNotificationMsg;
import appsgate.lig.keycard.sensor.spec.CoreKeyCardSensorSpec;
import appsgate.lig.enocean.ubikit.adapter.spec.UbikitAdapterService;

/**
 * This is the class that represent the EnOcean implementation of key card sensor.
 * 
 * @author Cédric Gérard
 * @since December 1, 2012
 * @version 1.0.0
 * 
 * @see KeyCardSensorSpec
 * @see CoreObjectSpec
 */
public class EnoceanKeyCardSensorImpl extends CoreObjectBehavior implements CoreKeyCardSensorSpec, CoreObjectSpec {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(EnoceanKeyCardSensorImpl.class);
	
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
	 * The current status (Card inserted/removed) = the last value received from this sensor
	 */
	private String currentStatus;

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
	
	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", sensorId);
		descr.put("type", userType); //4 for keyCard sensor
		descr.put("status", status);
		descr.put("inserted", currentStatus);
		descr.put("deviceType", sensoreType);
		
		return descr;
	}

	/**
	 * EnOcean proxy service uses to validate the sensor configuration with the
	 * EnOcean proxy (pairing phase)
	 */
	UbikitAdapterService enoceanProxy;
	
	@Override
	public boolean getCardState() {
		return Boolean.valueOf(currentStatus);
	}
	
	@Override
	public int getLastCardNumber() {
		return -1;
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
	
	public String getSignal() {
		return signal;
	}

	public void setPaired(boolean isPaired) {
		this.isPaired = String.valueOf(isPaired);
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
		logger.info("New keycard sensor detected, "+sensorId);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("Keycard sensor desapeared, "+sensorId);
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
	 * Called by APAM when a new status value is received from the sensor.
	 * @param newStatusValue the new status (Card inserted = true, card removed = false)
	 */
	public void currentStatusChanged (String newStatusValue) {
		logger.info("New status value from "+sensorId+"/"+sensorName+", "+newStatusValue);
		notifyChanges("inserted", newStatusValue);
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
	 * KeyCardNotificationMsg object and notifies ApAM that a new message has
	 * been released.
	 * 
	 * @return nothing, it just notifies ApAM that a new message has been
	 *         posted.
	 */
	public NotificationMsg notifyChanges(String varName, String value) {
		return new KeyCardNotificationMsg(Boolean.valueOf(currentStatus), varName, value, this);
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}

}
