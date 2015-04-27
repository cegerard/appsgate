package appsgate.lig.persistence;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class MongoDBConfiguration {

	private final static Logger logger = LoggerFactory
			.getLogger(MongoDBConfiguration.class);

	public static final String IMPL_NAME = "MongoDBConfiguration";

	private String dbHost;
	private Integer dbPort;
	private Integer dbTimeOut;

	private MongoClient mongoClient;
	MongoDBConfigFactory factory;
	String myName;

	public MongoDBConfiguration() {
	}

	public void setConfiguration(String dbHost, int dbPort, int dbTimeOut,
			MongoClient mongoClient, MongoDBConfigFactory factory, String instanceName) {
		logger.trace("setConfiguration(String dbHost : {}, int dbPort : {}, int dbTimeOut : {},"
			+" MongoClient mongoClient : {}, MongoDBConfigFactory factory : {}, String instanceName : {})",
			dbHost, dbPort, dbTimeOut, mongoClient, factory, instanceName);

		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbTimeOut = dbTimeOut;
		this.mongoClient = mongoClient;
		this.factory=factory;
		this.myName = instanceName;
		
	}

	public DB getDB(String dbName) throws MongoException {
		logger.trace("getDB(String dbName :"+dbName+")");

		if (mongoClient != null && isValid())
			return mongoClient.getDB(dbName);

		else {
			logger.error("Could not retrieve mongo client, returning null");
			return null;
		}
	}
	
	/**
	 * Try to remove an existing DataBase (if dbName unknown does nothing)
	 * @param dbName
	 * @return
	 * @throws MongoException
	 */
	public boolean dropDB(String dbName)  {
		logger.trace("dropDB(String dbName :"+dbName+")");

		if (mongoClient != null && isValid()) {
			if(mongoClient.getDatabaseNames().contains(dbName)) {
				mongoClient.dropDatabase(dbName);
				logger.trace("dropDB(...), "+dbName+" really dropped");
			}
			return true;
		} else {
			logger.error("Could not retrieve mongo client");
			return false;
		}
	}	
	
	/**
	 * Try to remove an existing Collection in the specified dataBase (if dbName or collection unknown does nothing)
	 * @param dbName
	 * @return
	 * @throws MongoException
	 */
	public boolean dropCollection(String dbName, String collectionName) {
		logger.trace("dropCollection(String dbName :"+dbName
				+", String collectionName"+collectionName+")");
		
		try {
			DB myDB = getDB(dbName);
			if (myDB.getCollectionNames().contains(collectionName)) {
				myDB.getCollection(collectionName).drop();
				logger.trace("dropCollection(...), "+collectionName+" really dropped");
			}
			return true; 
		} catch (Exception exc) {
			logger.error("Could not retrieve mongo DB, "+exc.getMessage());
			return false;			
		}
	}	
	
	

	/**
	 * Gets an helper class that wraps simple operation with MongoDB collection
	 * (mostly insert and remove operations)
	 * 
	 * @param dbName
	 * @param collectionName
	 * @return
	 */
	public DBHelper getDBHelper(String dbName, String collectionName) {
		logger.trace("getDBHelper(String dbName :"+dbName
				+", String collectionName"+collectionName+")");
		try {
			if (mongoClient != null && isValid()) {
				DB myDB = getDB(dbName);
				DBCollection myCollection = myDB.getCollection(collectionName);
				if (myCollection != null) {
					return new DBHelper(myCollection);
				} else {
					logger.error("DB Collection not available, returning null");
					return null;
				}
			} else {
				logger.error("Could not retrieve mongo client, returning null");
				return null;
			}
		} catch (Exception exc) {
			logger.error("Error while connecting to the DB : "
					+ exc.getMessage());
			return null;
		}
	}

	/**
	 * Get all the collections names (wrapping call to mongo API)
	 * @param dbName
	 * @return
	 */
	public Set<String> getCollections(String dbName) {
		logger.trace("getCollections(String dbName :"+dbName+")");

		try {
			if (mongoClient != null && isValid()) {
				DB myDB = getDB(dbName);
				return myDB.getCollectionNames();
			} else {
				logger.error("Could not retrieve mongo client, returning null");
				return null;
			}
		} catch (Exception exc) {
			logger.error("Error while connecting to the DB : "
					+ exc.getMessage());
			return null;
		}
	}
	
	/**
	 * Get all the collections names (wrapping call to mongo API)
	 * @param dbName
	 * @return
	 */
	public Set<String> getDatabases() {
		logger.trace("getDatabases()");

		try {
			if (mongoClient != null && isValid()) {
				return new HashSet<String>(mongoClient.getDatabaseNames());
			}else {
				logger.error("Could not retrieve mongo client, returning null");
				return null;
			}
		} catch (Exception exc) {
			logger.error("Error while connecting to the DB : "
					+ exc.getMessage());
			return null;
		}
	}	

	public boolean isValid() {
		boolean valid = checkMongoClient(mongoClient);
		if(!valid) {
			logger.info("isValid(), not valid anymore, call the factory to destroy ourself");
			if(factory != null) {
				factory.destroyconfig(myName);
			}
		}
		return valid;
	}

	static public boolean checkMongoClient(MongoClient mongoClient) {
		logger.trace("checkMongoClient(MongoClient mongoClient : {})", mongoClient);
		if (mongoClient != null) {
			try {// Forces the connection to check valid
				mongoClient.getDatabaseNames();
				return true;
			} catch (MongoException exception) {
				logger.error("checkMongoClient(...), exception occured", exception);
				return false;
			}
		}
		return false;

	}

}
