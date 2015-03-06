package appsgate.lig.temperature.sensor.watteco.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.temperature.sensor.messages.TemperatureNotificationMsg;
import appsgate.lig.temperature.sensor.spec.CoreTemperatureSensorSpec;
import appsgate.lig.watteco.adapter.spec.WattecoIOService;

/**
 * 
 * @author Cédric Gérard
 *
 */
public class WattecoTemperatureImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreTemperatureSensorSpec {


	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(WattecoTemperatureImpl.class);
	
	private String sensorName;
	private String sensorId;
	private String sensoreType;
	private String userType;
	private String status;
	private String isPaired;
	private String route;
	
	private String currentTemperature;
	
	/** the main border router */
	WattecoIOService wattecoAdapter;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New temperature sensor detected, "+sensorId);
		setSensorName("Temperature-"+sensorId);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("Temperature sensor desapeared, "+sensorId);
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
	
	/**
	 * Called by ApAM when the temperature value changed
	 * @param temp the new temperature
	 */
	public void temperatureChanged(String temp) {
		String newCelciusTemp = String.valueOf(getTemperature());
		logger.info("The temperature report by, "+ sensorId+" changed to "+newCelciusTemp);
		notifyChanges("value", newCelciusTemp);
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
		return new TemperatureNotificationMsg((float) (Float.valueOf(currentTemperature)/100.0), varName, value, this.getAbstractObjectId());
	}
	
	@Override
	public TemperatureUnit getTemperatureUnit() {
		return TemperatureUnit.Celsius;
	}

	@Override
	public float getTemperature() {
		int temp;
		byte[] b = null;
		b = wattecoAdapter.sendCommand(route, WattecoIOService.TEMPERATURE_MEASUREMENT_READ_ATTRIBUTE, true);
		Byte readByte = new Byte(b[8]);
		temp = (readByte << 8);
		readByte = new Byte(b[9]);
		temp += readByte;
		
		currentTemperature = String.valueOf(temp);
		return (float) (Float.valueOf(currentTemperature)/100.0);
	}

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
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", sensorId);
		descr.put("type", userType); //0 for temperature sensor
		descr.put("status", status);
		descr.put("value", currentTemperature);
		
		return descr;
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

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}

}
