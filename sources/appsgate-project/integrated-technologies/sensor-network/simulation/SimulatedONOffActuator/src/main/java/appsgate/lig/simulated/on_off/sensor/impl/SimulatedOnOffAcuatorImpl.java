package appsgate.lig.simulated.on_off.sensor.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.on_off.actuator.messages.OnOffActuatorNotificationMsg;
import appsgate.lig.on_off.actuator.spec.CoreOnOffActuatorSpec;

/**
 * This is the class that represent the simulate implementation of On/Off actuator.
 * 
 * @author Cédric Gérard
 * @since November 14, 2013
 * @version 1.0.0
 * 
 * @see OnOffActuatorSpec
 */
public class SimulatedOnOffAcuatorImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreOnOffActuatorSpec {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(SimulatedOnOffAcuatorImpl.class);
	
	/**
	 * the system name of this sensor. 
	 */
	private String actuatorName;

	/**
	 * The network sensor id
	 */
	private String actuatorId;

	/**
	 * The sensor type (Actuator or Sensor)
	 */
	private String actuatorType;

	/**
	 * True if the device is paired with EnOcean proxy false otherwise
	 */
	private String isPaired;

	/**
	 * The current simulated actuator state
	 */
	private String isOn;

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
	
	public String getActuatorName() {
		return actuatorName;
	}

	public void setActuatorName(String actuatorName) {
		this.actuatorName = actuatorName;
	}

	public boolean isPaired() {
		return Boolean.valueOf(isPaired);
	}

	public void setPaired(boolean isPaired) {
		this.isPaired = String.valueOf(isPaired);
	}

	public String getActuatorId() {
		return actuatorId;
	}
	
	/**
	 * Get the kind of sensor
	 * @return String that represent the sensor type from EnOcean
	 */
	public String getSensoreType() {
		return actuatorType;
	}

	@Override
	public String getAbstractObjectId() {
		return getActuatorId();
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
		
		descr.put("id", actuatorId);
		descr.put("type", userType); //8 for On_Off device
		descr.put("status", status);
		descr.put("isOn", isOn);
		
		return descr;
	}
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New On/Off actuator detected, "+actuatorId);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("An On/Off actuator desapeared, "+actuatorId);
	}
	
	public void isPairedChanged(String newPairedState){
		logger.info("New Paired status, "+newPairedState+", for "+actuatorId);
	}
	
	/**
	 * Called by APAM when the status of the on/Off device change
	 */
	public void isOnChanged (String newIsOnValue) {
		logger.info("The "+actuatorId+"/"+actuatorName+", state change to "+newIsOnValue);
		notifyChanges("isOn", isOn);
	}
	
	/**
	 * Called by ApAM when the status value changed
	 * @param newStatus the new status value.
	 * its a string the represent a integer value for the status code.
	 */
	public void statusChanged(String newStatus) {
		logger.info("The sensor, "+ actuatorId+" status changed to "+newStatus);
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
		return new OnOffActuatorNotificationMsg(isOn, varName, value, this.getAbstractObjectId());
	}
	
	@Override
	public boolean getTargetState() {
	
		return Boolean.valueOf(isOn);
	}

	@Override
	public void on() {
		logger.info("The sensor, "+actuatorId+" status request changed to ON");
		isOn = "true";
	}

	@Override
	public void off() {
		logger.info("The sensor, "+actuatorId+" status request changed to OFF");
		isOn = "false";
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SIMULATED_DEVICE;
	}

}
