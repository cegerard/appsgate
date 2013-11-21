package appsgate.lig.wmc.dfki.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.adapter.urc.services.URCAdapterServices;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;


public class DFKI_WMC_Impl implements CoreObjectSpec {
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(DFKI_WMC_Impl.class);
	
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
	 * URC proxy service 
	 */
	URCAdapterServices URCAdapterServices;
	
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
	public String getPictureId() {
		return pictureId;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		
		descr.put("id", actuatorId);
		descr.put("type", userType); //??
		descr.put("status", status);
		
		return descr;
	}

	@Override
	public void setPictureId(String pictureId) {
		this.pictureId = pictureId;
		notifyChanges("pictureId", pictureId);
	}
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New WMC services detected, "+actuatorId);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("WMC services desapeared, "+actuatorId);
	}
	
	public void isPairedChanged(String newPairedState){
		logger.info("New Paired status, "+newPairedState+", for "+actuatorId);
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
		return null;//new MediaPlayerNotificationMsg(isOn, varName, value, this);
	}
	
}
