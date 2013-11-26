package appsgate.lig.manager.propertyhistory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoException;

import fr.imag.adele.apam.ApamManagers;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Component;
import fr.imag.adele.apam.CompositeType;
import fr.imag.adele.apam.ManagerModel;
import fr.imag.adele.apam.PropertyManager;
import fr.imag.adele.apam.impl.CompositeTypeImpl;

public class PropertyHistoryManager implements PropertyManager {

	public static class DBConfig {
		public String histURL;
		public String histDBName;
		public Integer histDBTimeout;
		public String dropCollections;
		public String dbName;
		public String dbHost;
		public int dbPort;

		public DBConfig(Properties prop) {
			this.histURL = prop.getProperty(DBURL_KEY);
			this.histDBName = prop.getProperty(DBNAME_KEY);
			try {
				this.histDBTimeout = Integer.parseInt((String) prop
						.get(DBTIMEOUT_KEY));
			} catch (NumberFormatException e) {
				this.histDBTimeout = Integer.parseInt(DBTIMEOUT_DEFAULT);
			}

			this.dropCollections = prop.getProperty(DBDROP_KEY);
			this.dbName = prop.getProperty(DBNAME_KEY);
			try {
				this.dbPort = Integer.parseInt(prop.getProperty(DBPORT_KEY));
			} catch (NumberFormatException e) {
				this.histDBTimeout = Integer.parseInt(DBPORT_DEFAULT);
			}
		}
	}

	private int dbNameCounter = 0;

	// Link compositeType with it instance of obrManager
	private DBConfig data;

	private final Logger logger = LoggerFactory
			.getLogger(PropertyHistoryManager.class);

	private MongoClient mongoClient;

	/*
	 * The collection containing the attributes created, changed and removed.
	 */
	private static final String ChangedAttributes = "Properties";

	/*
	 * The collection containing the links (wires) created, and deleted
	 */
	private static final String DBHOST_KEY = "DBHost";

	private static final String DBHOST_DEFAULT = "localhost";

	private static final String DBPORT_KEY = "DBPort";

	private static final String DBPORT_DEFAULT = "27017";

	private static final String DBURL_KEY = "DBUrl";

	private static final String DBNAME_KEY = "DBName";
	private static final String DBNAME_DEFAULT = "AppsGatePropertyHistory-";

	private static final String DBTIMEOUT_KEY = "DBTimeout";
	private static final String DBTIMEOUT_DEFAULT = "3000";
	private static final String DBDROP_KEY = "dropCollectionsOnStart";

	private static final Object DBDROP_DEFAULT = "true";

	private DB db = null;

	public PropertyHistoryManager() {
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
		logger.debug("insertDBEntry(Component comp : "+comp+", String attr : "+attr+", String status : "+status);
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
		return "PropertyHistoryManager";
	}

	@Override
	public int getPriority() {
		return 20;
	}

	private Properties addDefaultProperties(Properties prop_model) {
		if (prop_model == null) {
			prop_model = new Properties();
		}

		logger.debug("For model : ");
		if (prop_model.get(DBHOST_KEY) == null)
			prop_model.put(DBHOST_KEY, DBHOST_DEFAULT);
		logger.debug(" -> loaded DB Host : " + prop_model.get(DBHOST_KEY));
		if (prop_model.get(DBPORT_KEY) == null)
			prop_model.put(DBPORT_KEY, DBPORT_DEFAULT);
		logger.debug(" -> loaded DB Port : " + prop_model.get(DBPORT_KEY));
		if (prop_model.get(DBURL_KEY) == null)
			prop_model.put(DBURL_KEY,
					DBHOST_DEFAULT.concat(":" + prop_model.get(DBPORT_KEY)));
		logger.debug(" -> loaded DB URL : " + prop_model.get(DBURL_KEY));
		if (prop_model.get(DBNAME_KEY) == null)
			prop_model.put(DBNAME_KEY,
					DBNAME_DEFAULT.concat(String.valueOf(dbNameCounter++)));
		logger.debug(" -> loaded DB Name : " + prop_model.get(DBNAME_KEY));
		if (prop_model.get(DBTIMEOUT_KEY) == null)
			prop_model.put(DBTIMEOUT_KEY, DBTIMEOUT_DEFAULT);
		logger.debug(" -> loaded DB Timeout : " + prop_model.get(DBTIMEOUT_KEY));

		if (prop_model.get(DBDROP_KEY) == null)
			prop_model.put(DBDROP_KEY, DBDROP_DEFAULT);
		logger.debug(" -> loaded DB Dropping Collection : "
				+ prop_model.get(DBDROP_KEY));

		return prop_model;
	}

	private Properties loadProperties(URL configuration) {
		/*
		 * if no model for the compositeType, set the default values
		 */
		if (configuration == null) {
			return addDefaultProperties(null);
		} else {
			try {// try to load the compositeType model
				logger.info("Loading properties from {}", configuration);
				Properties prop_model = new Properties();
				prop_model.load(configuration.openStream());
				return addDefaultProperties(prop_model);

			} catch (IOException e) {// if impossible to load the model for the
				// compositeType, set the root composite
				logger.error("Invalid Model. Cannot open configuration URL "
						+ configuration, e.getCause());
				return addDefaultProperties(null);
			}
		}
	}

	@Override
	public void newComposite(ManagerModel model, CompositeType compositeType) {
		logger.debug("PropertyHistoryManager, newComposite(ManagerModel model = "
				+ (model == null ? "null" : model.getManagerName())
				+ ", CompositeType compositeType = "
				+ (compositeType == null ? "null" : compositeType.getName()));

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
										&& modelFileName.substring(CST.ROOT_COMPOSITE_TYPE.length() + 1).startsWith(this.getName()) ) {
							configuration = modelFile.toURI().toURL();
							logger.debug("Found external configuration file : "+configuration);
						}

					} catch (MalformedURLException e) {
						logger.warn("Error when reading url : "+e.getMessage());
					}
				}
			}
		} else {
			configuration = model.getURL();
		}

		data = new DBConfig(loadProperties(configuration));

		try {

			Builder options = new MongoClientOptions.Builder();

			options.connectTimeout(data.histDBTimeout);

			if (mongoClient == null) {
				mongoClient = new MongoClient(data.histURL, options.build());
			}

			logger.info("trying to connect with database {} in host {}",
					data.histDBName, data.histURL);

			// force connection to be established
			mongoClient.getDatabaseNames();

			db = mongoClient.getDB(data.histDBName);

		} catch (Exception e) {
			logger.error("{} is inactive, it was unable to find the DB in {}",
					this.getName(), data.histURL);
		}

		try {

			// force connection to be established
			mongoClient.getDatabaseNames();

			/*
			 * if attribute dropComection is true, drop all collections
			 */
			if (data.dropCollections.equals("true")) {
				db.getCollection(ChangedAttributes).drop();
			}

		} catch (MongoException e) {
			logger.error("no Mongo Database at URL {} name {}", model.getURL(),
					data.histDBName);
			stop();
		}

	}

	public void start() throws Exception {

		logger.debug("starting...");
		ApamManagers.addPropertyManager(this);

	}

	public void stop() {
		logger.debug("stopping...");
		ApamManagers.removePropertyManager(this);
		if(mongoClient!=null) {
			mongoClient.close();
		}
		data = null;
	}

}
