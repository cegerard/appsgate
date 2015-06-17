package appsgate.lig.fairylights.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;

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
import appsgate.lig.fairylights.CoreFairyLightsSpec;
import appsgate.lig.fairylights.service.FairyLightsImpl;
import appsgate.lig.persistence.DBHelper;
import appsgate.lig.persistence.MongoDBConfiguration;


/**
 * Base implementation of the FairyLightsAdapter,
 * responsible for discovery of the FairyLightDevices and creation/update/removal of FairyLights groups
 */
public class FairyLightsAdapterImpl extends CoreObjectBehavior implements CoreObjectSpec,
FairyLightsAdapterSpec, FairyLightsStatusListener {
	
	private static Logger logger = LoggerFactory.getLogger(FairyLightsAdapterImpl.class);
	
	MongoDBConfiguration dbConfig;
	boolean dbBound = false;
	
	String host=null;
	int port = -1;
	
	Map<String, CoreFairyLightsSpec> instances;
	JSONArray currentPattern;
		
	LightManagement lightManager;

	
	//Apsgate Properties for CoreObjectSpec
	public static final String userType = FairyLightsAdapterSpec.class.getSimpleName();	
	int coreObjectStatus = 0;
	String coreObjectId;
	
	public static final String DEFAULT_HOST = "guirlande.local";	
	public static final String DEFAULT_PROTOCOL = "http://";
	public static final String LUMIPIXEL_API_URL = "/lumipixel/api/v1.0/leds";
	public static final String URL_SEP = "/";

	public static final String KEY_COLOR = "color";
	public static final String KEY_LEDS = "leds";
	public static final String KEY_ID = "id";
	
	DiscoveryHeartBeat discoveryHeartBeat;
	public static final long DISCOVERYPERIOD = 5*60*1000;
	
	Timer timer;
	boolean deviceAvailable = false;
	
	public FairyLightsAdapterImpl() {
		logger.trace("FairyLightsAdapterImpl(), default constructor");
		lightManager = LightManagement.getInstance();
		discoveryHeartBeat=new DiscoveryHeartBeat(this, DEFAULT_PROTOCOL, DEFAULT_HOST, LUMIPIXEL_API_URL);
		currentPattern = new JSONArray();
		
		coreObjectId = this.getClass().getSimpleName();// Do no need any hashcode or UUID, this service should be a singleton
    	coreObjectStatus = 2;
    	instances = new HashMap<String, CoreFairyLightsSpec>();
		
		timer = new Timer();
		timer.schedule(discoveryHeartBeat, 10000, DISCOVERYPERIOD);
	}
	
	@Override
	public String createFreeformLightsGroup(String name, JSONArray selectedLights) {		
		logger.trace("createFreeformLightsGroup(String name : {}, JSONArray selectedLights : {})",
				name, selectedLights);
		
		if(!dbBound()) {
			logger.warn("No Database bound, cannot guarantee the group configuration will be saved in the database,"
					+ " Instance will not be created  (restart DB first)");
			return null;
		}		
		
		String instanceName;
		if(LightManagement.GROUP_ALL_ID.equals(name)) {
			// special case for the group all, the id is the same as the name
			instanceName = LightManagement.GROUP_ALL_ID;
		} else {
			instanceName = FairyLightsImpl.class.getSimpleName()
					+"-"+generateInstanceID(8);
		}

		FairyLightsImpl group = (FairyLightsImpl)createApamComponent(name,instanceName);
				
		if(group == null) {
			logger.error("createFreeformLightsGroup(...) Unable to get Service Object"); 			
			return null;
		}
		
		JSONObject config = new JSONObject();
		config.put(FairyLightsImpl.KEY_LEDS, selectedLights);
		// Handle to retrieve the current light Number and current Color
		
		group.configure(lightManager, config);
		storeInstanceConfiguration(group.getDescription());
		instances.put(instanceName, group);

		return group.getAbstractObjectId();
	}

	@Override
	public String createContiguousLightsGroup(String name, int startingIndex, int endingIndex) {
		logger.trace("createContiguousLightsGroup(String name : {}, int startingIndex : {}, int endingIndex : {})",
				name, startingIndex, endingIndex);		
		if(endingIndex<startingIndex) {
			int tmp = startingIndex;
			startingIndex = endingIndex;
			endingIndex = tmp;
		}
		
		JSONArray selected= new JSONArray();
		for(int i=startingIndex; i<=endingIndex; i++) {
			selected.put(new JSONObject().put(FairyLightsImpl.KEY_ID, i));
		}
		return createFreeformLightsGroup(name, selected);
	}

	@Override
	public void updateLightsGroup(String groupId, JSONArray selectedLights) {
		logger.trace("updateLightsGroup(String groupId : {}, JSONArray selectedLights : {})", groupId, selectedLights);

		// TODO Auto-generated method stub
		
	}

	@Override
	public void releaseLight(int lightNumber) {
		logger.trace("releaseLight(int lightIndex : {})", lightNumber);
		lightManager.release(lightNumber);
		// TODO: state change
	}

	@Override
	public void removeLightsGroup(String groupId) {
		logger.trace("removeLightsGroup(String groupId : {})", groupId);

		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONArray getAvailableLights() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void deviceAvailable(String host) {
		logger.trace("deviceAvailable(String host : {})", host);
		synchronized (this) {
			if(this.host == null && host != null) {
				logger.trace("deviceAvailable(...), device was previously unavailable, restoring previous status");
				lightManager.setHost(host);
				currentPattern = lightManager.setColorPattern(LightManagement.GROUP_ALL_ID, currentPattern);
				
				logger.trace("deviceAvailable(...), device was previously unavailable, restoring group FairyLights-All,"
						+ " with default attributes (might be overloaded later by the corresponding entry in the db)");
				createContiguousLightsGroup(LightManagement.GROUP_ALL_ID, 0, LightManagement.FAIRYLIGHT_SIZE-1);
				
				if(dbBound()) {
					logger.trace("deviceAvailable(...), db available, restoring groups");
					restoreGroups();					
				} else {
					logger.trace("deviceAvailable(...), db not available will not restore groups");
				}	
			} else {
				logger.trace("deviceAvailable(...), device was previously available, do nothing");
			}
			this.host = host;
		}
	}

	@Override
	public void deviceUnavailable() {
		logger.trace("deviceUnavailable()");
		synchronized (this) {
			if(host != null) {
				logger.trace("deviceUnavailable(...), device was previously available, we have to destroy the groups");
				for(String groupId: instances.keySet()) {
					((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(groupId);
				}
				instances.clear();
			} else {
				logger.trace("deviceUnavailable(...), device was previously unavailable, do nothing");
			}
			host = null;
		}
	}

	@Override
	public String getAbstractObjectId() {
		return coreObjectId;
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.ADAPTER;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", getAbstractObjectId());
		descr.put("type", getUserType());
		descr.put("coreType", getCoreType());
		descr.put("status", getObjectStatus());

		return descr;
	}

	@Override
	public int getObjectStatus() {
		return coreObjectStatus;
	}

	@Override
	public String getUserType() {
		return userType;
	}
	
	
	String dbName = CoreObjectSpec.DBNAME_DEFAULT;
	String dbCollectionNameGroups = CoreFairyLightsSpec.class.getSimpleName()+"Groups";
	DBHelper dbHelperGroups = null;
	
	/**
	 * When db is bound look for the Group stored
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
			logger.trace("dbBound(), restoring Groups");
			dbHelperGroups = dbConfig.getDBHelper(dbName, dbCollectionNameGroups);
			if(dbHelperGroups == null
					|| dbHelperGroups.getJSONEntries() == null) {
				logger.warn("dbBound(), dbHelper unavailable for Groups");
				dbUnbound();
				return false;
			} else {
				if(discoveryHeartBeat.isAvailable()) {
					logger.trace("dbBound(), device available, restoring groups");
					restoreGroups();
				} else {
					logger.trace("dbBound(), device not available, will not restor groups now");
				}
			}
			logger.trace("dbBound(), finished restoring sensors and groups");			
			dbBound = true;
		}
		return dbBound;
	}
	
	private synchronized void restoreGroups() {
		logger.trace("restoreGroups()");
		
		Set<JSONObject> entries = dbHelperGroups.getJSONEntries();
		for(JSONObject entry : entries) {
			String id = entry.getString("id");
			if( id != null && CST.componentBroker.getInst(id) == null) {
				logger.trace("restoreGroups(), no running instance with same id : {}, creating one with description : {}",
						id,
						entry);
				
				FairyLightsImpl group = createApamComponent(entry.optString("name", id),id);
				group.configure(lightManager, entry);
				instances.put(id, group);						
			} else {
				logger.trace("restoreGroups(), already an instance with id : {}, keeping the existing one", entry.getString("id"));						
			}	
		}
	}
	
	/**
	 * when no db is available stop refuse to create/remove Groups as they won't be stored
	 */
	private synchronized void dbUnbound() {
		logger.trace("dbUnbound()");
		dbHelperGroups = null;
		dbBound = false;
	}
	
	/**
	 * Helper method that use ApAM api to create the component
	 * @param groupName
	 * @return
	 */
	private FairyLightsImpl createApamComponent(String groupName, String InstanceName) {
		Implementation implem = CST.apamResolver.findImplByName(null,FairyLightsImpl.IMPL_NAME);
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
		
		return (FairyLightsImpl)inst.getServiceObject();
	}
	
	
	private NotificationMsg stateChanged(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this.getAbstractObjectId());
	}

	@Override
	public void lightChanged(JSONArray lights) {
		logger.info("lightChanged(JSONArray lights : {}), not implemented right now", lights);
				// TODO implémenter la persistence
		if(dbBound()) {
			
		}
		
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
	
	private void storeInstanceConfiguration(JSONObject serviceDescription) {
		logger.trace("storeInstanceConfiguration(JSONObject serviceDescription : {})", serviceDescription);
		if(dbBound() 
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
		if(dbBound() 
				&& dbHelperGroups!= null) {
			boolean result = dbHelperGroups.remove(serviceId);
			if(result) {
				logger.trace("storeInstanceConfiguration(...), group successfully removed from the database");
			} else {
				logger.error("storeInstanceConfiguration(...), group not removed from the database");
			}			
		}
	}
	
	/**
	 * This one allows to keep tracks of whats going on with the groups
	 * And therefore save modifications
	 * @param msg
	 */
	private void fairyLightsChangedEvent(NotificationMsg msg) {
		logger.trace("fairyLightsChangedEvent(NotificationMsg msg : {})", msg);
		if(msg != null
				&& msg.getSource() != null
				&& instances.containsKey(msg.getSource())
				&& instances.get(msg.getSource()) != null) {
			// Findig those events that requires to be persisted
			if( FairyLightsImpl.KEY_LEDS.equals(msg.getVarName())) {
				logger.trace("fairyLightsChangedEvent(...), one or more light changed, saving the whole fairy lights");
				lightChanged(lightManager.getAllLights());

				// TODO : Delete this one when lightChanged will be implemented ? 
				CoreObjectSpec instance = (CoreObjectSpec)instances.get(msg.getSource());
				storeInstanceConfiguration(instance.getDescription());
			} else if(FairyLightsImpl.KEY_PATTERNS.equals(msg.getVarName()) ) {
				logger.trace("fairyLightsChangedEvent(...), patterns changed for one group, saving this group description");
				CoreObjectSpec instance = (CoreObjectSpec)instances.get(msg.getSource());
				storeInstanceConfiguration(instance.getDescription());
			}
		} else {
			logger.warn("fairyLightsChangedEvent(...), unable to find the FairyLightsGroup that sent the message");;
		}
		
	}

	
}
