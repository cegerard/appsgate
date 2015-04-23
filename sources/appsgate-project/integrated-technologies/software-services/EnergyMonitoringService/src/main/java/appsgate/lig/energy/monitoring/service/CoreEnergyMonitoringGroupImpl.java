/**
 * 
 */
package appsgate.lig.energy.monitoring.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.energy.monitoring.group.CoreEnergyMonitoringGroup;
import appsgate.lig.energy.monitoring.service.models.ActiveEnergySensor;

/**
 * @author thibaud
 *
 */
public class CoreEnergyMonitoringGroupImpl extends CoreObjectBehavior 
	implements CoreObjectSpec,
		CoreEnergyMonitoringGroup {
	
	public static final String IMPL_NAME = "CoreEnergyMonitoringGroupImpl";
	
	public static final String MSG_VARNAME_ACTIVEENERGY = "activeEnergy";
	public static final String MSG_VARNAME_ANOTHERTBD = "to be defined";


	private final static Logger logger = LoggerFactory.getLogger(CoreEnergyMonitoringGroupImpl.class);
	
    /**
     * CoreObject Stuff
     */
	private String serviceId;
	private String userType;
	private int status;
	
	/**
	 * This field should be injected at startup
	 */
	private String name;
	
	/**
	 * The String is the sensorID, the value holds the measures, index and state
	 */
	private Map<String, ActiveEnergySensor> sensors;
	double budgetTotal;
	double budgetUnit;
	ArrayList<String> periods;
	
	
	public final static String NAME_KEY = "name";
	public final static String SENSORS_KEY = "sensors";
	public final static String BUDGETTOTAL_KEY="budgetTotal";
	public final static String BUDGETUNIT_KEY="budgetUnit";
	public final static String BUDGETREMAINING_KEY="budgetRemaining";
	public final static String BUDGETRESETED_KEY="budgetReseted";
	
	public final static String PERIODS_KEY="periods";	
	
	
	public CoreEnergyMonitoringGroupImpl() {
    	userType = CoreEnergyMonitoringGroup.class.getSimpleName();
    	status = 2;
	}
	

	/**
	 * Callback when new apam Instance is created
	 */
	public void onInit() {
		logger.trace("onInit()");
		sensors = new HashMap<String, ActiveEnergySensor>();
	}
	
	public void configure(JSONArray sensors,
			double budgetTotal, double budgetUnit) {
		logger.trace("configure(JSONArray sensors : {}, "
				+ "double budgetTotal : {}, double budgetUnit : {})",
				sensors, budgetTotal, budgetUnit);
		setEnergySensorsGroup(sensors);
		setBudget(budgetTotal, budgetUnit);
		
		this.periods = new ArrayList<String>();
	}

	
	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		stateChanged(NAME_KEY, this.name, name);
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#getEnergySensorsGroup()
	 */
	@Override
	public JSONArray getEnergySensorsGroup() {
		return new JSONArray(sensors.keySet());
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#setEnergySensorsGroup(org.json.JSONArray)
	 */
	@Override
	public void setEnergySensorsGroup(JSONArray sensors) {
		logger.trace("setEnergySensorsGroup(JSONArray sensors : {})",sensors);
		this.sensors = new HashMap<String, ActiveEnergySensor>();
		for(int i = 0; sensors!= null
				&& i<sensors.length(); i++) {
			String s = sensors.optString(i);
			if(s!=null) {
				privateAddEnergySensor(s);
			}
		}
		stateChanged(SENSORS_KEY, null, getEnergySensorsGroup().toString());
	}
	
	private double getActiveEnergy(String sensorID) {
		double activeEnergy = 0;
		// TODO ask the real sensor
		
		return activeEnergy;
	}
	
	private synchronized void privateAddEnergySensor(String sensorID) {
		// TODO (we have 1° to check the period, and to 2° get the current measure of the sensor)
		long time = System.currentTimeMillis(); // TODO: maybe we should use the CoreClock Time ?
		
		sensors.put(sensorID, new ActiveEnergySensor(sensorID,
				getActiveEnergy(sensorID),
				checkPeriod(time)));
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#addEnergySensor(java.lang.String)
	 */
	@Override
	public void addEnergySensor(String sensorID) {
		logger.trace("addEnergySensor(String sensorID : {})", sensorID);

		privateAddEnergySensor(sensorID);
		stateChanged(SENSORS_KEY, null, getEnergySensorsGroup().toString());
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#removeEnergySensor(java.lang.String)
	 */
	@Override
	public void removeEnergySensor(String sensorID) {
		logger.trace("removeEnergySensor(String sensorID : {})", sensorID);
		sensors.remove(sensorID);		
		stateChanged(SENSORS_KEY, null, getEnergySensorsGroup().toString());
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#resetEnergy()
	 */
	@Override
	public void resetEnergy() {
		logger.trace("resetEnergy()");
		for(ActiveEnergySensor sensor : sensors.values()) {
			sensor.resetEnergy();
		}
		stateChanged(BUDGETRESETED_KEY, null, "");
		stateChanged(BUDGETREMAINING_KEY, String.valueOf(budgetTotal), String.valueOf(budgetTotal));

	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#getTotalEnergy()
	 */
	@Override
	public double getTotalEnergy() {
		double total = 0;
		for(ActiveEnergySensor sensor : sensors.values()) {
			total+=sensor.getTotalEnergy();
		}
		logger.trace("getTotalEnergy(), returning total: {} x unit : {}", total, budgetUnit);
		return total*budgetUnit;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#geEnergyDuringTimePeriod()
	 */
	@Override
	public double geEnergyDuringTimePeriod() {
		double total = 0;
		for(ActiveEnergySensor sensor : sensors.values()) {
			total+=sensor.getEnergyDuringPeriod();
		}
		logger.trace("getEnergyDuringPeriod(), returning energy: {} x unit : {}", total, budgetUnit);
		return total*budgetUnit;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#getRemainingBudget()
	 */
	@Override
	public double getRemainingBudget() {
		double total = geEnergyDuringTimePeriod();
		logger.trace("getRemainingBudget(), returning budgetTotal: {} - energyConsumed : {}", budgetTotal, total);
		return budgetTotal-total;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#getBudgetTotal()
	 */
	@Override
	public double getBudgetTotal() {
		logger.trace("getBudgetTotal(), returning budgetTotal: {} ", budgetTotal);
		return budgetTotal;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#getBudgetUnit()
	 */
	@Override
	public double getBudgetUnit() {
		logger.trace("getBudgetUnit(), returning budgetUnit: {} ", budgetUnit);
		return budgetUnit;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#setBudget(double, double)
	 */
	@Override
	public void setBudget(double budgetTotal, double budgetUnit) {
		logger.trace("setBudget(double budgetTotal : {}, double budgetUnit : {})", budgetTotal, budgetUnit);
		stateChanged(BUDGETTOTAL_KEY, String.valueOf(this.budgetTotal), String.valueOf(budgetTotal));
		stateChanged(BUDGETUNIT_KEY, String.valueOf(this.budgetUnit), String.valueOf(budgetUnit));
		this.budgetTotal = budgetTotal;
		this.budgetUnit = budgetUnit;
		
		// Changing budget sholud reset the energy counters
		resetEnergy();
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getAbstractObjectId()
	 */
	@Override
	public String getAbstractObjectId() {
		return serviceId;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getUserType()
	 */
	@Override
	public String getUserType() {
		return userType;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getObjectStatus()
	 */
	@Override
	public int getObjectStatus() {
		// This is a local service, it is always available
		return status;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getDescription()
	 */
	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", getAbstractObjectId());
		descr.put("type", getUserType());
		descr.put("coreType", getCoreType());
		descr.put("status", getObjectStatus());

		descr.put(NAME_KEY, getName());
		descr.put(SENSORS_KEY, getEnergySensorsGroup());
		descr.put(BUDGETTOTAL_KEY, getBudgetTotal());
		descr.put(BUDGETUNIT_KEY, getBudgetUnit());
		descr.put(PERIODS_KEY, getPeriods());

		return descr;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getCoreType()
	 */
	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SERVICE;
	}
	

	@Override
	public JSONArray getPeriods() {
		return new JSONArray(periods);
	}

	/**
	 * Check if the parameter time is inside a monitoring period
	 * @param time
	 * @return
	 */
	private boolean checkPeriod(long time) {
		// TODO check the periods in the scheduler
		return false;
	}	

	@Override
	public String addPeriod(long startDate, long endDate, boolean resetOnStart,
			boolean resetOnEnd) {
		// TODO we have to use the scheduler
		
		stateChanged(PERIODS_KEY, null, periods.toString());
		
		return null;
	}


	@Override
	public void removePeriodById(String eventID) {
		// TODO we have to use the scheduler
		stateChanged(PERIODS_KEY, null, periods.toString());
	}


	@Override
	public JSONObject getPeriodInfo(String eventID) {
		// TODO we have to use the scheduler
		return null;
	}
	
	private synchronized void addActiveEnergyMeasure(String sensorID, double value) {
		// Here is the core business function, 
		// 1° we have to compare the value with the previous one from this sensor (if any)
		// and accordingly start a new counter
		// 2° increase the total energy from this sensor
		// 3° check if we are in a monitoring period
		// and decrease the budget accordingly
		logger.trace("addActiveEnergyMeasure(String sensorID : {}, double value : {})",sensorID, value);
		
		long time = System.currentTimeMillis(); // TODO: maybe we should use the CoreClock Time ?
		
		sensors.get(sensorID).newEnergyMeasure(value, checkPeriod(time));
	}
	
	/**
	 * Every sensors that might trigger energy-related event should send message
	 * This handler filters
	 * 1° sensor providing event not in the current group (not optimal, we should only bind event provider from the group)
	 * 2° event is not related to energy consumption (not optimal, event should have been filtered above)
	 */
	@SuppressWarnings("unused")
	private void energyChangedEvent(NotificationMsg msg) {
		logger.trace("energyChangedEvent(NotificationMsg msg : {})",msg.JSONize());
		// Filter 0, basic filtering
		if(msg != null
				&& msg.getSource() != null
				&& msg.getVarName() != null
				&& msg.getNewValue() != null) {
			// Filter 1 sensor providing event not in the current group (not optimal, we should only bind event provider from the group)
			if(sensors.keySet().contains(msg.getSource())) {
				// Filter/routing 2 event is not related to energy consumption (not optimal, event should have been filtered above)
				if(msg.getVarName().equals(MSG_VARNAME_ACTIVEENERGY)) {
					try {
						double value = Double.parseDouble(msg.getNewValue());
						addActiveEnergyMeasure(msg.getSource(), value);
					} catch (NumberFormatException e) {
						logger.error("energyChangedEvent(..), value is not a double or float : ", e);
					} 
				} else if (msg.getVarName().equals(MSG_VARNAME_ANOTHERTBD)) {
					// If other category of energy measures from other devices types are used,
					// they should be added in this if/else structure
					logger.trace("energyChangedEvent(..), you should not be there");
				} else {
					logger.trace("energyChangedEvent(..), message varName is not managed for the moment");
				}
			} else {
				logger.trace("energyChangedEvent(..), sensor is not in this group");				
			}
		} else {
			logger.trace("energyChangedEvent(..), empty message or no source/varName/value specified");
		}		
	}
	
	
	private NotificationMsg stateChanged(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this.getAbstractObjectId());
	}
}
