/**
 * 
 */
package appsgate.lig.energy.monitoring.service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
		
	public static final String MSG_VARNAME_ACTIVEENERGY = "activeEnergy";
	public static final String MSG_VARNAME_ANOTHERTBD = "to be defined";
	
	Map<String, CoreEnergyMonitoringGroupImpl> instances;

	
	
		
	private final static Logger logger = LoggerFactory.getLogger(EnergyMonitoringAdapterImpl.class);
	
	public EnergyMonitoringAdapterImpl() {
    	serviceId = this.getClass().getSimpleName();// Do no need any hashcode or UUID, this service should be a singleton
    	userType = EnergyMonitoringAdapter.class.getSimpleName();
    	status = 2;
    	instances = new HashMap<String, CoreEnergyMonitoringGroupImpl>();
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
	
	private static final Random idGenerator= new Random(System.currentTimeMillis());
	private static int counter = 0;
	
	/**
	 * Helper method to generate a short and unique ID (UUID are too long to be friendly) 
	 * These might no bee unique
	 * @return
	 */
	public String  generateInstanceID(int size) {
		int i = 0;
		char[] tab = new char[size];
		char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9',
				'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
				'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
		
		tab[i++] = digits[counter%digits.length];
		counter++;
		while (i<size) {
			tab[i++]= digits[idGenerator.nextInt(digits.length)];
		}
		return new String(tab);		
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.EnergyMonitoringAdapter#createGroup(java.lang.String, org.json.JSONArray, double, double, long, long, long, long)
	 */
	@Override
	public String createEnergyMonitoringGroup(String groupName, JSONArray sensors,
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
				+"-"+generateInstanceID(8);
		CoreEnergyMonitoringGroupImpl group = createApamComponent(groupName,instanceName);
				
		if(group == null) {
			logger.error("createGroup(...) Unable to get Service Object"); 			
			return null;
		}
		group.configureNew(sensors, budgetTotal, budgetUnit);
		storeInstanceConfiguration(group.getDescription());
		instances.put(instanceName, group);

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
	public String createEnergyMonitoringEmptyGroup(String groupName) {
		// Created a group with default values
		// -1 budget -> no budget defined
		// 1 budget unit -> default value

		logger.trace("createEmptyGroup(String groupName : {})", groupName);
		return createEnergyMonitoringGroup(groupName, new JSONArray(), -1, 1);
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.energy.monitoring.EnergyMonitoringAdapter#removeGroup(java.lang.String)
	 */
	@Override
	public void removeEnergyMonitoringGroup(String groupID) {
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
		
		instances.remove(groupID);
		((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(groupID);
		removeInstanceConfiguration(groupID);
				
		stateChanged(REMOVED_GROUP, null, groupID);
	}
	
	private NotificationMsg stateChanged(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this.getAbstractObjectId());
	}
	
	String dbName = CoreObjectSpec.DBNAME_DEFAULT;
	String dbCollectionNameGroups = this.getClass().getSimpleName()+"Groups";
	String dbCollectionNameSensors = this.getClass().getSimpleName()+"Sensors";
	DBHelper dbHelperGroups = null;
	DBHelper dbHelperSensors = null;

	
	/**
	 * When db is bound look for the EnergyGroup stored
	 * and restore the missing ones.
	 */
	private synchronized boolean dbBound() {
		logger.trace("dbBound()");
		
		// The first time, we will synchro all groups in the DB, and after, return shortly
		if(dbBound) return true;
		
		if(dbConfig == null
				||!dbConfig.isValid()) {
			logger.warn("dbBound(), dbConfig unavailable");
			dbUnbound();
			
		} else {
			
			logger.trace("dbBound(), restoring Sensors");
			dbHelperSensors = dbConfig.getDBHelper(dbName, dbCollectionNameSensors);
			if(dbHelperSensors == null
					|| dbHelperSensors.getJSONEntries() == null) {
				logger.warn("dbBound(), dbHelper unavailable for Sensors");
				dbUnbound();
				return false;
			} else {
				// Synchro DB => sensors index pool
				Set<JSONObject> entries = dbHelperSensors.getJSONEntries();
				for(JSONObject entry : entries) {
					// the DB key _id must be used for sensor Id
					String sensorId = entry.get(DBHelper.ENTRY_ID).toString();
					if( sensorId != null ) {
						String energyIndex = entry.optString(sensorId,"-1");
						logger.trace("dbBound(), restoring sensor id : {}, with energyIndex : {}",sensorId, energyIndex);
						try {
							EnergySensorPool.getInstance().addEnergyMeasure(sensorId, Double.parseDouble(energyIndex));
						} catch (NumberFormatException e) {
							logger.error("dbBound(), error when parsing energy index value : ",e);
						}
					} else {
						logger.trace("dbBound(), already sensor with id : {}, does not restore the one in the db");						
					}		
				}
			}
			
			
			logger.trace("dbBound(), restoring Groups");
			dbHelperGroups = dbConfig.getDBHelper(dbName, dbCollectionNameGroups);
			if(dbHelperGroups == null
					|| dbHelperGroups.getJSONEntries() == null) {
				logger.warn("dbBound(), dbHelper unavailable for Groups");
				dbUnbound();
				return false;
			} else {
				// Synchro DB => running Instances
				Set<JSONObject> entries = dbHelperGroups.getJSONEntries();
				for(JSONObject entry : entries) {
					String id = entry.getString("id");
					if( id != null && CST.componentBroker.getInst(id) == null) {
						logger.trace("dbBound(), no running instance with same id : {}, creating one with description : {}",
								id,
								entry);
						CoreEnergyMonitoringGroupImpl group = createApamComponent(entry.getString("name"),id);
						group.configureFromJSON(entry);
						instances.put(id, group);						
					} else {
						logger.trace("dbBound(), already an instance with id : {}, keeping the existing one", entry.getString("id"));						
					}		
				}
			}
			logger.trace("dbBound(), finished restoring sensors and groups");			
			dbBound = true;
		}
		return dbBound;
	}
	
	/**
	 * when no db is available stop refuse to create/remove Energy Groups as they won't be stored
	 */
	private void dbUnbound() {
		logger.trace("dbUnbound()");
		dbHelperGroups = null;
		dbHelperSensors = null;

		dbBound = false;
	}
	
	private void storeInstanceConfiguration(JSONObject serviceDescription) {
		logger.trace("storeInstanceConfiguration(JSONObject serviceDescription : {})", serviceDescription);
		if(dbBound 
				&& dbHelperGroups!= null) {
			serviceDescription.put(DBHelper.ENTRY_ID, serviceDescription.getString("id"));
			boolean result = dbHelperGroups.insertJSON(serviceDescription);
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
				&& dbHelperGroups!= null) {
			boolean result = dbHelperGroups.remove(serviceId);
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
			//  synchro running instance => DB no more needed (each event is directly stored)
			
		}		
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
			// Filter/routing 1 event is not related to energy consumption (not optimal, event should have been filtered above)
			if(msg.getVarName().equals(MSG_VARNAME_ACTIVEENERGY)) {
				try {
					double value = Double.parseDouble(msg.getNewValue());
					String sensorId= msg.getSource();
					logger.trace("energyChangedEvent(..), sensorID: {}, value: {}. Updating the pool",
							sensorId, value);
					EnergySensorPool.getInstance().addEnergyMeasure(sensorId,
							value);

					
					if(dbBound() && dbHelperSensors != null) {
						logger.trace("energyChangedEvent(..), updating the database (sensor pool)");
						
						dbHelperSensors.insertJSON(new JSONObject()
						.put(DBHelper.ENTRY_ID, sensorId)
						.put(sensorId, String.valueOf(value)));
					}
					
					logger.trace("energyChangedEvent(..), notifying groups");
					for(CoreEnergyMonitoringGroupImpl group : instances.values()) {
						if(group.getEnergySensorsGroupAsSet().contains(sensorId)) {
							logger.trace("energyChangedEvent(..), updating the group id: {}, name: {}",
									group.getAbstractObjectId(),
									group.getName());

							group.energyChanged(sensorId);
							if(dbBound() && dbHelperGroups != null) {
								logger.trace("energyChangedEvent(..), updating the database (group configuration)");
								storeInstanceConfiguration(group.getDescription());
							}							
						}
					}
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
			logger.trace("energyChangedEvent(..), empty message or no source/varName/value specified");
		}		
	}
}
