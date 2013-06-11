package appsgate.lig.button_switch.sensor.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.button_switch.sensor.messages.SwitchNotificationMsg;
import appsgate.lig.button_switch.sensor.spec.SwitchSensorSpec;
import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;
import appsgate.lig.proxy.services.EnOceanService;

public class SwitchSensorImpl implements SwitchSensorSpec, AbstractObjectSpec {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(SwitchSensorImpl.class);
	
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
	 * the switch number
	 */
	private String switchNumber;
	
	/**
	 * the button last status (On=true / Off=false)
	 */
	private String buttonStatus;
	
	/**
	 * Attribute use to indicate that the status change
	 */
	private boolean switchState;

	/**
	 * The location where the sensor is installed
	 */
	private String locationId;

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
	EnOceanService enoceanProxy;
	
	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", sensorId);
		descr.put("type", userType); //2 for switch sensor
		descr.put("locationId", locationId);
		descr.put("status", status);
		descr.put("switchNumber", switchNumber);
		boolean stateBtn = Boolean.valueOf(buttonStatus);
		if(stateBtn){
			descr.put("buttonStatus", 1);
		} else {
			descr.put("buttonStatus", 0);
		}
		return descr;
	}

	@Override
	public Action getLastAction() {
		Integer switchButton = new Integer(switchNumber);
		return new Action(switchButton.byteValue(), Boolean.valueOf(buttonStatus));
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
	public int getLocationId() {
		return Integer.valueOf(locationId);
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
	public void setLocationId(int locationId) {
		this.locationId = String.valueOf(locationId);
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
		logger.info("New switch sensor detected, "+sensorId);
		
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("Switch sensor desapeared, "+sensorId);
	}
	
	public void isPairedChanged(String newPairedState){
		logger.info("New Paired status, "+newPairedState);
	}
	
//	/**
//	 * Called by APAM when a new switch is pressed.
//	 * @param newSwitchNumber the new switch number
//	 */
//	public void switchNumberChanged(String newSwitchNumber) {
//		logger.info("New switch value from "+sensorId+"/"+sensorName+", "+newSwitchNumber);
//		notifyChanges("switchNumber", newSwitchNumber);
//	}
//	
//	/**
//	 *  Called by APAM when a switch status changed.
//	 * @param status the new status
//	 */
//	public void buttonStatusChanged(String status) {
//		logger.info("New switch value from "+sensorId+"/"+sensorName+", "+status);
//		notifyChanges("buttonStatus", status);
//	}
	
	/**
	 *  Called by APAM when a switch state changed.
	 * @param justuse to trigger the state change
	 */
	public void switchChanged(String status) {
		if(switchState) {
			notifyChanges("switchNumber", this.switchNumber);
			logger.info("New switch value from "+sensorId+"/"+sensorName+", "+this.switchNumber);
			notifyChanges("buttonStatus", this.buttonStatus);
			logger.info("New switch value from "+sensorId+"/"+sensorName+", "+this.buttonStatus);
			switchState = false;
		}
	}
	
	/**
	 * Called by ApAM when the status value changed
	 * @param newStatus the new status value.
	 * Its a string the represent a integer value for the status code.
	 */
	public void statusChanged(String newStatus) {
		logger.info("The sensor, "+ sensorId+" status changed to "+newStatus);
		notifyChanges("status", newStatus);
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
		return new SwitchNotificationMsg(new Integer(switchNumber), Boolean.valueOf(buttonStatus), varName, value, this);
	}

}
