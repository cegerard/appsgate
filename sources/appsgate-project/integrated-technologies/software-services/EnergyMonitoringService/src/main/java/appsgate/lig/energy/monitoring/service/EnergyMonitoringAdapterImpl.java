/**
 * 
 */
package appsgate.lig.energy.monitoring.service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

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
import appsgate.lig.energy.monitoring.adapter.EnergyMonitoringAdapter;
import appsgate.lig.persistence.DBHelper;
import appsgate.lig.persistence.MongoDBConfiguration;

/**
 * @author thibaud
 *
 */
public class EnergyMonitoringAdapterImpl extends CoreObjectBehavior implements
		EnergyMonitoringAdapter, CoreObjectSpec, Runnable {
	
	public static final String ADDED_GROUP = "energyGroupAdded";
	public static final String REMOVED_GROUP = "energyGroupRemoved";
	
	MongoDBConfiguration dbConfig;
	boolean dbBound = false;
	
    /**
     * CoreObject Stuff
     */
	private String serviceId;
	private String userType;
	private int status;
		
	private final static Logger logger = LoggerFactory.getLogger(EnergyMonitoringAdapterImpl.class);
	
	public EnergyMonitoringAdapterImpl() {
    	serviceId = this.getClass().getSimpleName();// Do no need any hashcode or UUID, this service should be a singleton
    	userType = EnergyMonitoringAdapter.class.getSimpleName();
    	status = 2;
    	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    	executor.scheduleAtFixedRate(this, 5, 60*5, TimeUnit.SECONDS);
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
	
	private static final SecureRandom idGenerator= new SecureRandom();
	
	/**
	 * Helper method to generate a short and unique ID (UUID are too long to be friendly) 
	 * These might no bee unique
	 * @return
	 */
	public String  generateInstanceID() {
		byte[] id = new byte[8];
		idGenerator.nextBytes(id);
		String result = DatatypeConverter.printBase64Binary(id);
		
		return result.substring(0, result.indexOf('='));
		
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
		
		if(!dbBound()) {
			logger.warn("No Database bound, cannot guarantee the group configuration will be saved in the database,"
					+ " Instance will not be created  (restart DB first)");
			return null;
		}		

		String instanceName = CoreEnergyMonitoringGroupImpl.class.getSimpleName()
				+"-"+generateInstanceID();
		CoreEnergyMonitoringGroupImpl group = createApamComponent(groupName,instanceName);
				
		if(group == null) {
			logger.error("createGroup(...) Unable to get Service Object"); 			
			return null;
		}
		group.configureNew(sensors, budgetTotal, budgetUnit);
		storeInstanceConfiguration(group.getDescription());

		stateChanged(ADDED_GROUP, null, group.getAbstractObjectId());
		return group.getAbstractObjectId();
	}
	
	/**
	 * Helper method that use ApAM api to create the component
	 * @param groupName
	 * @return
	 */
	private CoreEnergyMonitoringGroupImpl createApamComponent(String groupName, String InstanceName) {
		Implementation implem = CST.apamResolver.findImplByName(null,CoreEnergyMonitoringGroupImpl.IMPL_NAME);
		if(implem == null) {
			logger.error("createApamComponent(...) Unable to get APAM Implementation"); 
			return null;
		}
		logger.trace("createGroup(), implem found");
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("groupName", groupName);
		properties.put("instance.name",InstanceName);
		
		Instance inst = implem.createInstance(null, properties);

		if(inst == null) {
			logger.error("createApamComponent(...) Unable to create APAM Instance"); 			
			return null;
		}
		
		return (CoreEnergyMonitoringGroupImpl)inst.getServiceObject();
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
		if(!dbBound()) {
			logger.warn("No Database bound, cannot guarantee the group will be removed from the database,"
					+ " Running Instance will not be destroyed (restart DB first)");
			return;
		}
		
		Instance inst = CST.apamResolver.findInstByName(null, groupID);
		if(inst == null) {
			logger.error("removeGroup(...) Unable to find APAM Instance"); 			
			return;
		}
		((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(inst);
		removeInstanceConfiguration(groupID);
				
		stateChanged(REMOVED_GROUP, null, groupID);
	}
	
	private NotificationMsg stateChanged(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this.getAbstractObjectId());
	}
	
	String dbName = CoreObjectSpec.DBNAME_DEFAULT;
	String dbCollectionName = this.getClass().getSimpleName();
	DBHelper dbHelper = null;

	
	/**
	 * When db is bound look for the EnergyGroup stored
	 * and restore the missing ones.
	 */
	private boolean dbBound() {
		logger.trace("dbBound()");
		
		// The first time, we will synchro all groups in the DB, and after, return shortly
		if(dbBound) return true;
		
		if(dbConfig == null
				||!dbConfig.isValid()) {
			logger.warn("dbBound(), dbConfig unavailable");
			dbUnbound();
			
		} else {		
			dbHelper = dbConfig.getDBHelper(dbName, dbCollectionName);
			if(dbHelper == null
					|| dbHelper.getJSONEntries() == null) {
				logger.warn("dbBound(), dbHelper unavailable");
				dbUnbound();
			} else {
				// Synchro DB => running Instances
				Set<JSONObject> entries = dbHelper.getJSONEntries();
				for(JSONObject entry : entries) {
					if( CST.componentBroker.getInst(entry.getString("id")) == null) {
						logger.trace("dbBound(), no running instance with same id : {}, creating one with description : {}",
								entry.getString("id"),
								entry);
						CoreEnergyMonitoringGroupImpl group = createApamComponent(entry.getString("name"),entry.getString("id"));
						group.configureFromJSON(entry);
					} else {
						logger.trace("dbBound(), already an instance with id : {}, keeping the existing one", entry.getString("id"));						
					}		
				}
				dbBound = true;				
			}
		}
		return dbBound;
	}
	
	/**
	 * when no db is available stop refuse to create/remove Energy Groups as they won't be stored
	 */
	private void dbUnbound() {
		logger.trace("dbUnbound()");
		dbHelper = null;
		dbBound = false;

	}
	
	private void storeInstanceConfiguration(JSONObject serviceDescription) {
		logger.trace("storeInstanceConfiguration(JSONObject serviceDescription : {})", serviceDescription);
		if(dbBound 
				&& dbHelper!= null) {
			serviceDescription.put(DBHelper.ENTRY_ID, serviceDescription.getString("id"));
			boolean result = dbHelper.insertJSON(serviceDescription);
			if(result) {
				logger.trace("storeInstanceConfiguration(...), group successfully inserted/updated in the database");
			} else {
				logger.error("storeInstanceConfiguration(...), group not inserted in the database");
			}
		}
	}
	
	private void removeInstanceConfiguration(String serviceId) {
		logger.trace("removeInstanceConfiguration(String serviceId : {})", serviceId);
		if(dbBound 
				&& dbHelper!= null) {
			boolean result = dbHelper.remove(serviceId);
			if(result) {
				logger.trace("storeInstanceConfiguration(...), group successfully removed from the database");
			} else {
				logger.error("storeInstanceConfiguration(...), group not removed from the database");
			}			
		}
	}

	@Override
	public void run() {
		logger.trace("run()");
		if(dbBound()) {
			// TODO synchro running instance => DB 
			
		}		
	}	
	

}
