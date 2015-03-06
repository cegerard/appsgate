package appsgate.lig.smartplug.sensor.watteco.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.smartplug.actuator_sensor.messages.SmartPlugNotificationMsg;
import appsgate.lig.smartplug.actuator_sensor.spec.CoreSmartPlugSpec;
import appsgate.lig.watteco.adapter.spec.WattecoIOService;

/**
 * 
 * @author Cédric Gérard
 *
 */
public class WattecoSmartPlugImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreSmartPlugSpec, SmartPlugServices {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(WattecoSmartPlugImpl.class);
	
	private String sensorName;
	private String sensorId;
	private String sensoreType;
	private String userType;
	private String status;
	private String isPaired;
	private String route;
	
	private String plugState;
	
	private String consumption;
	private String activeEnergy;
	
	private SmartPlugValue spv;
	
	/** the main border router */
	WattecoIOService wattecoAdapter;
	
	
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
		wattecoAdapter.sendCommand(route, WattecoIOService.ON_OFF_TOGGLE, false);
	}
	
	@Override
	public void on() {
		wattecoAdapter.sendCommand(route, WattecoIOService.ON_OFF_ON, false);
	}

	@Override
	public void off() {
		wattecoAdapter.sendCommand(route, WattecoIOService.ON_OFF_OFF, false);
	}
	
	@Override
	public int activePower() {
		return readAttribute().activePower;
	}

	@Override
	public int activeEnergy() {
		return readAttribute().activeEnergy;
	}
	
	@Override
	public boolean getRelayState() {
		byte[] b = wattecoAdapter.sendCommand(route, WattecoIOService.ON_OFF_READ_ATTRIBUTE, true);
		boolean state = extractState(b);
		plugState = Boolean.toString(state);
		return state;
	}

	@Override
	public SmartPlugValue readAttribute() {
		byte[] b = null;
		b = wattecoAdapter.sendCommand(route, WattecoIOService.SIMPLE_METERING_READ_ATTRIBUTE, true);
		spv = extractValues(b);
		consumption = String.valueOf(spv.activePower);
		activeEnergy = String.valueOf(spv.activeEnergy);
		return spv;
	}
	
	/* ***********************************************************************
	 * 							PRIVATE FUNCTIONS                            *
	 *********************************************************************** */
	
	private SmartPlugValue extractValues(byte[] ba) {
		// TODO: unsure, doc inaccurate
		SmartPlugValue spv = new SmartPlugValue();
		Byte b;
		
		// calculation of the 'summation of the active energy in W.h'
		b = new Byte(ba[9]);
		spv.activeEnergy  = (b << 16);
		b = new Byte(ba[10]);
		spv.activeEnergy += (b << 8);
		b = new Byte(ba[11]);
		spv.activeEnergy += b;
		
		// calculation of the 'number of sample'
		b = new Byte(ba[15]);
		spv.nbOfSamples  = (b << 8);
		b = new Byte(ba[16]);
		spv.nbOfSamples += b;
		
		// calculation of the 'active power in W'
		b = new Byte(ba[17]);
		spv.activePower = (b << 8);
		b = new Byte(ba[18]);
		spv.activePower += b;
		
		return spv;
	}
	
	private boolean extractState(byte[] ba) {
		return (ba[8] == new Byte("1").byteValue());
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
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", sensorId);
		descr.put("type", userType); //6 for SmartPlug sensor
		descr.put("status", status);
		descr.put("plugState", plugState);
		descr.put("consumption", consumption);
		descr.put("activeEnergy", activeEnergy);
		
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
