package appsgate.lig.main.impl;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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

import appsgate.lig.context.device.name.table.spec.DeviceNameTableSpec;
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
	 * Table for deviceId, user and device name association
	 */
	private DeviceNameTableSpec deviceNameTable;

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
		deviceNameTable.addName(objectId, user, name);

	}

	@Override
	public String getUserObjectName(String objectId, String user) {
		return deviceNameTable.getName(objectId, user);
	}

	@Override
	public void deleteUserObjectName(String objectId, String user) {
		deviceNameTable.deleteName(objectId, user);
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

//	@Override
//	public JSONArray getJSONSpaces() {
//		
//		@SuppressWarnings("unchecked")
//		ArrayList<Space> spaces =  (ArrayList<Space>) contextManager.getSpaces().clone();
//		JSONArray jsonspaceList = new JSONArray();
//		
//		for(Space space : spaces) {
//			jsonspaceList.put(space.getDescription());
//		}
//
//		return jsonspaceList;
//	}
//
//	@Override
//	public String newSpace(String parentId, String category, JSONObject place) {
//		try {
//			String spaceId = null;
//			Space parentPlace = contextManager.getSpace(parentId);
//			if(parentPlace != null) {
//				boolean isTagged = false;
//				ArrayList<String> tagsList =  new ArrayList<String>();
//				boolean isProperties = false;
//				HashMap<String, String> propertiesMap =  new HashMap<String, String>();
//				
//				if(place.has("tags")) {
//					JSONArray tags = place.getJSONArray("tags");
//					int size = tags.length();
//					int i = 0;
//					while (i < size) {
//						String tag = (String) tags.get(i);
//						tagsList.add(tag);
//						i++;
//					}
//					isTagged = true;
//				}
//				
//				if(place.has("properties")){
//					JSONArray tags = place.getJSONArray("properties");
//					int size = tags.length();
//					int i = 0;
//					while (i < size) {
//						JSONObject prop = tags.getJSONObject(i);
//						propertiesMap.put(prop.getString("key"), prop.getString("value"));
//						i++;
//					}
//					isProperties = true;
//				}
//				
//				if( isTagged && isProperties) {
//					spaceId = contextManager.addSpace(TYPE.valueOf(category), tagsList, propertiesMap, contextManager.getSpace(parentId));
//				}else {
//					spaceId = contextManager.addSpace(TYPE.valueOf(category), contextManager.getSpace(parentId));
//					Space spaceRef = contextManager.getSpace(spaceId);
//					if(isTagged) {
//						spaceRef.setTags(tagsList);
//					}
//					
//					if(isProperties) {
//						spaceRef.setProperties(propertiesMap);
//					}
//					
//					if(place.has("name")) {
//						spaceRef.addProperty("name", place.getString("name"));
//					}
//				}
//				
//				return spaceId;
//			}else {
//				logger.error("No parent place found with id: "+parentId);
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	@Override
//	public boolean removeSpace(String id) {
//		Space space = contextManager.getSpace(id);
//		if(space != null) {
//			return contextManager.removeSpace(space);
//		}
//		return false;
//	}
//	
//	@Override
//	public boolean removeTree(String id) {
//		Space space = contextManager.getSpace(id);
//		if(space != null) {
//			return contextManager.removeTree(space);
//		}
//		return false;
//	}
//
//
//	@Override
//	public boolean removeSpaceAndUserChildren(String id) {
//		Space space = contextManager.getSpace(id);
//		if(space != null) {
//			return contextManager.removeSpaceAndUserChildren(space);
//		}
//		return false;
//	}
//
//	@Override
//	public boolean updateSpace(String spaceId, JSONObject space) {
//		try {
//			Space spaceRef = contextManager.getSpace(spaceId);
//			boolean isSuccess = true; 
//			JSONObject notify = new JSONObject();
//			notify.put("reason", "updateSpace");
//			notify.put("spaceId", spaceId);
//			
//			//Move the space 
//			if(space.has("parent")) {
//				isSuccess &= contextManager.moveSpace(spaceRef, contextManager.getSpace(space.getString("parent")));
//				notify.put("parentId", "");
//			}
//			
//			//Set or clear the tag list
//			if(space.has("tags")) {
//				
//				JSONArray tagArray = space.getJSONArray("tags");
//				
//				if(tagArray.length() == 0) {
//					spaceRef.clearTags();
//				}else {
//					ArrayList<String> tags = new ArrayList<String>();
//					int nbTag = tagArray.length();
//					for(int i=0; i<nbTag; i++) {
//						tags.add(tagArray.getString(i));
//					}
//					spaceRef.setTags(tags);
//				}
//				notify.put("tags", "");
//			}
//			
//			//Set or clear the property list
//			if(space.has("properties")) {
//				
//				JSONArray propArray = space.getJSONArray("properties");
//				
//				if(propArray.length() == 0) {
//					spaceRef.clearProperties();
//				}else {
//					HashMap<String, String> props = new HashMap<String, String>();
//					int nbProp = propArray.length();
//					for(int i=0; i<nbProp; i++) {
//						JSONObject prop = propArray.getJSONObject(i);
//						props.put(prop.getString("key"), prop.getString("value"));
//					}
//					spaceRef.setProperties(props);
//				}
//				notify.put("properties", "");
//			}
//			
//			//Rename the space
//			if(space.has("name")) {
//				spaceRef.addProperty("name", space.getString("name"));
//				isSuccess &= true;
//				notify.put("properties", "");
//			}
//			
//			//Send notification
//			contextManager.spaceUpdated(notify);
//			
//			return isSuccess;
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
//	
//	@Override
//	public JSONObject getSpaceInfo(String spaceId) {
//		return contextManager.getSpace(spaceId).getDescription();
//	}
//
//
//	@Override
//	public JSONArray getSpacesByName(String name) {
//		JSONArray spaceByName = new JSONArray();
//		ArrayList<Space> spacesList = contextManager.getSpacesWithName(name);
//		for(Space space : spacesList){
//			spaceByName.put(space.getDescription());
//		}
//		return spaceByName;
//	}
//
//
//	@Override
//	public JSONArray getSpacesWithTags(JSONArray tags) {
//		int tagNb  = tags.length();
//		ArrayList<String> tagsList = new ArrayList<String>();
//		for(int i=0; i<tagNb; i++) {
//			try {
//				tagsList.add(tags.getString(i));
//			} catch (JSONException e) {
//				logger.error(e.getMessage());
//			}
//		}
//		
//		JSONArray spaceByTag = new JSONArray();
//		ArrayList<Space> spacesList = contextManager.getSpacesWithTags(tagsList);
//		for(Space space : spacesList){
//			spaceByTag.put(space.getDescription());
//		}
//		return spaceByTag;
//	}
//
//
//	@Override
//	public JSONArray getSpacesWithProperties(JSONArray keys) {
//		int keysNb  = keys.length();
//		ArrayList<String> keysList = new ArrayList<String>();
//		for(int i=0; i<keysNb; i++) {
//			try {
//				keysList.add(keys.getString(i));
//			} catch (JSONException e) {
//				logger.error(e.getMessage());
//			}
//		}
//		
//		JSONArray spaceByProp = new JSONArray();
//		ArrayList<Space> spacesList = contextManager.getSpacesWithProperties(keysList);
//		for(Space space : spacesList){
//			spaceByProp.put(space.getDescription());
//		}
//		return spaceByProp;
//	}
//
//
//	@Override
//	public JSONArray getSpacesWithPropertiesValue(JSONArray properties) {
//		int propertiesNb  = properties.length();
//		HashMap<String, String> propertiesList = new HashMap<String, String>();
//		for(int i=0; i<propertiesNb; i++) {
//			try {
//				JSONObject prop = properties.getJSONObject(i);
//				propertiesList.put(prop.getString("key"), prop.getString("value"));
//			} catch (JSONException e) {
//				logger.error(e.getMessage());
//			}
//		}
//		
//		JSONArray spaceByPropValue = new JSONArray();
//		ArrayList<Space> spacesList = contextManager.getSpacesWithPropertiesValue(propertiesList);
//		for(Space space : spacesList){
//			spaceByPropValue.put(space.getDescription());
//		}
//		return spaceByPropValue;
//	}
//	
//
//	@Override
//	public JSONObject getTreeDescription() {
//		return contextManager.getTreeDescription();
//	}
//
//
//	@Override
//	public JSONObject getTreeDescription(String rootId) {
//		Space root = contextManager.getSpace(rootId);
//		return contextManager.getTreeDescription(root);
//	}
//
//
//	@Override
//	public JSONObject getRootSpace() {
//		return contextManager.getRootSpace().getDescription();
//	}
//
//
//	@Override
//	public boolean addTag(String placeId, String tag) {
//		Space spaceRef = contextManager.getSpace(placeId);
//		if(spaceRef != null) {
//			if(spaceRef.addTag(tag)) {
//				//Send notification
//				try {
//					JSONObject notify = new JSONObject();
//					notify.put("reason", "addTag");
//					notify.put("spaceId", placeId);
//					notify.put("tags", "");
//			
//					contextManager.spaceUpdated(notify);
//				}catch(JSONException e) {
//					e.printStackTrace();
//				}
//				return true;
//			}
//		}
//		return false;
//	}
//
//
//	@Override
//	public boolean removeTag(String placeId, String tag) {
//		Space spaceRef = contextManager.getSpace(placeId);
//		if(spaceRef != null) {
//			if(spaceRef.removeTag(tag)) {
//				//Send notification
//				try {
//					JSONObject notify = new JSONObject();
//					notify.put("reason", "removeTag");
//					notify.put("spaceId", placeId);
//					notify.put("tags", "");
//			
//					contextManager.spaceUpdated(notify);
//				}catch(JSONException e) {
//					e.printStackTrace();
//				}
//				return true;
//			}
//		}
//		return false;
//
//	}
//
//
//	@Override
//	public boolean addProperty(String placeId, String key, String value) {
//		Space spaceRef = contextManager.getSpace(placeId);
//		if(spaceRef != null) {
//			if( spaceRef.addProperty(key, value)) {
//				//Send notification
//				try {
//					JSONObject notify = new JSONObject();
//					notify.put("reason", "addProperty");
//					notify.put("spaceId", placeId);
//					notify.put("properties", "");
//			
//					contextManager.spaceUpdated(notify);
//				}catch(JSONException e) {
//					e.printStackTrace();
//				}
//				return true;
//			}
//		}
//		return false;
//
//	}
//
//
//	@Override
//	public boolean removeProperty(String placeId, String key) {
//		Space spaceRef = contextManager.getSpace(placeId);
//		if(spaceRef != null) {
//			if(spaceRef.removeProperty(key)) {
//				//Send notification
//				try {
//					JSONObject notify = new JSONObject();
//					notify.put("reason", "removeProperty");
//					notify.put("spaceId", placeId);
//					notify.put("properties", "");
//			
//					contextManager.spaceUpdated(notify);
//				}catch(JSONException e) {
//					e.printStackTrace();
//				}
//				return true;
//			}
//		}
//		return false;
//
//	}
//	
//	@Override
//	public JSONArray getDevicesInSpaces(JSONArray typeList, JSONArray spaces) {
//		ArrayList<Space> spacesList = new ArrayList<Space>();
//		ArrayList<String> typeArray = new ArrayList<String>();
//		JSONArray coreObject = new JSONArray();
//		
//		try {
//			
//			//First get all Space from their space id, if the spaces array if empty
//			//we get only the root space
//			int size = spaces.length();
//			int i = 0;
//			if(size > 0) {
//				while(i < size){
//					spacesList.add(contextManager.getSpace(spaces.getString(i)));
//					i++;
//				}
//			}else {
//				spacesList.add(contextManager.getRootSpace());
//			}
//			
//			//Second convert unusable JSONArray type to an ArrayList<String>
//			size = typeList.length();
//			i = 0;
//			while(i < size){
//				typeArray.add(typeList.getString(i));
//				i++;
//			}
//			
//			// For each selected space we check if one of its descendant
//			// match any type in the type list
//			for(Space place : spacesList) {
//				ArrayList<Space> subSpaces = place.getSubSpaces();
//				for(Space subSpace : subSpaces) {
//					//TODO the TYPE.DEVICE check will be move latter whit service integration
//					//If no type is specified we get all devices
//					if(size > 0) {
//						if(subSpace.getType().equals(TYPE.DEVICE) && typeArray.contains(subSpace.getPropertyValue("type"))) {
//							coreObject.put(router.getDevice(subSpace.getPropertyValue("ref")));
//						}
//					}else {
//						if(subSpace.getType().equals(TYPE.DEVICE)) {
//							coreObject.put(router.getDevice(subSpace.getPropertyValue("ref")));
//						}
//					}
//				}
//			}
//		}catch(JSONException jsonEx) {
//			jsonEx.printStackTrace();
//		}
//		
//		return coreObject;
//	}
//
//	@Override
//	public JSONArray getPlaces(String habitatID) {
//		return placeManager.getJSONPlaces();
//	}
//
//	@Override
//	public JSONObject getPlaceInfo(String habitatID, String placeID) {
//		JSONObject jsonPlaces = null;
//		for(Space subSpace : contextManager.getSpace(habitatID).getChildren()){
//			if(subSpace.getName().contentEquals("places")) {
//				jsonPlaces = subSpace.getSubSpace(placeID).getDescription();
//				break;
//			}
//		}
//		return jsonPlaces;
//	}

//	@Override
//	public JSONArray getUsers() {
//		JSONArray userArray = new JSONArray();
//		Space userRoot = contextManager.getUserRoot();
//		userArray.put(userRoot.getDescription());
//		for(Space user : userRoot.getSubSpaces()) {
//			userArray.put(user.getDescription());
//		}
//		return userArray;
//	}
//
//	@Override
//	public String createUser(String login, String password) {
//		
//		HashMap<String, String> properties = new HashMap<String, String>();
//		properties.put("login", login);
//		properties.put("name", login);
//		
//		return contextManager.addUserSpace(properties, contextManager.getUserRoot(), password);
//	}
//
//	@Override
//	public boolean deleteUser(String id, String password) {
//		Space space = contextManager.getSpace(id);
//		if(space instanceof UserSpace) {
//			UserSpace userSpace = (UserSpace)space;
//			if(userSpace.authenticate(password)) {
//				return contextManager.removeSpace(userSpace);
//			}
//			logger.error("Incorrect password !");
//		}
//		logger.info("user deletion failed, maybe wrong identifier or password.");
//		return false;
//	}
//
//	@Override
//	public JSONObject getUserDetails(String id) {
//		Space userSpace = contextManager.getSpace(id);
//		return userSpace.getDescription();
//	}
//
//	@Override
//	public boolean checkIfLoginIsFree(String login) {
//		return contextManager.getSpacesWithName(login).isEmpty();
//	}
//
//	@Override
//	public boolean synchronizeAccount(String id, String password, JSONObject accountDetails) {
//		Space space = contextManager.getSpace(id);
//		if(space instanceof UserSpace) {
//			UserSpace userSpace = (UserSpace)space;
//			if(userSpace.authenticate(password)) {
//				return userSpace.addAccount(accountDetails);
//			}
//			logger.error("Incorrect password !");
//		}
//		logger.info("account syncronization failed, maybe wrong identifier or password.");
//		return false;
//	}
//
//	@Override
//	public boolean desynchronizeAccount(String id, String password, JSONObject accountDetails) {
//		Space space = contextManager.getSpace(id);
//		if(space instanceof UserSpace) {
//			UserSpace userSpace = (UserSpace)space;
//			if(userSpace.authenticate(password)) {
//				return userSpace.removeAccount(accountDetails);
//			}
//			logger.error("Incorrect password !");
//		}
//		logger.info("service account deletion failed, maybe wrong identifier or password.");
//		return false;
//	}
	
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
	
//	@Override
//	public void addNewDeviceSpace(JSONObject description) {
//		Space deviceRoot = contextManager.getDeviceRoot(contextManager.getCurrentHabitat());
//		try {
//			//If the device has no type attribute we can't put it in the good space or and the
//			//corresponding space
//			if(description.has("type")) {
//				//Looking for the device space category
//				String type  = description.getString("type");
//				Space deviceCat = null;
//				for(Space child : deviceRoot.getSubSpaces()) {
//					if(child.getType().equals(TYPE.CATEGORY) && child.getPropertyValue("deviceType").contentEquals(type)){
//						deviceCat = child;
//						break;
//					}
//				}
//
//				String[] retValue = fillCategoryAndObjectNames(description);
//				String objectName = retValue[0];
//				String categoryName = retValue[1];
//				
//				if(deviceCat == null) { //if no category exist for this device type we create it.
//					HashMap<String, String> properties = new HashMap<String, String>();
//					properties.put("deviceType", type);
//					properties.put("name", categoryName);
//					String spaceId = contextManager.addSpace(TYPE.CATEGORY, properties, deviceRoot);
//					deviceCat = contextManager.getSpace(spaceId);
//				}
//		
//				//Now we create the device space...
//				//... and we add it to the device category
//				HashMap<String, String> deviceProperties = new HashMap<String, String>();
//				deviceProperties.put("deviceType", type);
//				deviceProperties.put("ref", description.getString("id"));
//				deviceProperties.put("name", objectName);
//                                
//					//Test needed to determine whether a device space in the system category already exist or not.
//				ArrayList<Space> children = deviceCat.getChildren();
//				boolean exist = false;
//				for(Space space : children) {
//					if(space.getPropertyValue("ref").contentEquals(description.getString("id"))) {
//							exist = true;
//					}
//				}
//				if(!exist) {
//					contextManager.addSpace(TYPE.DEVICE, deviceProperties, deviceCat);
//				}
//			}
//			
//		}catch(JSONException jsonex) {
//			jsonex.printStackTrace();
//		}
//		
//	}
//
//
//	
//
//
//	@Override
//	public void removeDeviceSpace(String deviceId, String type) {
//		if(Bundle.STOPPING != context.getBundle(0).getState()) {
//			Space deviceRoot = contextManager.getDeviceRoot(contextManager.getCurrentHabitat());
//
//			// Looking for the device space category
//			Space deviceCat = null;
//			for (Space child : deviceRoot.getChildren()) {
//				if (child.getType().equals(TYPE.CATEGORY)
//						&& child.getPropertyValue("deviceType").contentEquals(type)) {
//					deviceCat = child;
//					break;
//				}
//			}
//
//			Space deviceSpace = null;
//			// Looking for the device space in the category children
//			for (Space child : deviceCat.getChildren()) {
//				if (child.getPropertyValue("ref").contentEquals(deviceId)) {
//					deviceSpace = child;
//					break;
//				}
//			}
//
//			// remove the device auto manage space from the space manager
//			contextManager.removeSpace(deviceSpace);
//		}
//	}
//	
//	@Override
//	public void addNewServiceSpace(JSONObject description) {
//		Space serviceRoot = contextManager.getServiceRoot(contextManager.getCurrentHabitat());
//		try {
//			//If the service has no type attribute we can't put it in the good space or and the
//			//corresponding space
//			if(description.has("type")) {
//				//Looking for the service space category
//				String type  = description.getString("type");
//				Space serviceCat = null;
//				for(Space child : serviceRoot.getSubSpaces()) {
//					if(child.getType().equals(TYPE.CATEGORY) && child.getPropertyValue("serviceType").contentEquals(type)){
//						serviceCat = child;
//						break;
//					}
//				}
//				
//				String[] retValue = fillCategoryAndObjectNames(description);
//				String objectName = retValue[0];
//				String categoryName = retValue[1];
//		
//				if(serviceCat == null) { //if no category exist for this device type we create it.
//					HashMap<String, String> properties = new HashMap<String, String>();
//					properties.put("serviceType", type);
//					properties.put("name", categoryName);
//					String spaceId = contextManager.addSpace(TYPE.CATEGORY, properties, serviceRoot);
//					serviceCat = contextManager.getSpace(spaceId);
//				}
//		
//				//Now we create the device space...
//				//... and we add it to the device category
//				HashMap<String, String> serviceProperties = new HashMap<String, String>();
//				serviceProperties.put("serviceType", type);
//				serviceProperties.put("ref", description.getString("id"));
//				serviceProperties.put("name", objectName);
//					//Test needed to determine whether a device space in the system category already exist or not.
//				ArrayList<Space> children = serviceCat.getChildren();
//				boolean exist = false;
//				for(Space space : children) {
//					if(space.getPropertyValue("ref").contentEquals(description.getString("id"))) {
//							exist = true;
//					}
//				}
//				if(!exist) {
//					contextManager.addSpace(TYPE.SERVICE, serviceProperties, serviceCat);
//				}
//			}
//			
//		}catch(JSONException jsonex) {
//			jsonex.printStackTrace();
//		}
//	}
//
//
//	@Override
//	public void removeServiceSpace(String serviceId, String type) {
//		if(Bundle.STOPPING != context.getBundle(0).getState()) {
//			Space serviceRoot = contextManager.getServiceRoot(contextManager.getCurrentHabitat());
//
//			// Looking for the service space category
//			Space serviceCat = null;
//			for (Space child : serviceRoot.getChildren()) {
//				if (child.getType().equals(TYPE.CATEGORY)
//						&& child.getPropertyValue("serviceType").contentEquals(type)) {
//					serviceCat = child;
//					break;
//				}
//			}
//
//			Space serviceSpace = null;
//			// Looking for the service space in the category children
//			for (Space child : serviceCat.getChildren()) {
//				if (child.getPropertyValue("ref").contentEquals(serviceId)) {
//					serviceSpace = child;
//					break;
//				}
//			}
//
//			// remove the device auto manage space from the space manager
//			contextManager.removeSpace(serviceSpace);
//		}
//	}

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
