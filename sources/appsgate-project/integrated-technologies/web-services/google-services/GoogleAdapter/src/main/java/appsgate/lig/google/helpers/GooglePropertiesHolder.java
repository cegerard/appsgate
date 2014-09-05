package appsgate.lig.google.helpers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.google.impl.GoogleAdapterImpl;

/**
 * Helper class to get google properties in a static context, should not hold access token or refresh token
 * @author thibaud
 *
 */
public class GooglePropertiesHolder {
	
	public static final String CONFIGURATION_FILE="google.configuration.file";
	
	
	public static boolean isInitialConfig() {
		return initialConfig;
	}

	public static boolean isCompleteConfig() {
		return completeConfig;
	}

	static boolean initialConfig=false;
	static boolean completeConfig=false;
	
	// The following property must exists in order to have an initialConfig is true  
	static String clientId=null;
	static String clientSecret=null;
	static String apiKey=null;
	static String scopes=null;

	// If the following properties are set the completeConfig is true 
	static String refreshToken=null;
	static String tokenType=null;	
	
	static Logger logger = LoggerFactory.getLogger(GooglePropertiesHolder.class);	
	
	static Properties myProps=new Properties();
	
	public static Properties getProperties() {
		return myProps;
	}
	
	/**
	 * This one is used to set all configuration properties
	 * to access to google webservices, such as :
	 * client_id is obtained from Google Developers Console,
	 * client_secret is obtained from Google Developers Console,
	 * api_key is obtained for Google Developers Console,
	 * scope defines the webservice scopes required to authenticate,
	 * if available a refresh_token
	 * Other properties depends on the OAuth2 authentication mode (Service account, Installed application, Device, ...)
	 * access_token should not be provided there
	 * @param configuration
	 */
	public static void configure(Properties configuration) {
		logger.trace("configure(Properties configuration = "+configuration);

		if(configuration==null ||configuration.size()==0) {
			logger.warn("No configuration properties found");
			return;
		}

		if(configuration.containsKey(GoogleOpenAuthent.PARAM_CLIENTID)) {
			clientId = configuration.getProperty(GoogleOpenAuthent.PARAM_CLIENTID);
			myProps.put(GoogleOpenAuthent.PARAM_CLIENTID, clientId);
			logger.debug(GoogleOpenAuthent.PARAM_CLIENTID+" = "+clientId);
		} 

		if(configuration.containsKey(GoogleOpenAuthent.PARAM_CLIENTSECRET)) {
			clientSecret = configuration.getProperty(GoogleOpenAuthent.PARAM_CLIENTSECRET);
			myProps.put(GoogleOpenAuthent.PARAM_CLIENTSECRET, clientSecret);
			logger.debug(GoogleOpenAuthent.PARAM_CLIENTSECRET+" = "+"XXXXXXX");									
		}

		if(configuration.containsKey(GoogleOpenAuthent.PARAM_SCOPE)) {
			scopes = configuration.getProperty(GoogleOpenAuthent.PARAM_SCOPE);
			myProps.put(GoogleOpenAuthent.PARAM_SCOPE, scopes);
			logger.debug(GoogleOpenAuthent.PARAM_SCOPE+" = "+scopes);			
		}

		if(configuration.containsKey(GoogleOpenAuthent.PARAM_APIKEY)) {
			apiKey = configuration.getProperty(GoogleOpenAuthent.PARAM_APIKEY);
			myProps.put(GoogleOpenAuthent.PARAM_APIKEY, apiKey);	
			logger.debug(GoogleOpenAuthent.PARAM_APIKEY+" = "+apiKey);						
		}

		if(clientId != null
				&& clientSecret != null
				&& apiKey != null
				&& scopes != null) {
			initialConfig=true;
			logger.debug("initialConfig is OK");												

		}else {
			initialConfig = false;
		}

		if(configuration.containsKey(GoogleOpenAuthent.RESP_REFRESHTOKEN)) {
			refreshToken = configuration.getProperty(GoogleOpenAuthent.RESP_REFRESHTOKEN);
			myProps.put(GoogleOpenAuthent.RESP_REFRESHTOKEN, refreshToken);
			logger.debug(GoogleOpenAuthent.RESP_REFRESHTOKEN+" = "+"XXXXXXX");												
		}			
		if(configuration.containsKey(GoogleOpenAuthent.RESP_TOKENTYPE)) {
			tokenType = configuration.getProperty(GoogleOpenAuthent.RESP_TOKENTYPE);
			myProps.put(GoogleOpenAuthent.RESP_TOKENTYPE, tokenType);

			logger.debug(GoogleOpenAuthent.RESP_TOKENTYPE+" = "+ tokenType);												
		}			

		if(initialConfig
				&& refreshToken != null
				&& tokenType != null) {
			completeConfig = true;
			logger.debug("completeConfig is OK");												
			
		} else {
			completeConfig = false;
		}
	}
	
	public static void loadFromFile(String fileName) {
		try {
			logger.trace("Try to read configuration file : "+fileName);
			File f = new File(fileName);
			if (f != null && f.isFile()) {
				logger.trace("Configuration file found !");
				myProps.put(CONFIGURATION_FILE, fileName);

				FileReader fr = new FileReader(f);
				Properties props = new Properties();
				props.load(fr);
				configure(props);
				logger.trace("Configuration file loaded: "+fileName);
			} else {
				logger.warn("Configuration file NOT found");
			}
		} catch (Exception exc) {
			logger.error(" Exception occured when reading the configuration file : "+exc.getMessage());
		}		
	}
	
	public static void storeToFile(String fileName) {
		try {
			logger.trace("Try to write configuration file : "+fileName);
			File f = new File(fileName);
			if (f != null ) {
				logger.trace("Configuration file found !");
				myProps.put(CONFIGURATION_FILE, fileName);
				
				String comment = "Configuration File for Google Webservices API";
				comment+="\n Last edit : ";
				comment+=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());

				FileWriter fw = new FileWriter(f);
				myProps.store(fw, comment);
				logger.trace("Configuration file written: "+fileName);
			}
		} catch (Exception exc) {
			logger.error(" Exception occured when reading the configuration file : "+exc.getMessage());
		}		
		
	}
	
	
}
