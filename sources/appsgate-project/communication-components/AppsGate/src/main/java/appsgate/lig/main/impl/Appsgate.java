package appsgate.lig.main.impl;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.upnp.UPnPDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.device.properties.table.spec.DevicePropertiesTableSpec;
import appsgate.lig.context.userbase.spec.UserBaseSpec;
import appsgate.lig.manager.place.spec.*;
import appsgate.lig.eude.interpreter.spec.EUDE_InterpreterSpec;
import appsgate.lig.main.impl.upnp.AppsGateServerDevice;
import appsgate.lig.main.impl.upnp.ServerInfoService;
import appsgate.lig.main.impl.upnp.StateVariableServerIP;
import appsgate.lig.main.impl.upnp.StateVariableServerURL;
import appsgate.lig.main.impl.upnp.StateVariableServerWebsocket;
import appsgate.lig.main.spec.AppsGateSpec;
//import appsgate.lig.manager.context.spec.ContextManagerSpec;
//import appsgate.lig.manager.space.spec.subSpace.Space;
//import appsgate.lig.manager.space.spec.subSpace.UserSpace;
//import appsgate.lig.manager.space.spec.subSpace.Space.TYPE;
import appsgate.lig.router.spec.RouterApAMSpec;

/**
 * This class is the central component for AppsGate server. It allow client part
 * to make methods call from HMI managers.
 * 
 * It expose AppGate server as an UPnP device to gather informations about it
 * through the SSDP discovery protocol
 * 
 * @author Cédric Gérard
 * @since April 23, 2013
 * @version 1.0.0
 * 
 */
public class Appsgate implements AppsGateSpec {

	/**
	 * 
	 * static class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(Appsgate.class);

	/**
	 * HTTP service dependency resolve by iPojo. Allow to register HTML
	 * resources to the Felix HTTP server
	 */
	private HttpService httpService;

//	/**
//	 * The place manager ApAM component to handle the space manager service reference
//	 */
//	private ContextManagerSpec contextManager;
	
	/**
	 * Table for deviceId, user and device properties association
	 */
	private DevicePropertiesTableSpec devicePropertiesTable;

	/**
	 * The space manager ApAM component to handle the object space
	 */
	private PlaceManagerSpec placeManager;

	/**
	 * The user manager ApAM component to handle the user base
	 */
	private UserBaseSpec userManager;

	/**
	 * Reference on the AppsGate Router to execute command on devices
	 */
	private RouterApAMSpec router;

	/**
	 * Reference to the EUDE interpreter to manage end user programs
	 */
	private EUDE_InterpreterSpec interpreter;
	
	
	private String wsPort="8087";

	private BundleContext context;
	private ServiceRegistration<?> serviceRegistration;

	private AppsGateServerDevice upnpDevice;
	private ServerInfoService upnpService;
	private StateVariableServerIP serverIP;
	private StateVariableServerURL serverURL;
	private StateVariableServerWebsocket serverWebsocket;	

	/**
	 * Default constructor for AppsGate java object. it load UPnP device and
	 * services profiles and subscribes the corresponding listeners.
	 * 
	 */
	public Appsgate(BundleContext context) {
		logger.debug("new AppsGate, BundleContext : " + context);
		this.context = context;
		upnpDevice = new AppsGateServerDevice(context);
		logger.debug("UPnP Device instanciated");
		registerUpnpDevice();
		retrieveLocalAdress();

		logger.info("AppsGate instanciated");
	}
	
	
	private void registerUpnpDevice() {
		Dictionary<String, Object> dict = upnpDevice.getDescriptions(null);
		serviceRegistration = context.registerService(
				UPnPDevice.class.getName(), upnpDevice, dict);
		logger.debug("UPnP Device registered");
		
		upnpService = (ServerInfoService) upnpDevice.getService(ServerInfoService.SERVICE_ID);
		serverIP = (StateVariableServerIP) upnpService.getStateVariable(StateVariableServerIP.VAR_NAME);
		serverURL = (StateVariableServerURL) upnpService.getStateVariable(StateVariableServerURL.VAR_NAME);
		serverWebsocket = (StateVariableServerWebsocket) upnpService.getStateVariable(StateVariableServerWebsocket.VAR_NAME);
	}

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("AppsGate is starting");

		if (httpService != null) {
			final HttpContext httpContext = httpService.createDefaultHttpContext();
			final Dictionary<String, String> initParams = new Hashtable<String, String>();
			initParams.put("from", "HttpService");
			try {
				httpService.registerResources("/appsgate", "/WEB", httpContext);
				logger.debug("Registered URL : "
						+ httpContext.getResource("/WEB"));
				logger.info("AppsGate mains HTML pages registered.");
			} catch (NamespaceException ex) {
				logger.error("NameSpace exception");
			}
		}
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("AppsGate is stopping");
		httpService.unregister("/appsgate");
	}

	@Override
	public JSONArray getDevices() {
		return router.getDevices();
	}
	
	@Override
	public JSONObject getDevice(String deviceId) {
		return router.getDevice(deviceId);
	}
	
	@Override
	public JSONArray getDevices(String type) {
		return router.getDevices(type);
	}

	@Override
	public void setUserObjectName(String objectId, String user, String name) {
		devicePropertiesTable.addName(objectId, user, name);

	}

	@Override
	public String getUserObjectName(String objectId, String user) {
		return devicePropertiesTable.getName(objectId, user);
	}

	@Override
	public void deleteUserObjectName(String objectId, String user) {
		devicePropertiesTable.deleteName(objectId, user);
	}
	
	@Override
	public boolean addGrammar(String deviceType, JSONObject grammarDescription) {
		return devicePropertiesTable.addGrammarForDeviceType(deviceType, grammarDescription);
	}


	@Override
	public boolean removeGrammar(String deviceType) {
		return devicePropertiesTable.removeGrammarForDeviceType(deviceType);
	}

	@Override
	public JSONObject getGrammarFromType(String deviceType) {
		return devicePropertiesTable.getGrammarFromType(deviceType);
	}
	
	@Override
	public JSONArray getPlaces() {
		return placeManager.getJSONPlaces();
	}

	@Override
	public void newPlace(JSONObject place) {
		try {
			//TODO put the hierarchical management
			//String placeParent = place.getString("parent");
			String placeParent = null;
			String placeId = placeManager.addPlace(place.getString("name"), placeParent);
			JSONArray devices = place.getJSONArray("devices");
			int size = devices.length();
			int i = 0;
			while (i < size) {
				String objId = (String) devices.get(i);
				placeManager.moveObject(objId, placeManager.getCoreObjectPlaceId(objId), placeId);
				i++;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removePlace(String id) {
		placeManager.removePlace(id);
	}

	@Override
	public void updatePlace(JSONObject place) {
		// for now we could just rename a place
		try {
			placeManager.renamePlace(place.getString("id"),
					place.getString("name"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void moveDevice(String objId, String srcPlaceId, String destPlaceId) {
		placeManager.moveObject(objId, srcPlaceId, destPlaceId);
	}

	@Override
	public String getCoreObjectPlaceId(String objId) {
		return placeManager.getCoreObjectPlaceId(objId);
	}
	
	@Override
	public JSONArray getUsers() {
		return userManager.getUsers();
	}

	@Override
	public boolean createUser(String id, String password, String lastName,
			String firstName, String role) {
		return userManager.adduser(id, password, lastName, lastName, role);
	}

	@Override
	public boolean deleteUser(String id, String password) {
		return userManager.removeUser(id, password);
	}

	@Override
	public JSONObject getUserDetails(String id) {
		return userManager.getUserDetails(id);
	}

	@Override
	public JSONObject getUserFullDetails(String id) {
		JSONObject obj = new JSONObject();

		try {
			obj.put("user", userManager.getUserDetails(id));
			obj.put("devices", userManager.getAssociatedDevices(id));
			obj.put("accounts", userManager.getAccountsDetails(id));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return obj;
	}

	@Override
	public boolean checkIfIdIsFree(String id) {
		return userManager.checkIfIdIsFree(id);
	}

	@Override
	public boolean synchronizeAccount(String id, String password,
			JSONObject accountDetails) {
		return userManager.addAccount(id, password, accountDetails);
	}

	@Override
	public boolean desynchronizedAccount(String id, String password,
			JSONObject accountDetails) {
		return userManager.removeAccount(id, password, accountDetails);
	}

	@Override
	public boolean associateDevice(String id, String password, String deviceId) {
		return userManager.addDevice(id, password, deviceId);
	}

	@Override
	public boolean separateDevice(String id, String password, String deviceId) {
		return userManager.removeDevice(id, password, deviceId);
	}


	@Override
	public JSONArray getPlacesByName(String name) {
		JSONArray placeByName = new JSONArray();
		ArrayList<SymbolicPlace> placesList = placeManager.getPlacesWithName(name);
		for(SymbolicPlace place : placesList){
			placeByName.put(place.getDescription());
		}
		return placeByName;
	}

	@Override
	public JSONArray gePlacesWithTags(JSONArray tags) {
		int tagNb  = tags.length();
		ArrayList<String> tagsList = new ArrayList<String>();
		for(int i=0; i<tagNb; i++) {
			try {
				tagsList.add(tags.getString(i));
			} catch (JSONException e) {
				logger.error(e.getMessage());
			}
		}
		
		JSONArray placeByTag = new JSONArray();
		ArrayList<SymbolicPlace> placesList = placeManager.getPlacesWithTags(tagsList);
		for(SymbolicPlace place : placesList){
			placeByTag.put(place.getDescription());
		}
		return placeByTag;
	}


	@Override
	public JSONArray getPlacesWithProperties(JSONArray keys) {
		int keysNb  = keys.length();
		ArrayList<String> keysList = new ArrayList<String>();
		for(int i=0; i<keysNb; i++) {
			try {
				keysList.add(keys.getString(i));
			} catch (JSONException e) {
				logger.error(e.getMessage());
			}
		}
		
		JSONArray placeByProp = new JSONArray();
		ArrayList<SymbolicPlace> placesList = placeManager.getPlacesWithProperties(keysList);
		for(SymbolicPlace place : placesList){
			placeByProp.put(place.getDescription());
		}
		return placeByProp;
	}

	@Override
	public JSONArray getPlacesWithPropertiesValue(JSONArray properties) {
		int propertiesNb  = properties.length();
		HashMap<String, String> propertiesList = new HashMap<String, String>();
		for(int i=0; i<propertiesNb; i++) {
			try {
				JSONObject prop = properties.getJSONObject(i);
				propertiesList.put(prop.getString("key"), prop.getString("value"));
			} catch (JSONException e) {
				logger.error(e.getMessage());
			}
		}
		
		JSONArray placeByPropValue = new JSONArray();
		ArrayList<SymbolicPlace> placesList = placeManager.getPlacesWithPropertiesValue(propertiesList);
		for(SymbolicPlace place : placesList){
			placeByPropValue.put(place.getDescription());
		}
		return placeByPropValue;
	}

	@Override
	public JSONArray getRootPlaces() {
		JSONArray rootPlaces = new JSONArray();
		for(SymbolicPlace place : placeManager.getRootPlaces()) {
			rootPlaces.put(place.getDescription());
		}
		return rootPlaces;
	}

	@Override
	public boolean addTag(String placeId, String tag) {
		return placeManager.addTag(placeId, tag);
	}

	@Override
	public boolean removeTag(String placeId, String tag) {
		return placeManager.removeTag(placeId, tag);
	}

	@Override
	public boolean addProperty(String placeId, String key, String value) {
		return placeManager.addProperty(placeId, key, value);
	}

	@Override
	public boolean removeProperty(String placeId, String key) {
		return placeManager.removeProperty(placeId, key);
	}
	
	@Override
	public boolean addProgram(JSONObject jsonProgram) {
		return interpreter.addProgram(jsonProgram);
	}

	@Override
	public boolean removeProgram(String programId) {
		return interpreter.removeProgram(programId);
	}

	@Override
	public boolean updateProgram(JSONObject jsonProgram) {
		return interpreter.update(jsonProgram);
	}

	@Override
	public boolean callProgram(String programId) {
		return interpreter.callProgram(programId);
	}

	@Override
	public boolean stopProgram(String programId) {
		return interpreter.stopProgram(programId);
	}

	@Override
	public boolean pauseProgram(String programId) {
		return interpreter.pauseProgram(programId);
	}

	@Override
	public JSONArray getPrograms() {
		HashMap<String, JSONObject> map = interpreter.getListPrograms();
		JSONArray programList = new JSONArray();
		for (String key : map.keySet()) {
			programList.put(map.get(key));
		}
		return programList;
	}

	@Override
	public boolean isProgramActive(String programId) {
		return interpreter.isProgramActive(programId);
	}

	@Override
	public void shutdown() {
		BundleContext ctx = FrameworkUtil.getBundle(Appsgate.class)
				.getBundleContext();
		Bundle systemBundle = ctx.getBundle(0);
		try {
			systemBundle.stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void restart() {
		BundleContext ctx = FrameworkUtil.getBundle(Appsgate.class)
				.getBundleContext();
		Bundle systemBundle = ctx.getBundle(0);
		try {
			systemBundle.update();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	private void retrieveLocalAdress() {
		// initiate UPnP state variables
		try {


			Inet4Address localAddress = (Inet4Address) InetAddress.getLocalHost();
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)) {
				if (!netint.isLoopback() && !netint.isVirtual()
						&& netint.isUp()) { // TODO check also if its the local
											// network. but It will difficult to
											// find automatically the right
											// network interface
					if(!netint.getDisplayName().contentEquals("tun0")) {
						logger.debug("The newtwork interface {} will be inspected.",netint.getDisplayName());
						Enumeration<InetAddress> addresses = netint.getInetAddresses();
						for (InetAddress address : Collections.list(addresses)) {
							if (address instanceof Inet4Address) {
								localAddress = (Inet4Address) address;
								break;
							}
						}
					}
				}
			}
			
			serverIP.setStringValue(localAddress.getHostAddress());
			logger.debug("State Variable name : "+serverIP.getName()+", value : "+serverIP.getCurrentStringValue());
			serverURL.setStringValue("http://"+serverIP.getCurrentStringValue()+ "/index.html");
			logger.debug("State Variable name : "+serverURL.getName()+", value : "+serverURL.getCurrentStringValue());
			serverWebsocket.setStringValue("http://"+serverIP.getCurrentStringValue()+ ":"+wsPort+"/");
			logger.debug("State Variable name : "+serverWebsocket.getName()+", value : "+serverWebsocket.getCurrentStringValue());

		} catch (UnknownHostException e) {
			logger.debug("Unknown host: ");
			e.printStackTrace();
		} catch (SocketException e) {
			logger.debug("Socket exception for UPnP: ");
			e.printStackTrace();
		}
	}

//	/**
//	 * File String with the object name and category name for EHMI GUI
//	 * @param objectName the name of the object
//	 * @param categoryName the name of the category to create for this object
//	 * @param description the object JSON description
//	 * @throws NumberFormatException
//	 * @throws JSONException
//	 */
//	private String[] fillCategoryAndObjectNames(JSONObject description) throws NumberFormatException, JSONException {
//		String objectName = null;
//		String categoryName = null;
//		
//		// Get the user type of the device and put 
//		// a string that allow internationalization
//		// of the type string 
//		int userType = Integer.valueOf(description.getString("type"));
//		switch(userType) {
//		
//			/** Devices  **/
//			case 0: //Temperature
//				objectName = "devices.temperature.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			
//			case 1: //Illumination
//				objectName = "devices.illumination.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			
//			case 2: //Switch
//				objectName = "devices.switch.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			
//			case 3: //Contact
//				objectName = "devices.contact.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			
//			case 4: //Key card Switch
//				objectName = "devices.keycard-reader.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//				
//			case 5: //Occupancy
//				objectName = "devices.occupancy.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			
//			case 6: //Smart plug
//				objectName = "devices.plug.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			
//			case 7: //PhilipsHUE
//				objectName = "devices.lamp.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//				
//			case 8: //On/Off actuator
//				objectName = "devices.actuator.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			
//			case 9: //CO2
//				objectName = "devices.co2.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//				
//			/** AppsGate System services **/
//			case 21: //System clock
//				objectName = "devices.clock.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//				
//			/** UPnP aggregate services **/
//			case 31: //Media player
//				categoryName = "services.mediaplayer.name.plural";
//				try{
//					objectName = description.getString("friendlyName");
//					if(!objectName.isEmpty()) {
//						break;
//					}
//				}catch(JSONException ex) {}
//				objectName = "services.mediaplayer.name.singular";
//				break;
//				
//			case 36: //Media browser
//				categoryName = "services.mediabrowser.name.plural";
//				try{
//					objectName = description.getString("friendlyName");
//					if(!objectName.isEmpty()) {
//						break;
//					}
//				}catch(JSONException ex) {}
//				objectName = "services.mediabrowser.name.singular";
//				break;
//				
//			/** UPnP services **/
//			case 415992004: //AV Transport
//				objectName = "services.avtransport.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			case 794225618: //Content directory
//				objectName = "services.contentdirectory.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			case 2052964255: //Connection manager
//				objectName = "services.connectionManager.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			case -164696113: //Rendering control
//				objectName = "services.renderingControl.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			case -532540516: //???
//				objectName = "services.unknown";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			case -1943939940: //???
//				objectName = "services.unknown";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			
//			/** Web services **/
//			case 101: //Google calendar
//				objectName = "webservices.googlecalendar.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			case 102: //Mail
//				objectName = "webservices.mail.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//			case 103: //Weather
//				objectName = "webservices.weather.name.singular";
//				categoryName = objectName.replace("singular", "plural");
//				break;
//				
//			/** Default **/
//			default:
//				objectName = "devices.device-no-name";
//				categoryName = objectName.replace("singular", "plural");
//		}
//		String [] retValue = new String[2];
//		retValue[0] =  objectName;
//		retValue[1] = categoryName;
//		return retValue;		
//	}

}
