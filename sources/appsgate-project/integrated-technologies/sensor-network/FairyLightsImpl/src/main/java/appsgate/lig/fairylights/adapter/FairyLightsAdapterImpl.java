package appsgate.lig.fairylights.adapter;

import java.util.HashMap;
import java.util.Map;
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
import appsgate.lig.fairylights.service.LumiPixelImpl;
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
	
	FairyLightsImpl groupAll= null; // This group is special, it represents the whole FairyLightDevice
	public static final String GROUP_ALL_ID = "FairyLights-All";
	
	
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
		discoveryHeartBeat=new DiscoveryHeartBeat(this, DEFAULT_PROTOCOL, DEFAULT_HOST, LUMIPIXEL_API_URL);
		currentPattern = new JSONArray();
		
		coreObjectId = this.getClass().getSimpleName();// Do no need any hashcode or UUID, this service should be a singleton
    	coreObjectStatus = 2;
    	instances = new HashMap<String, CoreFairyLightsSpec>();
		
		timer = new Timer();
		timer.schedule(discoveryHeartBeat, 10000, DISCOVERYPERIOD);
	}

	@Override
	public void createFreeformLightsGroup(String name, JSONArray selectedLights) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createContiguousLightsGroup(String name, int startingIndex, int endingIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLightsGroup(String groupId, JSONArray selectedLights) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void releaseLight(int lightIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeLightsGroup(String groupId) {
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
				LumiPixelImpl.setHost(host);
				currentPattern = LumiPixelImpl.setColorPattern(currentPattern);
				
				logger.trace("deviceAvailable(...), device was previously unavailable, restoring group FairyLights-All,"
						+ " with default attributes (might be overloaded later by the corresponding entry in the db)");
				instances.put(GROUP_ALL_ID,createApamComponent(GROUP_ALL_ID, GROUP_ALL_ID));
				
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
		return CORE_TYPE.SERVICE;
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
				// TODO

//				FairyLightsImpl group = createApamComponent(entry.getString("name"),id);
//				group.configureFromJSON(entry);
//				instances.put(id, group);						
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
	private CoreFairyLightsSpec createApamComponent(String groupName, String InstanceName) {
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
		
		return (CoreFairyLightsSpec)inst.getServiceObject();
	}
	
	
	private NotificationMsg stateChanged(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this.getAbstractObjectId());
	}

	@Override
	public void lightChanged(JSONArray lights) {
		// TODO implémenter la persistence
		if(dbBound()) {
			
		}
		
	}

		
	
	

	
}
