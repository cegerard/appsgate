package appsgate.lig.context;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.persistence.MongoDBConfigFactory;
import appsgate.lig.persistence.MongoDBConfiguration;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
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

public class ContextHistory implements DataBasePullService, DataBasePushService {

	private final Logger logger = LoggerFactory.getLogger(ContextHistory.class);

	private static final String DBNAME_DEFAULT = "ContextHistory";

	/*
	 * The collection containing the links (wires) created, and deleted
	 */

	private MongoDBConfigFactory myConfigFactory = null;
	private MongoDBConfiguration myConfiguration = null;

	/**
	 * The collection containing symbol table
	 */
	private static final String CONTEXT_COLLECTION = "context";

	public void start() throws Exception {
		if (myConfigFactory != null) {
			myConfiguration = myConfigFactory.newConfiguration(DBNAME_DEFAULT);
		} else {
			logger.error("Configuration Factory not bound");
			stop();
		}

	}

	public void stop() {
	}

	@Override
	public boolean pushData_add(String name, String userID, String objectID,
			String addedValue, ArrayList<Entry<String, Object>> properties) {
		if (myConfiguration != null && myConfiguration.getDB() != null)
			try {
				DBCollection context = myConfiguration.getDB().getCollection(
						CONTEXT_COLLECTION);

				BasicDBObject newVal = new BasicDBObject("name", name)
						.append("time", System.currentTimeMillis())
						.append("op", DataBasePushService.OP.ADD.toString())
						.append("userID", userID).append("objectID", objectID)
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
	public boolean pushData_remove(String name, String userID, String objectID,
			String removedValue, ArrayList<Entry<String, Object>> properties) {
		if (myConfiguration != null && myConfiguration.getDB() != null)

			try {
				DBCollection context = myConfiguration.getDB().getCollection(
						CONTEXT_COLLECTION);

				BasicDBObject newVal = new BasicDBObject("name", name)
						.append("time", System.currentTimeMillis())
						.append("op", DataBasePushService.OP.REMOVE.toString())
						.append("userID", userID).append("objectID", objectID)
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
	public boolean pushData_change(String name, String userID, String objectID,
			String oldValue, String newValue,
			ArrayList<Entry<String, Object>> properties) {
		if (myConfiguration != null && myConfiguration.getDB() != null)
			try {

				DBCollection context = myConfiguration.getDB().getCollection(
						CONTEXT_COLLECTION);

				BasicDBObject newVal = new BasicDBObject("name", name)
						.append("time", System.currentTimeMillis())
						.append("op", DataBasePushService.OP.CHANGE.toString())
						.append("userID", userID).append("objectID", objectID)
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
		// force connection to be established

		if (myConfiguration != null && myConfiguration.getDB() != null) {

			DBCollection context = myConfiguration.getDB().getCollection(
					CONTEXT_COLLECTION);
			DBCursor cursor = context
					.find(new BasicDBObject("name", ObjectName));

			DBObject val = null;
			Long curTime, lastTime;
			lastTime = Long.valueOf(0);

			for (DBObject cur : cursor) {
				curTime = (Long) cur.get("time");
				if (curTime > lastTime) {
					lastTime = curTime;
					val = cur;
				}
			}

			if (val != null) {
				return new JSONObject(val.toMap());
			}
		}

		return null;
	}

	@Override
	public boolean pushData_add(String name, String objectID,
			String addedValue, ArrayList<Entry<String, Object>> properties) {
		if (myConfiguration != null && myConfiguration.getDB() != null)
			try {

				DBCollection context = myConfiguration.getDB().getCollection(
						CONTEXT_COLLECTION);

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
		if (myConfiguration != null && myConfiguration.getDB() != null)
			try {
				DBCollection context = myConfiguration.getDB().getCollection(
						CONTEXT_COLLECTION);

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
		if (myConfiguration != null && myConfiguration.getDB() != null)
			try {

				DBCollection context = myConfiguration.getDB().getCollection(
						CONTEXT_COLLECTION);

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
