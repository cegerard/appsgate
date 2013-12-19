package appsgate.lig.plug.actuator_sensors.enocean.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.enocean.ubikit.adapter.services.EnOceanService;
import appsgate.lig.smartplug.actuator_sensor.messages.SmartPlugNotificationMsg;
import appsgate.lig.smartplug.actuator_sensor.spec.CoreSmartPlugSpec;

public class EnoceanPlugAcuatorSensorImpl implements CoreObjectSpec, CoreSmartPlugSpec {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(EnoceanPlugAcuatorSensorImpl.class);
	
	private String sensorName;
	private String sensorId;
	private String sensoreType;
	private String pictureId;
	private String userType;
	private String status;
	private String isPaired;
	
	private String plugState;
	
	private String consumption;
	
	/**
	 * EnOcean proxy service uses to validate the sensor configuration with the
	 * EnOcean proxy (pairing phase)
	 */
	EnOceanService enoceanProxy;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New smart plug sensor detected, "+sensorId);
		setSensorName("SmartPlug-"+sensorId);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("Smart plug sensor desapeared, "+sensorId);
	}
	
	/**
	 * Called by ApAM when the isPaired property is changed
	 * @param newPairedState the new paired state
	 */
	public void isPairedChanged(String newPairedState){
		logger.info("New Paired status, "+newPairedState+", for "+sensorId);
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
	
	public void plugStateChanged(String plugState) {
		logger.info("The plug state, "+ sensorId+" changed to "+plugState);
		notifyChanges("plugState", plugState);
	}
	
	public void consumptionChanged(String consumption) {
		logger.info("The sensor, "+ sensorId+" consumption changed to "+consumption);
		notifyChanges("consumption", consumption);
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
		return new SmartPlugNotificationMsg(this, varName, value);
	}
	
	/* ***********************************************************************
	 * 							 PUBLIC FUNCTIONS                            *
	 *********************************************************************** */
	
	@Override
	public void toggle() {
		boolean state = new Boolean(plugState);
		if(state){
			off();
		}else{
			on();
		}
	}

	@Override
	public void on() {
		enoceanProxy.turnOnActuator(sensorId);
	}

	@Override
	public void off() {
		enoceanProxy.turnOffActuator(sensorId);
	}

	@Override
	public int activePower() {
		enoceanProxy.sendActuatorUpdateEvent(sensorId);
		return new Integer(consumption);
	}

	@Override
	public int activeEnergy() {
		return -1;
	}

	@Override
	public boolean getRelayState() {
		boolean state = new Boolean(plugState);
		return state;
	}

	/* ***********************************************************************
	 * 							    ACCESSORS                                *
	 *********************************************************************** */
	
	@Override
	public String getAbstractObjectId() {
		return sensorId;
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
		descr.put("id", sensorId);
		descr.put("type", userType); //6 for SmartPlug sensor
		descr.put("status", status);
		descr.put("plugState", plugState);
		descr.put("consumption", consumption);
		
		return descr;
	}

	@Override
	public void setPictureId(String pictureId) {
		this.pictureId = pictureId;
	}

	public boolean isPaired() {
		return Boolean.valueOf(isPaired);
	}

	public void setPaired(boolean isPaired) {
		this.isPaired = String.valueOf(isPaired);
	}
	
	public String getSensoreType() {
		return sensoreType;
	}
	
	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}
}
