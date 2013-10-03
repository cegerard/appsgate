package appsgate.validation.configuration;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.device.name.table.spec.DeviceNameTableSpec;
import appsgate.lig.context.userbase.spec.UserBaseSpec;
import appsgate.lig.manager.place.spec.PlaceManagerSpec;

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
	 * Device name table dependency
	 */
	private DeviceNameTableSpec deviceNameTableSpec;
	
	/**
	 * Place manager dependency
	 */
	private PlaceManagerSpec placeManagerSpec;
	
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
		//Add a test user for Experimenta
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
			
			//Add mail account
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
		
		logger.info("########## add places");
		placeManagerSpec.addPlace("1011", "Cuisine");
		placeManagerSpec.addPlace("1010", "Salon");
		placeManagerSpec.addPlace("1001", "Salle de bain");
		placeManagerSpec.addPlace("1002", "Chambre");
		placeManagerSpec.addPlace("1003", "Entree");
		logger.info("########## places added");
		
		//Temperature EnOcean
		deviceNameTableSpec.addName("ENO57ce7", "", "Capteur de temperature");
		//Switch EnOcean
		deviceNameTableSpec.addName("ENO27b5da", "", "Interrupteur salle de bain");
		deviceNameTableSpec.addName("ENO2842be", "", "Interrupteur salon");
		deviceNameTableSpec.addName("ENO2842c2", "", "Interrupteur cuisine");
		deviceNameTableSpec.addName("ENO27b2b1", "", "Interrupteur chambre");
		deviceNameTableSpec.addName("ENO2796f3", "", "Interrupteur entree");
		//Contact EnOcean
		deviceNameTableSpec.addName("ENO1c1da", "", "Capteur de contact bouilloire");
		deviceNameTableSpec.addName("ENO1c167", "", "Capteur de contact porte entree");
		deviceNameTableSpec.addName("ENO1c168", "", "Capteur de contact salle de bain");
		deviceNameTableSpec.addName("ENO1c169", "", "Capteur Porte cle");
		deviceNameTableSpec.addName("ENO1c17a", "", "");
		//Card switch EnOcean
		deviceNameTableSpec.addName("ENO2840bb", "", "Lecteur de cartes de la chambre");
		
		//TODO Rename watteco devices
		//Smart plug Watteco
		//deviceNameTableSpec.addName("ENO2840bb", "", "Lecteur de cartes de la chambre");
		
		//TODO Rename Philips hue devices
		//Philips HUE lights
		//deviceNameTableSpec.addName("ENO2840bb", "", "Lecteur de cartes de la chambre");
		
		//TODO Move devices
		//placeManagerSpec.moveObject(objId, oldPlaceID, newPlaceID);
		
		//TODO deploy program
		
		
		
		logger.debug("Experimenta configuration set up.");
	}
	
	
	/**
	 * Callback called when the ApAM instance will be delete.
	 */
	public void deleteInst() {
		logger.debug("Experimenta configurator uninstall");
		//TODO remove accounts
		
		//TODO remove user
		
		//TODO device names
		
		//TODO remove device places
		
		//TODO Remove places
		
		//TODO remove programs
	}
}
