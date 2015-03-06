package appsgate.lig.simulated.temperature.sensor.impl;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.temperature.sensor.messages.TemperatureNotificationMsg;
import appsgate.lig.temperature.sensor.spec.CoreTemperatureSensorSpec;

/**
 * This is the class that represent the simulate implementation of temperature sensor.
 * 
 * @author Cédric Gérard
 * @since June 18, 2013
 * @version 1.0.0
 * 
 * @see TemperatureSensorSpec
 * @see AbstractObjectSpec
 */
public class SimulatedTemperatureSensorImpl extends CoreObjectBehavior implements CoreObjectSpec,
		CoreTemperatureSensorSpec {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(SimulatedTemperatureSensorImpl.class);
	
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
	 * The simulation refresh rate
	 */
	private String evolutionRate;
	
	/**
	 * The simulation step value
	 */
	private String evolutionValue;

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
	 * The timer that trigger the refresh task each "rate" milliseconds
	 */
	private Timer refreshTimer = new Timer();
	
	/**
	 * The RefreshTask member
	 */
	private RefreshTask refreshtask = new RefreshTask();
	
	/**
	 * The timer that trigger the simulation refresh task
	 */
	private Timer simulationTimer = new Timer();
	
	/**
	 * The RefreshTask member
	 */
	private SimulationTask simulationtask = new SimulationTask();
	
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
	public String getUserType() {
		return userType;
	}

	@Override
	public int getObjectStatus() {
		return Integer.valueOf(status);
	}
	
	/**
	 * Set the sensor status by java method call.
	 * @param newStatus the new status as a String
	 */
	public void setObjectStatus(String newStatus) {
		status = newStatus;
		statusChanged(newStatus);
	}

	
	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		
		descr.put("id", sensorId);
		descr.put("type", userType); //2 for temperature sensor
		descr.put("status", status);
		descr.put("value", currentTemperature);
		
		return descr;
	}

	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New simulated temperature sensor detected, "+sensorId);
//		long id = (long)Math.random()*100000;
//		sensorName += "-"+id;
//	    sensorId = "0-"+id;
		refreshTimer.scheduleAtFixedRate(refreshtask, 5000, Long.valueOf(notifRate));
		simulationTimer.scheduleAtFixedRate(simulationtask, 5000, Long.valueOf(evolutionRate));
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		refreshtask.cancel();
		refreshTimer.cancel();
		refreshTimer.purge();
		
		simulationtask.cancel();
		simulationTimer.cancel();
		simulationTimer.purge();
		
		logger.info("simulated Temperature sensor desapeared, "+sensorId);
	}
	
	/**
	 * Called by APAM when a new temperature value is received from the sensor.
	 * @param newTemperatureValue the new temperature
	 */
	public void currentTemperatureChanged (String newTemperatureValue) {
		logger.info("New simulated temperature value from "+sensorId+"/"+sensorName+", "+newTemperatureValue);
		notifyChanges("value", newTemperatureValue);
	}
	
	/**
	 * Called by ApAM when the status value change
	 * @param newStatus the new status value.
	 * its a string the represent a integer value for the status code.
	 */
	public void statusChanged(String newStatus) {
		logger.info("The sensor, "+ sensorId+" status changed to "+newStatus);
		notifyChanges("status", newStatus);
	}
	
	/**
	 * Called by ApAM when the refresh rate change.
	 * @param newRate the new refresh rate as a String
	 */
	public void rateChanged (String newRate) {
		logger.info("The sensor, "+ sensorId+" notification rate changed to "+newRate);
		reScheduleRefreshTask();
		notifyChanges("notifRate", newRate);
	}
	
	/**
	 * Called by ApAM when the simulation rate change.
	 * @param newRate the new simulation rate as a String
	 */
	public void SimulationRateChanged (String newRate) {
		logger.debug("The sensor, "+ sensorId+" evolution rate changed to "+newRate);
		reScheduleEvolutionTask();
	}
	
	/**
	 * Set the simulated temperature from java method call
	 * @param newTemperatureValue the new temperature as a String
	 */
	public void setTemperature(String newTemperatureValue) {
		this.currentTemperature = newTemperatureValue;
		notifyChanges("value", newTemperatureValue);
	}
	
	/**
	 * Set the refresh rate with a Java method call
	 * @param newRate the new refresh rate as a String
	 */
	public void setRefreshRate(String newRate) {
		notifRate = newRate;
		rateChanged(newRate);
	}
	
	/**
	 * Set the refresh simulation rate with a Java method call
	 * @param newRate the new refresh rate as a String
	 */
	public void setSimulationRate(String newRate) {
		evolutionRate = newRate;
		SimulationRateChanged(newRate);
	}
	
	/**
	 * Set the evolutionValue
	 * @param value the new evolution value
	 */
	public void setSimulationValue(String value) {
		logger.debug("The sensor, "+ sensorId+" evolution value changed to "+value);
		evolutionValue = value;
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
	
	/**
	 * Reschedule the refresh task with the rate
	 * member value
	 */
	private void reScheduleRefreshTask() {
		refreshtask.cancel();
		refreshtask = new RefreshTask();
		refreshTimer.scheduleAtFixedRate(refreshtask, 0, Long.valueOf(notifRate));
	}
	
	/**
	 * Reschedule the simulation task with the rate
	 * member value
	 */
	private void reScheduleEvolutionTask() {
		simulationtask.cancel();
		simulationtask = new SimulationTask();
		simulationTimer.scheduleAtFixedRate(simulationtask, 0, Long.valueOf(evolutionRate));
	}
	
	/**
	 * The task that is executed automatically to notify the temperature
	 */
	private class RefreshTask extends TimerTask {

		@Override
		public void run() {
			notifyChanges("value", currentTemperature);
		}
	}

	/**
	 * The task that is executed automatically to notify the temperature
	 */
	private class SimulationTask extends TimerTask {

		@Override
		public void run() {
			float val = Float.valueOf(evolutionValue);
			float res = getTemperature()+val;
			setTemperature(String.valueOf(res));
		}
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SIMULATED_DEVICE;
	}
}
