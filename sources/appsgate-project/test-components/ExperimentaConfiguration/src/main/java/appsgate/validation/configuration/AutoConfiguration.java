package appsgate.validation.configuration;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import appsgate.lig.context.userbase.spec.UserBaseSpec;

/**
 * Class use to set up complete configuration in appsgate server
 * @author Cédric Gérard
 * @since September 30, 2013
 * @version 1.0.0
 *
 */
public class AutoConfiguration {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(AutoConfiguration.class);
	
	/**
	 * User base dependency
	 */
	private UserBaseSpec UserBaseSpec;
	
	/**
	 * Built the auto configuration instance
	 */
	public AutoConfiguration() {
		super();
	}
	
	/**
	 * Callback called when the ApAm instance has been created
	 */
	public void newInst() {
		//Initiate the user base
		logger.debug("Initiate the user base");
		//Add a test user for experimenta
		if (UserBaseSpec.adduser("666", "appsgate", "Dupont", "Rene", "fils")) {
			logger.info("########## user added");
			//Add Google agendas account
			logger.info("########## add agenda accounts");
			JSONObject account = new JSONObject();
			try {
				account.put("login", "smarthome.inria@gmail.com");
				account.put("password", "smarthome2012");
				account.put("service", "GoogleAgenda");
				account.put("details", new JSONObject().put("calendarName", "Agenda boulot"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (UserBaseSpec.addAccount("666", "appsgate", account)) {
				logger.info("########## Agenda boulot account added");
			}
			
			account = new JSONObject();
			try {
				account.put("login", "smarthome.inria@gmail.com");
				account.put("password", "smarthome2012");
				account.put("service", "GoogleAgenda");
				account.put("details", new JSONObject().put("calendarName", "Smart home"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (UserBaseSpec.addAccount("666", "appsgate", account)) {
				logger.info("########## Smart home account added");
			}
			
			logger.info("########## add mail accounts");
			account = new JSONObject();
			try {
				account.put("login", "smarthome.inria@gmail.com");
				account.put("password", "smarthome2012");
				account.put("service", "Mail");
				account.put("details", new JSONObject().put("refreshRate", 120000));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (UserBaseSpec.addAccount("666", "appsgate", account)) {
				logger.info("########## Mail account added");
			}
			
		}
		
		//TODO Create Places
		
		//TODO Rename devices
		
		//TODO Move devices
	}
	
	
	/**
	 * Callback called when the ApAM instance will be delete.
	 */
	public void deleteInst() {
		logger.debug("Configurator uninstall");
	}
}
