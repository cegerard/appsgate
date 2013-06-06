package appsgate.lig.core.virtual.temperature.sensor.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;
import appsgate.lig.temperature.sensor.messages.TemperatureNotificationMsg;
import appsgate.lig.temperature.sensor.spec.TemperatureSensorSpec;

public class CoreVirtualTemperatureSensorImpl implements AbstractObjectSpec,
		TemperatureSensorSpec {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(CoreVirtualTemperatureSensorImpl.class);
	
	/**
	 * the system name of this sensor.
	 */
	private String sensorName;

	/**
	 * The network sensor id
	 */
	private String sensorId;

	/**
	 * The current temperature = the last value received from this sensor
	 */
	private String currentTemperature;

	/**
	 * The temperature notification rate
	 */
	private String notifRate;
	
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
	
	@Override
	public TemperatureUnit getTemperatureUnit() {
		return TemperatureUnit.Celsius;
	}

	@Override
	public float getTemperature() {
		return Float.valueOf(currentTemperature);
	}
	
	public String getSensorName() {
		return sensorName;
	}
	
	public String getSensorId() {
		return sensorId;
	}

	@Override
	public String getAbstractObjectId() {
		return getSensorId();
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
	
	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		
		descr.put("id", sensorId);
		descr.put("type", userType); //99 for virtual temperature sensor
		descr.put("locationId", locationId);
		descr.put("status", status);
		descr.put("value", currentTemperature);
		
		return descr;
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
		logger.info("New virtual temperature sensor detected, "+sensorId);
		long id = (long)Math.random()*100000;
		sensorName += "-"+id;
	    sensorId = "0-"+id;
	    status = "2";
			
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("Virtual Temperature sensor desapeared, "+sensorId);
	}
	
	/**
	 * Called by APAM when a new temperature value is received from the sensor.
	 * @param newTemperatureValue the new temperature
	 */
	public void currentTemperatureChanged (String newTemperatureValue) {
		logger.info("New virtual temperature value from "+sensorId+"/"+sensorName+", "+newTemperatureValue);
		notifyChanges("value", newTemperatureValue);
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
	
	public void rateChanged (String newRate) {
		logger.info("The sensor, "+ sensorId+" notification rate changed to "+newRate);
		//TODO rescheduler l'envoi de notifs
		notifyChanges("notifRate", newRate);
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

}
