package appsgate.lig.contact.sensor.enocean.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.contact.sensor.messages.ContactNotificationMsg;
import appsgate.lig.contact.sensor.spec.CoreContactSensorSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.enocean.ubikit.adapter.spec.UbikitAdapterService;


/**
 * This is the class that represent the EnOcean implementation of contact sensor.
 * 
 * @author Cédric Gérard
 * @since December 1, 2012
 * @version 1.0.0
 * 
 * @see CoreContactSensorSpec
 * @see CoreObjectSpec
 */
public class EnoceanContactSensorImpl extends CoreObjectBehavior implements CoreContactSensorSpec, CoreObjectSpec {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(EnoceanContactSensorImpl.class);
	
	/**
	 * the system name of this sensor.
	 */
	private String sensorName;

	/**
	 * The network sensor id
	 */
	private String sensorId ="";

	/**
	 * The sensor type (Actuator or Sensor)
	 */
	private String sensorType = "";

	/**
	 * True if the device is paired with EnOcean proxy false otherwise
	 */
	private String isPaired = "";
	
	/**
	 * Hold the last signal strength in DBM
	 */
	private String signal = "";
	
	/**
	 * The current status = the last value received from this sensor
	 */
	private String currentStatus = "";

	/**
	 * The type for user of this sensor
	 */
	private String userType = "";

	/**
	 * The current sensor status.
	 * 
	 * 0 = Off line or out of range
	 * 1 = In validation mode (test range for sensor for instance)
	 * 2 = In line or connected
	 */
	private String status = "";

	/**
	 * EnOcean proxy service uses to validate the sensor configuration with the
	 * EnOcean proxy (pairing phase)
	 */
	UbikitAdapterService enoceanProxy;

	@Override
	public boolean getContactStatus() {
		return Boolean.valueOf(currentStatus);
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

	public String getSensorType() {
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
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", sensorId);
		descr.put("type", userType); //3 for contact sensor
		descr.put("status", status);
		descr.put("contact", currentStatus);
		descr.put("deviceType", sensorType);
		
		return descr;
	}

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New contact sensor detected, "+sensorId);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("Contact sensor desapeared, "+sensorId);
	}
	
	public void isPairedChanged(String newPairedState) {
		logger.info("New Paired status, "+newPairedState+", for "+sensorId);
	}
	
	/**
	 * Called by ApAM when the signal strength changed
	 * @param newSignalValue the new signal value
	 */
	public void signalChanged(String newSignalValue) {
		logger.info(newSignalValue+" dbm signal strength for "+sensorId);
		notifyChanges("signal", signal, newSignalValue);
        signal = newSignalValue;
	}
	
	/**
	 * Called by APAM when a new contact status is received from the sensor.
	 * @param newContactStatus the new contact status
	 */
	public void currentStatusChanged(String newContactStatus) {
		logger.info("New contact status from "+sensorId+"/"+sensorName+", "+newContactStatus);
		notifyChanges("contact", currentStatus, newContactStatus);
        currentStatus = newContactStatus;
	}
	
	/**
	 * Called by ApAM when the status value changed
	 * @param newStatus the new status value.
	 * its a string the represent a integer value for the status code.
	 */
	public void statusChanged(String newStatus) {
		logger.info("The sensor, "+ sensorId+" status changed to "+newStatus);

		notifyChanges("status", status, newStatus);
        status = currentStatus;
	}

	/**
	 * This method uses the ApAM message model. Each call produce a
	 * ContactNotificationMsg object and notifies ApAM that a new message has
	 * been released.
	 * 
	 * @return nothing, it just notifies ApAM that a new message has been
	 *         posted.
	 */
	public NotificationMsg notifyChanges(String varName, String oldValue, String newValue) {
		return new ContactNotificationMsg(varName, oldValue, newValue, this);
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}

}
