package appsgate.lig.manager.propertyhistory;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConfig {
	
	static private final Logger logger = LoggerFactory.getLogger(DBConfig.class);
	
	public static final String DBHOST_KEY = "DBHost";

	public static final String DBHOST_DEFAULT = "localhost";

	public static final String DBPORT_KEY = "DBPort";

	public static final String DBPORT_DEFAULT = "27017";

	public static final String DBURL_KEY = "DBUrl";

	public static final String DBNAME_KEY = "DBName";


	public static final String DBTIMEOUT_KEY = "DBTimeout";
	public static final String DBTIMEOUT_DEFAULT = "3000";
	public static final String DBDROP_KEY = "dropCollectionsOnStart";

	public static final String DBDROP_DEFAULT = "true";	
	
	
	public String dbURL;
	public String dBName;
	public Integer dBTimeout;
	public String dropCollections;
	public String dbName;
	public String dbHost;
	public int dbPort;

	public DBConfig(Properties prop) {
		this.dbURL = prop.getProperty(DBURL_KEY);
		this.dBName = prop.getProperty(DBNAME_KEY);
		try {
			this.dBTimeout = Integer.parseInt((String) prop
					.get(DBTIMEOUT_KEY));
		} catch (NumberFormatException e) {
			this.dBTimeout = Integer.parseInt(DBTIMEOUT_DEFAULT);
		}

		this.dropCollections = prop.getProperty(DBDROP_KEY);
		this.dbName = prop.getProperty(DBNAME_KEY);
		try {
			this.dbPort = Integer.parseInt(prop.getProperty(DBPORT_KEY));
		} catch (NumberFormatException e) {
			this.dBTimeout = Integer.parseInt(DBPORT_DEFAULT);
		}
	}
	
	
	public static Properties addDefaultProperties(Properties prop_model) {
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
					((String)prop_model.get(DBHOST_KEY)).concat(":" + prop_model.get(DBPORT_KEY)));
		logger.debug(" -> loaded DB URL : " + prop_model.get(DBURL_KEY));
		if (prop_model.get(DBTIMEOUT_KEY) == null)
			prop_model.put(DBTIMEOUT_KEY, DBTIMEOUT_DEFAULT);
		logger.debug(" -> loaded DB Timeout : " + prop_model.get(DBTIMEOUT_KEY));

		if (prop_model.get(DBDROP_KEY) == null)
			prop_model.put(DBDROP_KEY, DBDROP_DEFAULT);
		logger.debug(" -> loaded DB Dropping Collection : "
				+ prop_model.get(DBDROP_KEY));

		return prop_model;
	}
	
	static public Properties loadProperties(URL configuration) {

		if (configuration == null) {
			return addDefaultProperties(null);
		} else {
			try {
				logger.info("Loading properties from {}", configuration);
				Properties prop_model = new Properties();
				prop_model.load(configuration.openStream());
				return addDefaultProperties(prop_model);

			} catch (IOException e) {
				logger.error(" Cannot open configuration URL "
						+ configuration, e.getCause());
				return addDefaultProperties(null);
			}
		}
	}	
	
}