package appsgate.lig.fairylights.adapter;

import java.net.Inet4Address;
import java.net.UnknownHostException;
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
import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.fairylights.CoreFairyLightsSpec;
import appsgate.lig.fairylights.service.FairyLightsImpl;
import appsgate.lig.fairylights.utils.HttpUtils;
import appsgate.lig.persistence.DBHelper;
import appsgate.lig.persistence.MongoDBConfiguration;


/**
 * Base implementation of the FairyLightsAdapter,
 * responsible for discovery of the FairyLightDevices and creation/update/removal of FairyLights groups
 */
public class FairyLightsAdapterImpl extends CoreObjectBehavior implements CoreObjectSpec,
FairyLightsAdapterSpec, FairyLightsDiscoveryListener {
	
	private static Logger logger = LoggerFactory.getLogger(FairyLightsAdapterImpl.class);
	
	MongoDBConfiguration dbConfig;
	boolean dbBound = false;
	
	String host;
	int port = -1;
	
	Map<String, FairyLightsImpl> instances;
	
	
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
	public static final long DISCOVERYPERIOD = 5*60+1000;
	
	Timer timer;
	
	public FairyLightsAdapterImpl() {
		logger.trace("FairyLightsAdapterImpl(), default constructor");
		discoveryHeartBeat=new DiscoveryHeartBeat(this, DEFAULT_PROTOCOL, DEFAULT_HOST, LUMIPIXEL_API_URL);
		
		coreObjectId = this.getClass().getSimpleName();// Do no need any hashcode or UUID, this service should be a singleton
    	coreObjectStatus = 2;
    	instances = new HashMap<String, FairyLightsImpl>();
		
		timer = new Timer();
		timer.schedule(discoveryHeartBeat, 20000, DISCOVERYPERIOD);
	}

	@Override
	public void createFreeformLightsGroup(JSONArray selectedLights) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createContiguousLightsGroup(int startingIndex, int endingIndex) {
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
		// Should (re)create all the configured instances
		// TODO Auto-generated method stub
	}

	@Override
	public void deviceUnavailable() {
		// Should remove the instances
		// TODO Auto-generated method stub
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
				// Synchro DB => running Instances
				Set<JSONObject> entries = dbHelperGroups.getJSONEntries();
				for(JSONObject entry : entries) {
					String id = entry.getString("id");
					if( id != null && CST.componentBroker.getInst(id) == null) {
						logger.trace("dbBound(), no running instance with same id : {}, creating one with description : {}",
								id,
								entry);
						// TODO

//						FairyLightsImpl group = createApamComponent(entry.getString("name"),id);
//						group.configureFromJSON(entry);
//						instances.put(id, group);						
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
	 * when no db is available stop refuse to create/remove Groups as they won't be stored
	 */
	private void dbUnbound() {
		logger.trace("dbUnbound()");
		dbHelperGroups = null;

		dbBound = false;
	}
	
	private NotificationMsg stateChanged(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this.getAbstractObjectId());
	}

		
	
	

	
}
