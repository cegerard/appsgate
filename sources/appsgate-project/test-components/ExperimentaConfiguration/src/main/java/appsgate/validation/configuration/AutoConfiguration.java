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
//			
//			//Add Google agendas account
//			logger.info("########## add agenda accounts");
			JSONObject account = new JSONObject();
//			try {
//				account.put("login", "smarthome.inria@gmail.com");
//				account.put("password", "smarthome2012");
//				account.put("service", "GoogleAgenda");
//				account.put("details", new JSONObject().put("calendarName", "Agenda boulot"));
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//			if (UserBaseSpec.addAccount("666", "appsgate", account)) {
//				logger.info("########## Agenda boulot account added");
//			}
//			
//			account = new JSONObject();
//			try {
//				account.put("login", "smarthome.inria@gmail.com");
//				account.put("password", "smarthome2012");
//				account.put("service", "GoogleAgenda");
//				account.put("details", new JSONObject().put("calendarName", "Smart home"));
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//			if (UserBaseSpec.addAccount("666", "appsgate", account)) {
//				logger.info("########## Smart home account added");
//			}
			
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
		deviceNameTableSpec.addName("ENO1c17a", "", "Capteur de contact Aspirateur");
		//Card switch EnOcean
		deviceNameTableSpec.addName("ENO2840bb", "", "Lecteur de carte");
		
		//Smart plug Watteco
		deviceNameTableSpec.addName("fe80::ff:ff00:2f0b", "", "Prise gigogne Ventilateur");
		deviceNameTableSpec.addName("fe80::ff:ff00:2c0a", "", "Prise gigogne Chauffage");
		
		//TODO change the HUE bridge IP
		//Philips HUE lights
		String chambreLightId = "194.199.23.135-1";
		String sdbLightId 	  = "194.199.23.135-2";
		String cuisineLightId = "194.199.23.135-4";
		String salonLightId   = "194.199.23.135-6";
		
		deviceNameTableSpec.addName(chambreLightId, "", "Lampe chambre");
		deviceNameTableSpec.addName(sdbLightId, "", "Lampe salle de bain");
		deviceNameTableSpec.addName(cuisineLightId, "", "Lampe cuisine");
		deviceNameTableSpec.addName(salonLightId, "", "Lampe salon");
		
		//Media Renderer
		//TODO change name of media renderer
		
		//Put the devices into places
		placeManagerSpec.moveObject("ENO57ce7", "-1", "1001");
		placeManagerSpec.moveObject("ENO27b5da", "-1", "1001");
		placeManagerSpec.moveObject("ENO1c168", "-1", "1001");
		placeManagerSpec.moveObject("fe80::ff:ff00:2c0a", "-1", "1001");
		placeManagerSpec.moveObject(sdbLightId, "-1", "1001");
		placeManagerSpec.moveObject("ENO27b2b1", "-1", "1002");
		placeManagerSpec.moveObject("ENO2840bb", "-1", "1002");
		placeManagerSpec.moveObject(chambreLightId, "-1", "1002");
		placeManagerSpec.moveObject("ENO2842c2", "-1", "1011");
		placeManagerSpec.moveObject("ENO1c1da", "-1", "1011");
		placeManagerSpec.moveObject(cuisineLightId, "-1", "1011");
		placeManagerSpec.moveObject("ENO2796f3", "-1", "1003");
		placeManagerSpec.moveObject("ENO1c167", "-1", "1003");
		placeManagerSpec.moveObject("ENO1c169", "-1", "1003");
		placeManagerSpec.moveObject("ENO2842be", "-1", "1010");
		placeManagerSpec.moveObject("ENO1c17a", "-1", "1010");
		placeManagerSpec.moveObject(salonLightId, "-1", "1010");
//		
//		//TODO put the Media renderer into the living room
//		logger.debug("Experimenta user, devices and places configuration set up.");
	}
	
	
	/**
	 * Callback called when the ApAM instance will be delete.
	 */
	public void deleteInst() {
		logger.debug("Experimenta configurator uninstall");
	}
}
