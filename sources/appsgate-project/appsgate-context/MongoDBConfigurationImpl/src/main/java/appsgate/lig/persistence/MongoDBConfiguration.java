package appsgate.lig.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class MongoDBConfiguration {
	
	private final Logger logger = LoggerFactory.getLogger(MongoDBConfiguration.class);

    public static final String IMPL_NAME = "MongoDBConfiguration";

    private String dbHost;
    private Integer dbPort;
    private Integer dbTimeOut;

    private MongoClient mongoClient;
	
	public MongoDBConfiguration() {
	}

    public void setConfiguration(String dbHost, int dbPort, int dbTimeOut, MongoClient mongoClient) {
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbTimeOut = dbTimeOut;
        this.mongoClient = mongoClient;
    }

	public DB getDB(String dbName) throws MongoException {

		if (mongoClient != null && isValid())
			return mongoClient.getDB(dbName);
		
		else {
			logger.error("Could not retrieve mongo client, returning null");
			return null;
		}
	}

    public boolean isValid() {
        return checkMongoClient( mongoClient);
    }

    static public boolean checkMongoClient(MongoClient mongoClient) {
        if (mongoClient != null) {
            try {
                // Forces the connection to check valid
                mongoClient.getDatabaseNames();
                return true;
            } catch (MongoException exception) {
                return false;
            }
        }
        return false;

    }


}
