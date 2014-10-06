package appsgate.lig.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * This helper class is designed to provide helper methods to use mongo DB
 * (without mongoDB dependencies)
 * @author thibaud
 *
 */
public class DBHelper {
	
	private final Logger logger = LoggerFactory.getLogger(DBHelper.class);
	
	
	public static final String ENTRY_ID = "_id";
	
	
	DBCollection dbCollection;
	
	public DBHelper(DBCollection dbCollection) {
		this.dbCollection = dbCollection;
	}
	
	public Set<JSONObject> getJSONEntries() {
		logger.trace("Set<JSONObject> getJSONEntries()");
		try {
			Set<JSONObject> result = new HashSet<JSONObject>();
			DBCursor cursor = dbCollection.find();
			while(cursor != null && cursor.hasNext()) {
				result.add(new JSONObject(JSON.serialize(cursor.next())));
			}
			logger.trace("Set<JSONObject> getJSONEntries(), returning "+result);
			return result;
		} catch(Exception exc) {
			logger.error("Exception while inserting object, "+exc.getMessage());
			return null;
		}
	}
	
	/**
	 * Insert a Simple object (such as a String)
	 * The object will be the _id
	 * If already existing in the DB, does nothing
	 * @param obj
	 * @return true if the entry was inserted
	 */
	public boolean insertSimpleObject(Object obj) {
		logger.trace("insertSimpleObject(Object obj : "+obj.toString()+")");
		try {
			dbCollection.insert(new BasicDBObject(ENTRY_ID,obj));
			return true;
		} catch(Exception exc) {
			logger.error("Exception while inserting object, "+exc.getMessage());
			return false;
		}
	}
	
	/**
	 * Insert a Simple object (such as a String)
	 * The object will get automatically an _id (if the key is not the id)
	 * If already existing in the DB, does nothing
	 * @param obj
	 * @return true if the entry was inserted
	 */
	public boolean insertSimpleObject(String key, Object obj) {
		logger.trace("insertSimpleObject(String key : "+key+", Object obj : "+obj.toString()+")");
		try {
			DBObject dbo = new BasicDBObject();
			dbo.put(key, obj);
			dbCollection.insert(dbo);
			return true;
		} catch(Exception exc) {
			logger.error("Exception while inserting object, "+exc.getMessage());
			return false;
		}
	}

	/**
	 * Insert a JSON object
	 * The object will get automatically an _id (if the key is not the id)
	 * If already existing in the DB, does nothing
	 * @param obj
	 * @return true if the entry was inserted
	 */
	public boolean insertJSON(JSONObject obj) {
		logger.trace("insertJSON(JSONObject obj : "+obj.toString()+")");
		try {
			dbCollection.insert((DBObject)JSON.parse(obj.toString()));
			return true;
		} catch(Exception exc) {
			logger.error("Exception while inserting object, "+exc.getMessage());
			return false;
		}
	}
	
	public boolean dropAndInsertListSimpleObject(List<Object> list) {
		logger.trace("removeAndInsertList(List<Object> list : "+list+")");
		try {
			dbCollection.drop();
			if(list != null && list.size()>0) {
				for(Object obj : list) {
					insertSimpleObject(obj);
				}
			}
			return true;
		} catch(Exception exc) {
			logger.error("Exception while inserting object, "+exc.getMessage());
			return false;
		}
	}
	
	public boolean dropAndInsertJSONArray(JSONArray array) {
		logger.trace("dropAndInsertJSONArray(JSONArray array : "+array.toString()+")");
		try {
			dbCollection.drop();
			for(int i=0; array!= null && i< array.length(); i++) {
				insertJSON(array.getJSONObject(i));
			}
			return true;
		} catch(Exception exc) {
			logger.error("Exception while inserting object, "+exc.getMessage());
			return false;
		}
	}
	
	
}
