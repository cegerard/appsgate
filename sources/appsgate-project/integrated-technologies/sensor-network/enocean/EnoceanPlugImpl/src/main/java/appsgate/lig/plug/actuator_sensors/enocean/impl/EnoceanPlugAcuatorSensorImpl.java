package appsgate.lig.plug.actuator_sensors.enocean.impl;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.enocean.ubikit.adapter.spec.UbikitAdapterService;
import appsgate.lig.smartplug.actuator_sensor.messages.SmartPlugNotificationMsg;
import appsgate.lig.smartplug.actuator_sensor.spec.CoreSmartPlugSpec;

public class EnoceanPlugAcuatorSensorImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreSmartPlugSpec {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(EnoceanPlugAcuatorSensorImpl.class);
	
	private String sensorName;
	private String sensorId;
	private String sensoreType;
	private String userType;
	private String status;
	private String isPaired;
	/**
	 * Hold the last signal strength in DBM
	 */
	private String signal;
	private String plugState;
	
	private String consumption;  //In Watt
	private String activeEnergy; //In Watt.s
	private String lastRequest;  //Date
	
	private float[] metering = {(float) 0.0, (float) 0.0};
	private long[] date = {0, 1};
	private Timer timer = new Timer();
	private TimerTask meteringAction;
     
	
	/**
	 * EnOcean proxy service uses to validate the sensor configuration with the
	 * EnOcean proxy (pairing phase)
	 */
	UbikitAdapterService enoceanProxy;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New smart plug sensor detected, "+sensorId);
		setSensorName("SmartPlug-"+sensorId);
		meteringAction = new meteringTask();
		timer.scheduleAtFixedRate(meteringAction, 5000, 600000); //Schedule metering task to 5 seconds and every 10 minutes
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
	 * Called by ApAM when the signal strength changed
	 * @param newSignalValue the new singal value
	 */
	public void signalChanged(String newSignalValue) {
		logger.info(newSignalValue+" dbm signal strength for "+sensorId);
		notifyChanges("signal", newSignalValue);
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
		

		if(plugState.contentEquals("true")) {
			meteringAction.cancel();
			timer.purge();
			meteringAction = new meteringTask();
			timer.scheduleAtFixedRate(meteringAction, 0, 10000);
		}else {
			meteringAction.cancel();
			timer.purge();
			consumption = "0";
		}
	}
	
	public void consumptionChanged(String consumption) {
		logger.info("The sensor, "+ sensorId+" consumption changed to "+consumption);
		notifyChanges("consumption", consumption);
	}
	
	public void activeEnergyChanged(String activeEnergy) {
		logger.info("The sensor, "+ sensorId+" activeEnergy changed to "+activeEnergy);
		notifyChanges("activeEnergy", activeEnergy); //Active Energy message reactivated because used for energy consumption
		addValue(new Float(activeEnergy), new Long(lastRequest));
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
		return new SmartPlugNotificationMsg(this.getAbstractObjectId(), varName, value);
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
		activeEnergy();
	}

	@Override
	public void off() {
		enoceanProxy.turnOffActuator(sensorId);
	}

	@Override
	public int activePower() {
		return new Float(consumption).intValue();
	}

	@Override
	public int activeEnergy() {
		enoceanProxy.sendActuatorUpdateEvent(sensorId);
		return new Float(activeEnergy).intValue();
	}
	
	/**
	 * add the value to a tab that allow the instance to calculate it's
	 * real time consumption
	 * 
	 * @param activeEnergy the consumption in Watt.s
	 * @param date the time stamp
	 */
	public void addValue(float activeEnergy, long newDate){
		Float cons;
		if(date[1] < date[0]) {
			date[1] = newDate;
			metering[1] = activeEnergy;
			cons = new Float(1000*(metering[1]-metering[0])/(date[1]-date[0]));
			
		} else {
			date[0] = newDate;
			metering[0] = activeEnergy;
			cons = new Float(1000*(metering[0]-metering[1])/(date[0]-date[1]));
		}
		
		float diff = cons-Float.valueOf(consumption);
		if(diff > 0 || Math.abs(diff) >= 1.5){
			consumption = String.valueOf(Math.round(cons));
		} 		
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
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", sensorId);
		descr.put("type", userType); //6 for SmartPlug sensor
		descr.put("status", status);
		descr.put("plugState", plugState);
		descr.put("consumption", consumption);
		descr.put("activeEnergy", activeEnergy);
		descr.put("deviceType", sensoreType);
		
		return descr;
	}

	public boolean isPaired() {
		return Boolean.valueOf(isPaired);
	}

	public void setPaired(boolean isPaired) {
		this.isPaired = String.valueOf(isPaired);
	}
	
	public String getSignal() {
		return signal;
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
	
	
	class meteringTask extends TimerTask {
        public void run() {
        	activeEnergy();
        }
    }


	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}
}
