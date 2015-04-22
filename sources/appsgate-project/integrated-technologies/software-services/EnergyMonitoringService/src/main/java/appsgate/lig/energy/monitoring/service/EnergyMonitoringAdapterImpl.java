/**
 * 
 */
package appsgate.lig.energy.monitoring.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.energy.monitoring.EnergyMonitoringAdapter;

/**
 * @author thibaud
 *
 */
public class EnergyMonitoringAdapterImpl extends CoreObjectBehavior implements
		EnergyMonitoringAdapter, CoreObjectSpec {
	
	public static final String ADDED_GROUP = "added";
	public static final String REMOVED_GROUP = "removed";
	
    /**
     * CoreObject Stuff
     */
	private String serviceId;
	private String userType;
	private int status;
	
	private final static Logger logger = LoggerFactory.getLogger(EnergyMonitoringAdapterImpl.class);
	
	public EnergyMonitoringAdapterImpl() {
    	serviceId = this.getClass().getName()+"-"+this.hashCode();
    	userType = EnergyMonitoringAdapter.class.getSimpleName();
    	status = 2;
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

		return descr;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.core.object.spec.CoreObjectSpec#getCoreType()
	 */
	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SERVICE;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.EnergyMonitoringAdapter#createGroup(java.lang.String, org.json.JSONArray, double, double, long, long, long, long)
	 */
	@Override
	public String createGroup(String groupName, JSONArray sensors,
			double budgetTotal, double budgetUnit) {
		logger.trace("createGroup(String groupName : {}, JSONArray sensors : {}, "
				+ "double budgetTotal : {}, double budgetUnit : {})",
				groupName, sensors, budgetTotal, budgetUnit);

		Implementation implem = CST.apamResolver.findImplByName(null,CoreEnergyMonitoringGroupImpl.IMPL_NAME);
		if(implem == null) {
			logger.error("createGroup(...) Unable to get APAM Implementation"); 
			return null;
		}
		logger.trace("createGroup(), implem found");
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("groupName", groupName);
		properties.put("instance.name", CoreEnergyMonitoringGroupImpl.class.getName()
				+"-"+groupName
				+"-"+UUID.randomUUID());
		
		Instance inst = implem.createInstance(null, properties);
		if(inst == null) {
			logger.error("createGroup(...) Unable to create APAM Instance"); 			
			return null;
		}
		
		CoreEnergyMonitoringGroupImpl group = (CoreEnergyMonitoringGroupImpl)inst.getServiceObject();
		if(group == null) {
			logger.error("createGroup(...) Unable to get Service Object"); 			
			return null;
		}
		group.configure(sensors, budgetTotal, budgetUnit);		

		stateChanged(ADDED_GROUP, null, group.getAbstractObjectId());
		return group.getAbstractObjectId();
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.EnergyMonitoringAdapter#createEmptyGroup(java.lang.String)
	 */
	@Override
	public String createEmptyGroup(String groupName) {
		// Created a group with default values
		// -1 budget -> no budget defined
		// 1 budget unit -> default value

		logger.trace("createEmptyGroup(String groupName : {})", groupName);
		return createGroup(groupName, new JSONArray(), -1, 1);
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.EnergyMonitoringAdapter#removeGroup(java.lang.String)
	 */
	@Override
	public void removeGroup(String groupID) {
		logger.trace("removeGroup(String groupID : {})", groupID);
		
		Instance inst = CST.apamResolver.findInstByName(null, groupID);
		if(inst == null) {
			logger.error("removeGroup(...) Unable to find APAM Instance"); 			
			return;
		}
		((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(inst);
				
		stateChanged(REMOVED_GROUP, null, groupID);
	}
	
	private NotificationMsg stateChanged(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this.getAbstractObjectId());
	}	

}
