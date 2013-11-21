package appsgate.lig.contact.sensor.dfki.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.adapter.urc.services.URCAdapterServices;
import appsgate.lig.contact.sensor.messages.ContactNotificationMsg;
import appsgate.lig.contact.sensor.spec.CoreContactSensorSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

public class DFKIContactSensorImpl implements CoreObjectSpec,
		CoreContactSensorSpec {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(DFKIContactSensorImpl.class);
	
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
	 * The current status = the last value received from this sensor
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

	/**
	 * The current picture identifier
	 */
	private String pictureId;

	/**
	 * URC Adapter services
	 */
	URCAdapterServices URCAdapterServices;

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
		notifyChanges("pictureId",  pictureId);

	}
	
	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", sensorId);
		descr.put("type", userType); //3 for contact sensor
		descr.put("status", status);
		descr.put("contact", currentStatus);
		
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
	
	public void isPairedChanged(String newPairedState){
		logger.info("New Paired status, "+newPairedState+", for "+sensorId);
	}
	
	/**
	 * Called by APAM when a new contact status is received from the sensor.
	 * @param newContactStatus the new contact status
	 */
	public void currentStatusChanged(String newContactStatus) {
		logger.info("New contact status from "+sensorId+"/"+sensorName+", "+newContactStatus);
		notifyChanges("contact", newContactStatus);
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
	 * ContactNotificationMsg object and notifies ApAM that a new message has
	 * been released.
	 * 
	 * @return nothing, it just notifies ApAM that a new message has been
	 *         posted.
	 */
	public NotificationMsg notifyChanges(String varName, String value) {
		return new ContactNotificationMsg(Boolean.valueOf(currentStatus), varName, value, this);
	}


}
