package appsgate.lig.occupancy.sensor.watteco.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.occupancy.sensor.messages.OccupancyNotificationMsg;
import appsgate.lig.occupancy.sensor.spec.CoreOccupancySpec;
import appsgate.lig.watteco.adapter.spec.WattecoIOService;

public class WattecoOccupancyImpl implements CoreObjectSpec, CoreOccupancySpec {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(WattecoOccupancyImpl.class);
	
	private String sensorName;
	private String sensorId;
	private String sensoreType;
	private String pictureId;
	private String userType;
	private String status;
	private String isPaired;
	private String route;
	
	private String occupied;
	
	/** the main border router */
	WattecoIOService wattecoAdapter;

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New occupancy sensor detected, "+sensorId);
		setSensorName("Occupancy-"+sensorId);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("Occupancy sensor desapeared, "+sensorId);
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
	
	public void occupiedChanged(String occupied) {
		logger.info("The occupancy sensor, "+ sensorId+" changed to "+occupied);
		notifyChanges("occupied", occupied);
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
		return new OccupancyNotificationMsg(this, Boolean.valueOf(occupied), varName, value);
	}
	
	@Override
	public boolean getOccupied() {
		byte[] b = wattecoAdapter.sendCommand(route, WattecoIOService.OCCUPANCY_SENSING_READ_ATTRIBUTE, true);
		return (new Byte("1").byteValue() == b[b.length-1]);
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
		descr.put("type", userType); //5 for motion sensor
		descr.put("status", status);
		descr.put("occupied", occupied);
		
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

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}

}
