package appsgate.lig.manager.propertyhistory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.persistence.MongoDBConfig;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoException;

import fr.imag.adele.apam.ApamManagers;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Component;
import fr.imag.adele.apam.CompositeType;
import fr.imag.adele.apam.DynamicManager;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.Link;
import fr.imag.adele.apam.ManagerModel;
import fr.imag.adele.apam.PropertyManager;

public class PropertyHistoryManagerMongoImpl implements PropertyManager, DynamicManager {


	private final Logger logger = LoggerFactory.getLogger(PropertyHistoryManagerMongoImpl.class);
	
	MongoClient mongoClient=null;
	
	public static final String MANAGER_NAME="PropertyHistoryManager";
	
	/*
	 * The collection containing the attributes created, changed and removed.
	 */
	private static final String ChangedAttributes = "Properties";
	
	private static final String DBNAME_DEFAULT = "AppsGatePropertyHistory-";
	
	private int dbNameCounter=0;

	/*
	 * The collection containing the links (wires) created, and deleted
	 */


	private MongoDBConfig data = null;
	private DB db =null;

	public PropertyHistoryManagerMongoImpl() {
		data = null;

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
		if (data != null && mongoClient != null) {
			try {
				// force connection to be established
				mongoClient.getDatabaseNames();

				DBCollection ChangedAttr = db.getCollection(ChangedAttributes);

				ChangedAttr.insert(new BasicDBObject("source", comp.getName())
						.append("time", System.currentTimeMillis())
						.append("property", attr)
						.append("value", comp.getProperty(attr))
						.append("status", status));
				logger.debug("Entry added in the DB");
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

	@Override
	public int getPriority() {
		return 20;
	}


	private Properties addDefaultProperties(Properties prop_model) {
		
		if (prop_model.get(MongoDBConfig.DBNAME_KEY) == null)
			prop_model.put(MongoDBConfig.DBNAME_KEY,
					DBNAME_DEFAULT.concat(String.valueOf(dbNameCounter++)));

		logger.debug(" -> loaded DB Name : " + prop_model.get(MongoDBConfig.DBNAME_KEY));

		return prop_model;
	}


	@Override
	public void newComposite(ManagerModel model, CompositeType compositeType) {
		logger.debug("PropertyHistoryManager, newComposite(ManagerModel model = "
				+ (model == null ? "null" : model.getManagerName())
				+ ", CompositeType compositeType = "
				+ (compositeType == null ? "null" : compositeType.getName()));

		if (db == null) {

			URL configuration = null;
			if (model == null) { // model is root
				// trying to retrieve an external default configuration file
				File modelDirectory = new File("conf");

				if (modelDirectory.exists() && modelDirectory.isDirectory()) {
					for (File modelFile : modelDirectory.listFiles()) {
						try {
							String modelFileName = modelFile.getName();
							
							if (modelFileName.endsWith(".cfg")
									&& modelFileName
											.startsWith(CST.ROOT_COMPOSITE_TYPE)
									&& modelFileName
											.substring(
													CST.ROOT_COMPOSITE_TYPE
															.length() + 1)
											.startsWith(this.getName())) {
								configuration = modelFile.toURI().toURL();
								logger.debug("Found external configuration file : "
										+ configuration);
							}

						} catch (MalformedURLException e) {
							logger.warn("Error when reading url : "
									+ e.getMessage());
						}
					}
				}
			} else {
				configuration = model.getURL();
			}

			Properties prop = MongoDBConfig.loadProperties(configuration);
			prop = addDefaultProperties(prop);
			data = new MongoDBConfig(prop);

			try {

				Builder options = new MongoClientOptions.Builder();

				options.connectTimeout(data.dBTimeout);

				if (mongoClient == null) {
					mongoClient = new MongoClient(data.dbURL, options.build());
				}

				logger.info("trying to connect with database {} in host {}",
						data.dBName, data.dbURL);

				// force connection to be established
				mongoClient.getDatabaseNames();

				db = mongoClient.getDB(data.dBName);

			} catch (Exception e) {
				logger.error(
						"{} is inactive, it was unable to find the DB in {}",
						this.getName(), data.dbURL);
			}

			try {

				/*
				 * if attribute dropComection is true, drop all collections
				 */
				if (data.dropCollections.equals("true")) {
					db.getCollection(ChangedAttributes).drop();
				}
				logger.info("First connection with DB OK");

				initialPropertiesPolling();

			} catch (MongoException e) {
				logger.error("no Mongo Database at URL {} name {}",
						model.getURL(), data.dBName);
				stop();
			}
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
		ApamManagers.addPropertyManager(this);
		ApamManagers.addDynamicManager(this);

	}

	public void stop() {
		logger.debug("stopping...");
		ApamManagers.removePropertyManager(this);
		ApamManagers.removeDynamicManager(this);
		if (mongoClient != null) {
			mongoClient.close();
		}
		data = null;
		db = null;
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

}
