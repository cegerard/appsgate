/**
 * 
 */
package appsgate.lig.energy.monitoring.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup;

/**
 * @author thibaud
 *
 */
public class CoreEnergyMonitoringGroupImpl extends CoreObjectBehavior 
	implements CoreObjectSpec,
		CoreEnergyMonitoringGroup {
	
	public static final String IMPL_NAME = "CoreEnergyMonitoringGroupImpl";

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
	
	private JSONArray sensors;
	double budgetTotal;
	double budgetUnit;
	JSONArray periods;
	
	
	public final static String NAME_KEY = "name";
	public final static String SENSORS_KEY = "sensors";
	public final static String BUDGETTOTAL_KEY="budgetTotal";
	public final static String BUDGETUNIT_KEY="budgetUnit";
	public final static String BUDGETREMAINING_KEY="budgetRemaining";
	
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
	}
	
	public void configure(JSONArray sensors,
			double budgetTotal, double budgetUnit, JSONArray periods) {
		logger.trace("configure(JSONArray sensors : {}, "
				+ "double budgetTotal : {}, double budgetUnit : {}, JSONArray periods : {},",
				sensors, budgetTotal, budgetUnit, periods);
		setEnergySensorsGroup(sensors);
		setBudget(budgetTotal, budgetUnit);
		
		this.periods = (periods==null? new JSONArray():periods);
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
		return sensors;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#setEnergySensorsGroup(org.json.JSONArray)
	 */
	@Override
	public void setEnergySensorsGroup(JSONArray sensors) {
		logger.trace("setEnergySensorsGroup(JSONArray sensors : {})",sensors);
		this.sensors = (sensors == null? new JSONArray():sensors);
		stateChanged(SENSORS_KEY, null, getEnergySensorsGroup().toString());
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#addEnergySensor(java.lang.String)
	 */
	@Override
	public void addEnergySensor(String sensorID) {
		// TODO Auto-generated method stub
		
		stateChanged(SENSORS_KEY, null, getEnergySensorsGroup().toString());
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#removeEnergySensor(java.lang.String)
	 */
	@Override
	public void removeEnergySensor(String sensorID) {
		// TODO Auto-generated method stub
		
		stateChanged(SENSORS_KEY, null, getEnergySensorsGroup().toString());
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#resetEnergy()
	 */
	@Override
	public void resetEnergy() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#getTotalEnergy()
	 */
	@Override
	public double getTotalEnergy() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#geEnergyDuringTimePeriod()
	 */
	@Override
	public double geEnergyDuringTimePeriod() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#getRemainingBudget()
	 */
	@Override
	public double getRemainingBudget() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#getBudgetTotal()
	 */
	@Override
	public double getBudgetTotal() {
		return budgetTotal;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.CoreEnergyMonitoringGroup#getBudgetUnit()
	 */
	@Override
	public double getBudgetUnit() {
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
		return periods;
	}

	@Override
	public void addPeriod(JSONObject period) {
		// TODO Auto-generated method stub
		
		stateChanged(PERIODS_KEY, null, periods.toString());

		
	}

	@Override
	public void removePeriodAtIndex(int index) {
		// TODO Auto-generated method stub
		
		stateChanged(PERIODS_KEY, null, periods.toString());
	}	
	
	private NotificationMsg stateChanged(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this.getAbstractObjectId());
	}



}
