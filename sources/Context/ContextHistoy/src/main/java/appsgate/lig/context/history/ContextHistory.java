package appsgate.lig.context.history;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.history.services.DataBasePullService;
import appsgate.lig.context.history.services.DataBasePushService;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoException;

/**
 * The context history is use to all every context component to save its state.
 * It offer services to save the current state and to get the last state save
 * to/from a mongo data base
 * 
 * @author Cédric Gérard
 * @since June 16, 2013
 * @version 1.0.0
 * 
 * @see DataBasePullService
 * @see DataBasePushService
 *
 */
@Component
@Instantiate
@Provides(specifications = { DataBasePullService.class, DataBasePushService.class })
public class ContextHistory implements DataBasePullService, DataBasePushService {

	private final Logger logger = LoggerFactory.getLogger(ContextHistory.class);

	//private static final String DB_NAME_KEY = "DBName";
	private static final String DB_NAME_VALUE_DEFAULT = "ContextHistory";

	//private static final String DB_URL_KEY = "DBUrl";
	private static final String DB_URL_VALUE_DEFAULT = "localhost";

	//private static final String DB_CONNECT_TIMEOUT_KEY = "DBTimeout";
	private static final String DB_CONNECT_TIMEOUT_VALUE_DEFAULT = "3000";
	//private static final String DB_DROP_START = "dropCollectionsOnStart";
	
	/**
	 * The collection containing symbol table
	 */
	private static final String CONTEXT_COLLECTION = "context";
	

	private String contextHistURL = null;
	private String contextHistDBName = null;
	private Integer contextHistDBTimeout = null;
	//private String contextDropCollections = null;

	private MongoClient mongoClient;
	
	
	/**
	 * Mongo Data base
	 */
	private DB db = null;
	
	
	@Validate
	public void start() throws Exception {


		contextHistURL 		 = DB_URL_VALUE_DEFAULT;
		contextHistDBName 	 = DB_NAME_VALUE_DEFAULT;
		contextHistDBTimeout = Integer.parseInt(DB_CONNECT_TIMEOUT_VALUE_DEFAULT);

		try {

			Builder options = new MongoClientOptions.Builder();

			options.connectTimeout(contextHistDBTimeout);

			mongoClient = new MongoClient(contextHistURL, options.build());

			logger.info("trying to connect with database {} in host {}", contextHistDBName, contextHistURL);

			//force connection to be established
			mongoClient.getDatabaseNames();

			db = mongoClient.getDB(contextHistDBName);

		} catch (Exception e) {
			logger.error("Context history is inactive, it was unable to find the DB in {}", contextHistURL);
		} 

	}

	@Invalidate
	public void stop() {}

	@Override
	public boolean pushData_add(String name, String userID, String objectID, String addedValue, ArrayList<Entry<String, Object>> properties) {
		try {

			//force connection to be established
			mongoClient.getDatabaseNames();

			DBCollection context = db.getCollection(CONTEXT_COLLECTION);

			BasicDBObject newVal = new BasicDBObject("name", name)
					.append("time", System.currentTimeMillis())
					.append("op", DataBasePushService.OP.ADD.toString())
					.append("userID", userID)
					.append("objectID", objectID)
					.append("addedValue", addedValue);

			ArrayList<BasicDBObject> stateArray = new ArrayList<BasicDBObject>();
			
			for (Map.Entry<String, Object> e : properties) {
				stateArray.add(new BasicDBObject(e.getKey(), e.getValue()));
			}
			
			newVal.append("state", stateArray);
			
			context.insert(newVal);
			return true;
			
		} catch (MongoException e) {
			stop();
		}
		return false;
	}

	@Override
	public boolean pushData_remove(String name, String userID, String objectID, String removedValue, ArrayList<Entry<String, Object>> properties) {
		try {

			//force connection to be established
			mongoClient.getDatabaseNames();

			DBCollection context = db.getCollection(CONTEXT_COLLECTION);

			BasicDBObject newVal = new BasicDBObject("name", name)
					.append("time", System.currentTimeMillis())
					.append("op", DataBasePushService.OP.REMOVE.toString())
					.append("userID", userID)
					.append("objectID", objectID)
					.append("removedValue", removedValue);

			ArrayList<BasicDBObject> stateArray = new ArrayList<BasicDBObject>();
			
			for (Map.Entry<String, Object> e : properties) {
				stateArray.add(new BasicDBObject(e.getKey(), e.getValue()));
			}
			
			newVal.append("state", stateArray);
			
			context.insert(newVal);
			return true;
		} catch (MongoException e) {
			stop();
		}
		return false;
	}

	@Override
	public boolean pushData_change(String name, String userID, String objectID, String oldValue, String newValue, ArrayList<Entry<String, Object>> properties) {
		try {

			//force connection to be established
			mongoClient.getDatabaseNames();

			DBCollection context = db.getCollection(CONTEXT_COLLECTION);

			BasicDBObject newVal = new BasicDBObject("name", name)
					.append("time", System.currentTimeMillis())
					.append("op", DataBasePushService.OP.CHANGE.toString())
					.append("userID", userID)
					.append("objectID", objectID)
					.append("oldValue", oldValue)
					.append("newValue", newValue);

			ArrayList<BasicDBObject> stateArray = new ArrayList<BasicDBObject>();
			
			for (Map.Entry<String, Object> e : properties) {
				stateArray.add(new BasicDBObject(e.getKey(), e.getValue()));
			}
			
			newVal.append("state", stateArray);
			
			context.insert(newVal);
			return true;
		} catch (MongoException e) {
			stop();
		}
		return false;
	}

	@Override
	public JSONObject pullLastObjectVersion(String ObjectName) {
		//force connection to be established
		mongoClient.getDatabaseNames();
		
		DBCollection context = db.getCollection(CONTEXT_COLLECTION);
		DBCursor cursor = context.find(new BasicDBObject("name", ObjectName));
		
		DBObject val = null;
		Long curTime, lastTime;
		lastTime = Long.valueOf(0);
		
		for(DBObject cur : cursor) {
			curTime = (Long)cur.get("time");
			if(curTime > lastTime) {
				lastTime = curTime;
				val = cur;
			}
		}
		
		if(val != null) {
			return new JSONObject(val.toMap());
		}
		
		return null;
	}

	@Override
	public boolean pushData_add(String name, String objectID,
			String addedValue, ArrayList<Entry<String, Object>> properties) {
		try {

			//force connection to be established
			mongoClient.getDatabaseNames();

			DBCollection context = db.getCollection(CONTEXT_COLLECTION);

			BasicDBObject newVal = new BasicDBObject("name", name)
					.append("time", System.currentTimeMillis())
					.append("op", DataBasePushService.OP.ADD.toString())
					.append("objectID", objectID)
					.append("addedValue", addedValue);

			ArrayList<BasicDBObject> stateArray = new ArrayList<BasicDBObject>();
			
			for (Map.Entry<String, Object> e : properties) {
				stateArray.add(new BasicDBObject(e.getKey(), e.getValue()));
			}
			
			newVal.append("state", stateArray);
			
			context.insert(newVal);
			return true;
		} catch (MongoException e) {
			stop();
		}
		return false;
	}

	@Override
	public boolean pushData_remove(String name, String objectID,
			String removedValue, ArrayList<Entry<String, Object>> properties) {
		try {

			//force connection to be established
			mongoClient.getDatabaseNames();

			DBCollection context = db.getCollection(CONTEXT_COLLECTION);

			BasicDBObject newVal = new BasicDBObject("name", name)
					.append("time", System.currentTimeMillis())
					.append("op", DataBasePushService.OP.REMOVE.toString())
					.append("objectID", objectID)
					.append("removedValue", removedValue);

			ArrayList<BasicDBObject> stateArray = new ArrayList<BasicDBObject>();
			
			for (Map.Entry<String, Object> e : properties) {
				stateArray.add(new BasicDBObject(e.getKey(), e.getValue()));
			}
			
			newVal.append("state", stateArray);
			
			context.insert(newVal);
			return true;
		} catch (MongoException e) {
			stop();
		}
		return false;
	}

	@Override
	public boolean pushData_change(String name, String objectID,
			String oldValue, String newValue,
			ArrayList<Entry<String, Object>> properties) {
		try {

			//force connection to be established
			mongoClient.getDatabaseNames();

			DBCollection context = db.getCollection(CONTEXT_COLLECTION);

			BasicDBObject newVal = new BasicDBObject("name", name)
					.append("time", System.currentTimeMillis())
					.append("op", DataBasePushService.OP.CHANGE.toString())
					.append("objectID", objectID)
					.append("oldValue", oldValue)
					.append("newValue", newValue);

			ArrayList<BasicDBObject> stateArray = new ArrayList<BasicDBObject>();
			
			for (Map.Entry<String, Object> e : properties) {
				stateArray.add(new BasicDBObject(e.getKey(), e.getValue()));
			}
			
			newVal.append("state", stateArray);
			
			context.insert(newVal);
			return true;
		} catch (MongoException e) {
			stop();
		}
		return false;
	}
	
}
