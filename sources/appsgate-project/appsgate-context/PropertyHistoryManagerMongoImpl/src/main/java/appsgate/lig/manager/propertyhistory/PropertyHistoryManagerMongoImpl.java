package appsgate.lig.manager.propertyhistory;

import java.io.IOException;
import java.util.*;

import fr.imag.adele.apam.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.manager.propertyhistory.services.PropertyHistoryManager;
import appsgate.lig.persistence.MongoDBConfigFactory;
import appsgate.lig.persistence.MongoDBConfiguration;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class PropertyHistoryManagerMongoImpl implements PropertyManager,
		DynamicManager, PropertyHistoryManager, ContextualManager {


	private final Logger logger = LoggerFactory
			.getLogger(PropertyHistoryManagerMongoImpl.class);

	public static final String MANAGER_NAME = "PropertyHistoryManager";

	public final static String PROP_DEVICEID = "deviceId";
	public final static String PROP_TIME = "time";
	public final static String PROP_PROPERTY = "property";
	public final static String PROP_VALUE = "value";
	public final static String PROP_STATUS = "status";

	/*
	 * The collection containing the attributes created, changed and removed.
	 */
	public static final String ChangedAttributes = "Properties";

	public static final String DBNAME_DEFAULT = "AppsGatePropertyHistory-";

//	private int dbNameCounter = 0;

	private boolean dropCollections = false;

	/*
	 * The collection containing the links (wires) created, and deleted
	 */

	private MongoDBConfigFactory myConfigFactory = null;
	private MongoDBConfiguration myConfiguration = null;

	public MongoDBConfiguration getMyConfiguration() {
		return myConfiguration;
	}

	public void setMyConfiguration(MongoDBConfiguration myConfiguration) {
		this.myConfiguration = myConfiguration;
	}

	public PropertyHistoryManagerMongoImpl() {
	}

	@Override
	public void attributeAdded(Component comp, String attr, String newValue) {
		insertDBEntry(comp, attr, "added");

	}

	@Override
	public void attributeChanged(Component comp, String attr, String newValue,
			String oldValue) {
		insertDBEntry(comp, attr, "changed");

	}

	@Override
	public void attributeRemoved(Component comp, String attr, String oldValue) {
		insertDBEntry(comp, attr, "removed");
	}

	private void insertDBEntry(Component comp, String attr, String status) {
		logger.debug("insertDBEntry(Component comp : " + comp
				+ ", String attr : " + attr + ", String status : " + status);
		if (myConfiguration != null && myConfiguration.getDB() != null) {
			try {

				if (comp != null
						&& comp.getAllProperties().containsKey(PROP_DEVICEID)
						&& comp.getAllProperties().containsKey(attr)
						&& !Arrays.asList(CST.buildAttributes).contains(attr)
						&& !Arrays.asList(CST.finalAttributes).contains(attr)) {

					DBCollection ChangedAttr = myConfiguration.getDB()
							.getCollection(ChangedAttributes);

					DBObject entry = new BasicDBObject("source", comp.getName())
							.append(PROP_DEVICEID,
									comp.getProperty(PROP_DEVICEID))
							.append(PROP_TIME, System.currentTimeMillis())
							.append(PROP_PROPERTY, attr)
							.append(PROP_VALUE, comp.getProperty(attr))
							.append(PROP_STATUS, status);

					ChangedAttr.insert(entry);
					logger.debug("Entry added in the DB");
				} else {
					logger.debug("Cannot insert DB Entry, component is null ? : "
							+ comp
							+ ", doesn't have a device Id ? : "
							+ comp.getProperty(PROP_DEVICEID)
							+ ", doesn't monitor the measured value as a property ? "
							+ comp.getProperty(attr)
							+ " or attribute is a built attribute or an apam reserved property ?"
							+ attr);
				}

			} catch (MongoException e) {
				stop();
				logger.error("Cannot insert DBEntry " + e.getMessage());
			}
		} else {
			logger.error("Cannot insert DBEntry no valid configuration for DB");
		}
	}

	@Override
	public String getName() {
		return MANAGER_NAME;
	}



	public void newComposite(ManagerModel model, CompositeType compositeType) {
		logger.debug("PropertyHistoryManager, newComposite(ManagerModel model = "
				+ (model == null ? "null" : model.getManagerName())
				+ ", CompositeType compositeType = "
				+ (compositeType == null ? "null" : compositeType.getName()));

		if (myConfiguration != null && myConfiguration.getDB() != null)
			try {

				/*
				 * if attribute dropComection is true, drop all collections
				 */
				if (dropCollections) {
					myConfiguration.getDB().getCollection(ChangedAttributes)
							.drop();
				}
				logger.info("First connection with DB OK");

				initialPropertiesPolling();

			} catch (MongoException e) {
				logger.error("Error connecting to mongo DB" + e.getMessage());
				stop();
			}
	}

	private void initialPropertiesPolling() {
		logger.debug("Initial properties polling");
		for (Instance inst : CST.componentBroker.getInsts()) {
			logger.debug("For instance : " + inst.getName());
			for (String propertyName : inst.getAllPropertiesString().keySet()) {
				logger.debug(" -> adding property : " + propertyName
						+ ", whose value : " + inst.getProperty(propertyName));
				insertDBEntry(inst, propertyName, "added");
			}
		}
	}

	public void start() throws Exception {

		logger.debug("starting...");
		if (myConfigFactory != null) {
			myConfiguration = myConfigFactory.newConfiguration(DBNAME_DEFAULT);
			ApamManagers.addPropertyManager(this);
			ApamManagers.addDynamicManager(this);
		} else {
			logger.error("Configuration Factory not bound");
			stop();
		}
	}

	public void stop() {
		logger.debug("stopping...");
		ApamManagers.removePropertyManager(this);
		ApamManagers.removeDynamicManager(this);
	}

	@Override
	public void addedComponent(Component newComponent) {
		if (newComponent != null && newComponent instanceof Instance) {
			for (String propertyName : newComponent.getAllPropertiesString()
					.keySet()) {
				logger.debug("For instance : " + newComponent.getName()
						+ ", adding property : " + propertyName
						+ ", whose value : "
						+ newComponent.getProperty(propertyName));
				insertDBEntry(newComponent, propertyName, "added");
			}
		}

	}

	@Override
	public void addedLink(Link wire) {
		// Nothing to do

	}

	@Override
	public void removedComponent(Component lostComponent) {
		if (lostComponent != null && lostComponent instanceof Instance) {
			for (String propertyName : lostComponent.getAllPropertiesString()
					.keySet()) {
				logger.debug("For instance : " + lostComponent.getName()
						+ ", adding property : " + propertyName
						+ ", whose value : "
						+ lostComponent.getProperty(propertyName));
				insertDBEntry(lostComponent, propertyName, "removed");
			}
		}
	}

	@Override
	public void removedLink(Link wire) {
		// Nothing to do

	}

	/**
	 * Simple getter for property changes on the Database
	 * 
	 * @param devicesID
	 *            is an Array of devicesID (String) on which we want to evaluate
	 *            the property name, if null, we inspect all devices ID
	 * @param propertyName
	 *            The property name to evaluate
	 * @param time_start
	 *            is the difference, measured in milliseconds, between the
	 *            desired STARTING period and midnight, January 1, 1970 UTC,
	 *            (not that simulated values, as future values can lead to weird
	 *            results, loss of causality)
	 * @param time_end
	 *            is the difference, measured in milliseconds, between the
	 *            desired ENDING period and midnight, January 1, 1970 UTC,
	 *            maximum value 'should' be the current Time (except for
	 *            simulation -> which can lead to weird results, loss of
	 *            causality), time_end value MUST be greater than time_start
	 * @return a String that represent DB results as a JSON Array (itself
	 *         containing Arrays), example : [ deviceID1 : [{time: time_start,
	 *         state: v0},{time:t1,state:v1},...,{time:tf,state:vf} ], deviceID2
	 *         : [...], ... ]
	 */
	@Override
	public String getDevicesStatesHistoryAsString(Set<String> devicesID,
			String propertyName, long time_start, long time_end) {
		JSONObject result = getDevicesStatesHistoryAsJSON(devicesID,
				propertyName, time_start, time_end);
		if (result != null) {
			return result.toString();
		}
		return null;
	}

	@Override
	public JSONObject getDevicesStatesHistoryAsJSON(Set<String> devicesID,
			String propertyName, long time_start, long time_end) {
		logger.debug("getDevicesStatesHistoryAsJSON("
				+ " Set<String> devicesID : "
				+ (devicesID == null ? "null" : devicesID.toString())
				+ ", String propertyName : " + propertyName
				+ ", long time_start : " + time_start + ", long time_end : "
				+ time_end);

		if (myConfiguration != null && myConfiguration.getDB() != null) {
			try {

				// JSONArray result = new JSONArray();
				Map<String, JSONArray> results = new HashMap<String, JSONArray>();

				DBCollection changedAttr = myConfiguration.getDB()
						.getCollection(ChangedAttributes);

				BasicDBObject filter = new BasicDBObject();
				filter.put(PROP_DEVICEID, 1);
				filter.put(PROP_VALUE, 1);
				filter.put(PROP_TIME, 1);

				BasicDBObject ordering = new BasicDBObject();
				ordering.put(PROP_DEVICEID, 1);
				ordering.put(PROP_TIME, -1);

				// Step One retrieve last attribute value BEFORE (or equal)
				// time_start
				BasicDBObject queryOne = new BasicDBObject();
				if (devicesID != null && !devicesID.isEmpty()) {
					queryOne.put(PROP_DEVICEID, new BasicDBObject("$in",
							new ArrayList<String>(devicesID)));
				}
				queryOne.put(PROP_PROPERTY, propertyName);
				queryOne.put(PROP_TIME, new BasicDBObject("$lte", time_start));

				DBCursor cursorOne = changedAttr.find(queryOne, filter);

				cursorOne.sort(ordering);
				String lastDevId = null;
				while (cursorOne.hasNext()) {
					DBObject current = cursorOne.next();
					String currentDevId = current.get(PROP_DEVICEID).toString();
					if (currentDevId != null && !currentDevId.equals(lastDevId)) {
						lastDevId = currentDevId;
						results.put(lastDevId,
								new JSONArray()
								.put(new JSONObject()
									.put(PROP_TIME, time_start)
									.put(PROP_VALUE,
										current.get(PROP_VALUE))));
					}
				}
				logger.trace("Results containing latest property value at time start:\n "
								+ results.entrySet());

				cursorOne.close();

				// Step Two retrieve all attribute change between time_start and
				// time end

				BasicDBObject queryTwo = new BasicDBObject();
				if (devicesID != null && !devicesID.isEmpty()) {
					queryTwo.put(PROP_DEVICEID, new BasicDBObject("$in",
							new ArrayList<String>(devicesID)));
				}
				queryTwo.put(PROP_PROPERTY, propertyName);
				queryTwo.put(PROP_TIME, new BasicDBObject("$gt", time_start)
						.append("$lte", time_end));
				ordering.put(PROP_TIME, 1);

				DBCursor cursorTwo = changedAttr.find(queryTwo, filter);
				cursorTwo.sort(ordering);

				while (cursorTwo.hasNext()) {
					DBObject current = cursorTwo.next();
					String currentDevId = current.get(PROP_DEVICEID).toString();
					if (currentDevId != null) {
						JSONArray tab = results.get(currentDevId);
						if(tab != null && tab.length()>0) {
							tab.put(new JSONObject()
							.put(PROP_TIME, current.get(PROP_TIME))
							.put(PROP_VALUE,
								current.get(PROP_VALUE)));
						} else {
							tab = new JSONArray().put(new JSONObject()
							.put(PROP_TIME, current.get(PROP_TIME))
							.put(PROP_VALUE,
								current.get(PROP_VALUE)));
						}
						results.put(currentDevId, tab);
					}
				}

				logger.trace("Results appended with all values since time start:\n "
						+ results.entrySet());

				cursorTwo.close();
				
				JSONObject jsonResult = new JSONObject(results);
				logger.trace("Results in JSON :\n "
						+ results.entrySet());


				logger.trace("Query Successfull !");
				return jsonResult;

			} catch (MongoException e) {
				logger.error("Cannot query Database " + e.getMessage());
			} catch (JSONException e) {
				logger.error("Exception during JSON Parsing" + e.getMessage());
			}
		} else {
			logger.error("Cannot query Database, no valid configuration for DB");
		}

		return null;
	}

    @Override
    public void initializeContext(CompositeType compositeType) {
        logger.debug("initializeContext(CompositeType compositeType = "+compositeType);

		/*
		 * Get the model, if specified
		 */
        ManagerModel model = compositeType.getModel(this);
        if (model != null && model.getURL() != null) {
		/*
		 * Try to load the model from the specified location, as a map of properties
		 */
            Properties configuration = null;
            try {
                configuration = new Properties();
                configuration.load(model.getURL().openStream());

                newComposite(model, compositeType);

            } catch (IOException e) {
                logger.warn("Invalid ManagerModel. Cannot read stream " + model.getURL(), e.getCause());
            }
        } else {
            logger.warn("no ManagerModel specified for composite");
        }



    }
}
