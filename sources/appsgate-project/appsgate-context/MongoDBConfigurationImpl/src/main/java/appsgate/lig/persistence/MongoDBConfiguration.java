package appsgate.lig.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class MongoDBConfiguration {
	
	private final Logger logger = LoggerFactory.getLogger(MongoDBConfiguration.class);
	
	public MongoDBConfiguration(String dbName,
			MongoDBConfigFactory factory) {
		super();
		this.dbName = dbName;
		this.factory = factory;
	}

	private String dbName;
	private MongoDBConfigFactory factory;
	
	public DB getDB() throws MongoException {
		if(factory == null) {
			logger.error("Configuration factory is null (???), aborting");
			return null;
		}
			
		MongoClient mongoClient = factory.getMongoClient();

		if (mongoClient != null)
			return mongoClient.getDB(dbName);
		
		else {
			logger.error("Could not retrieve mongo client, returning null");
			return null;
		}
	}

}
