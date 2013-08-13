package persistence;

import java.net.UnknownHostException;
import java.util.Date;

import watteco.sensors.SmartPlugValue;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 * A class proposing functions to log sensor values into the mongoDB database.
 * 
 * @author thalgott
 */
public class Persistence {
	
	/**
	 * Inserts a smart plug's values into the database at a specific time.
	 * 
	 * @param val the values measured by the smart plug
	 * @param date the current date (millisecond granularity)
	 * @throws UnknownHostException
	 */
	public static void insert(SmartPlugValue val, Date date) throws UnknownHostException {
		// open mongoDB (needs to be installed on system)
		MongoClient client 	= new MongoClient();
		// connect to the database
		DB database 		= client.getDB("sensors");
		// connect to the watteco collection
		DBCollection c 		= database.getCollection("watteco");
		// create an object representing this measure
		BasicDBObject o 	= new BasicDBObject().
				append("date", 			date.getTime()).
				append("active_energy", val.activeEnergy).
				append("nb_of_samples", val.nbOfSamples).
				append("active_power", 	val.activePower);
		// insert it in the collection
		c.insert(o);
		client.close();
	}
	
	/**
	 * Drop all the watteco measures in the database.
	 *  
	 * @throws UnknownHostException
	 */
	public static void dropWatteco() throws UnknownHostException {
		MongoClient client 	= new MongoClient();
		DB database 		= client.getDB("sensors");
		DBCollection c 		= database.getCollection("watteco");
		c.drop();
		client.close();
	}
	
}
