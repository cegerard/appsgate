package appsgate.lig.co2.sensor.watteco.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.co2.sensor.messages.Co2NotificationMsg;
import appsgate.lig.co2.sensor.spec.CoreCO2SensorSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.watteco.adapter.WattecoAdapter;
import appsgate.lig.watteco.adapter.services.WattecoIOService;

/**
 * 
 * @author Cédric Gérard
 *
 */
public class WattecoCO2Impl implements CoreObjectSpec, CoreCO2SensorSpec {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(WattecoCO2Impl.class);
	
	private String sensorName;
	private String sensorId;
	private String sensoreType;
	private String pictureId;
	private String userType;
	private String status;
	private String isPaired;
	private String route;
	
	private String currentCO2Concentration;
	
	/** the main border router */
	WattecoIOService wattecoAdapter;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New CO2 sensor detected, "+sensorId);
		setSensorName("CO2-"+sensorId);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("CO2 sensor desapeared, "+sensorId);
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
	 * Called by ApAM when the CO2 concentration value changed
	 * @param CO2 the new CO2 concentration
	 */
	public void CO2Changed(String co2) {
		String newC02Concentration = String.valueOf(getCO2Concentration());
		logger.info("The CO2 concentration reported by, "+ sensorId+" changed to "+newC02Concentration);
		notifyChanges("value", newC02Concentration);
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
		return new Co2NotificationMsg(Integer.valueOf(currentCO2Concentration), varName, value, this);
	}

	@Override
	public int getCO2Concentration() {
		int temp;
		byte[] b = null;
		b = wattecoAdapter.sendCommand(route, WattecoAdapter.ANALOG_INPUT_READ_ATTRIBUTE, true);
		Byte readByte = new Byte(b[8]);
		temp = (readByte << 32);
		readByte = new Byte(b[9]);
		temp += (readByte << 16);
		readByte = new Byte(b[10]);
		temp += (readByte << 8);
		readByte = new Byte(b[11]);
		temp += readByte;
		
		currentCO2Concentration = String.valueOf(temp);
		return Integer.valueOf(currentCO2Concentration);
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
	public String getPictureId() {
		return pictureId;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", sensorId);
		descr.put("type", userType); //9 for CO2 sensor
		descr.put("status", status);
		descr.put("value", String.valueOf(getCO2Concentration()));
		
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
