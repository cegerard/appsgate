package appsgate.lig.ehmi.impl;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

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

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
import appsgate.lig.chmi.spec.CHMIProxySpec;
import appsgate.lig.chmi.spec.listeners.CoreEventsListener;
import appsgate.lig.chmi.spec.listeners.CoreUpdatesListener;
import appsgate.lig.context.device.properties.table.spec.DevicePropertiesTableSpec;
import appsgate.lig.context.userbase.spec.UserBaseSpec;
import appsgate.lig.manager.place.spec.*;
import appsgate.lig.ehmi.exceptions.CoreDependencyException;
import appsgate.lig.ehmi.exceptions.ExternalComDependencyException;
import appsgate.lig.ehmi.impl.listeners.EHMICommandListener;
import appsgate.lig.ehmi.impl.listeners.ObjectEventListener;
import appsgate.lig.ehmi.impl.listeners.ObjectUpdateListener;
import appsgate.lig.ehmi.impl.listeners.TimeObserver;
import appsgate.lig.ehmi.impl.upnp.AppsGateServerDevice;
import appsgate.lig.ehmi.impl.upnp.ServerInfoService;
import appsgate.lig.ehmi.impl.upnp.StateVariableServerIP;
import appsgate.lig.ehmi.impl.upnp.StateVariableServerURL;
import appsgate.lig.ehmi.impl.upnp.StateVariableServerWebsocket;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.StateDescription;
import appsgate.lig.ehmi.spec.listeners.CoreListener;
import appsgate.lig.ehmi.spec.messages.ClockAlarmNotificationMsg;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.spec.EUDE_InterpreterSpec;


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
public class EHMIProxyImpl implements EHMIProxySpec {

	/**
	 * 
	 * static class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(EHMIProxyImpl.class);

	/**
	 * HTTP service dependency resolve by iPojo. Allow to register HTML
	 * resources to the Felix HTTP server
	 */
	private HttpService httpService;
	
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
	 * Reference on the remote proxy service to execute command on devices/services
	 */
	private CHMIProxySpec coreProxy;

	/**
	 * Reference to the EUDE interpreter to manage end user programs
	 */
	private EUDE_InterpreterSpec interpreter;
	
    /**
     * Service to be notified when clients send commands
     */
    private ListenerService addListenerService;

    /**
     * Service to communicate with clients
     */
    private SendWebsocketsService sendToClientService;
	
	
	private String wsPort="8087";

	private BundleContext context;
	private ServiceRegistration<?> serviceRegistration;

	private AppsGateServerDevice upnpDevice;
	private ServerInfoService upnpService;
	private StateVariableServerIP serverIP;
	private StateVariableServerURL serverURL;
	private StateVariableServerWebsocket serverWebsocket;

	/**
	 * Listener for EHMI command from clients
	 */
	private EHMICommandListener commandListener;

	/**
	 * Object update state event listener
	 */
	private CoreEventsListener objectEventsListener;

	/**
	 * object discovery listener
	 */
	private CoreUpdatesListener objectUpdatesListener;	
	
	 /**
     * Events subscribers list
     */
    private final HashMap<Entry, ArrayList<CoreListener>> eventsListeners = new HashMap<Entry, ArrayList<CoreListener>>();

    /**
     * Hash map use to conserve alarm identifier.
     */
    private final HashMap<Entry, Integer> alarmListenerList = new HashMap<Entry, Integer>();
    
    /**
     * This is the system clock to deal with time in AppsGate EHMI
     */
    private SystemClock systemClock;

	/**
	 * Default constructor for EHMIImpl java object. it load UPnP device and
	 * services profiles and subscribes the corresponding listeners.
	 * 
	 */
	public EHMIProxyImpl(BundleContext context) {
		logger.debug("new EHMI, BundleContext : " + context);
		this.context = context;
		this.commandListener = new EHMICommandListener(this);
		this.objectEventsListener = new ObjectEventListener(this);
		this.objectUpdatesListener = new ObjectUpdateListener(this);
		this.upnpDevice = new AppsGateServerDevice(context);
		logger.debug("UPnP Device instanciated");
		registerUpnpDevice();
		retrieveLocalAdress();
		logger.info("EHMI instanciated");
	}
	
	
	private void registerUpnpDevice() {
		Dictionary<String, Object> dict = upnpDevice.getDescriptions(null);
		serviceRegistration = context.registerService(UPnPDevice.class.getName(), upnpDevice, dict);
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
		logger.debug("EHMI is starting");
		systemClock = new SystemClock(this);
		
		if (httpService != null) {
			final HttpContext httpContext = httpService.createDefaultHttpContext();
			final Dictionary<String, String> initParams = new Hashtable<String, String>();
			initParams.put("from", "HttpService");
			try {
				httpService.registerResources("/spok", "/WEB/client", httpContext);
				logger.debug("Registered URL : "+ httpContext.getResource("/WEB/client"));
				logger.info("SPOK HTML pages registered.");
			} catch (NamespaceException ex) {
				logger.error("NameSpace exception");
			}
		}
		
        try{
        	if (addListenerService.addCommandListener(commandListener, "EHMI")) {
        		logger.info("EHMI command listener deployed.");
        	} else {
        		logger.error("EHMI command listener subscription failed.");
        	}
        }catch(ExternalComDependencyException comException) {
    		logger.debug("Resolution failed for listener service dependency, the EHMICommandListener will not be registered");
    	}
        
        try {
        	if(coreProxy.CoreEventsSubscribe(objectEventsListener)){
        		logger.info("Core event listener deployed.");
        		systemClock.startRemoteSync(coreProxy);
        	}else {
        		logger.error("Core event deployement failed.");
        	}
        	if(coreProxy.CoreUpdatesSubscribe(objectUpdatesListener)) {
        		logger.info("Core updates listener deployed.");
        	}else {
        		logger.error("Core updates listener deployement failed.");
        	}
		}catch(CoreDependencyException coreException) {
    		logger.warn("Resolution failled for core dependency, no notification subscription can be set.");
    		if(systemClock.isRemote())
    			systemClock.stopRemoteSync(coreProxy);
		}
		
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		httpService.unregister("/spok");
		try{
			addListenerService.removeCommandListener("EHMI");
		}catch(ExternalComDependencyException comException) {
    		logger.warn("Resolution failed for listener service dependency, the EHMICommandListener will not be unregistered");
    	}
		
    	try {
        	coreProxy.CoreEventsUnsubscribe(objectEventsListener);
        	coreProxy.CoreUpdatesUnsubscribe(objectUpdatesListener);
        	if(systemClock.isRemote())
        		systemClock.stopRemoteSync(coreProxy);
		}catch(CoreDependencyException coreException) {
    		logger.warn("Resolution failed for core dependency, no notification subscription can be delete.");
		}
    	
    	logger.info("EHMI has been stopped.");
	}

	@Override
	public JSONArray getDevices() {
		JSONArray devices = new JSONArray();
		try {
			return addContextData(coreProxy.getDevices());
		}catch(CoreDependencyException coreException) {
    		logger.debug("Resolution failled for core dependency, no device can be found.");
    		if(systemClock.isRemote()){
    			systemClock.stopRemoteSync(coreProxy);
    		}
    		try {
				devices.put(addContextData(systemClock.getDescription(), systemClock.getAbstractObjectId()));
			} catch (JSONException e) {
				logger.error(e.getMessage());
			}
    	}
		return devices;
	}
	
	@Override
	public JSONObject getDevice(String deviceId) {
		JSONObject devices = new JSONObject();
		try {
			JSONObject coreObject = coreProxy.getDevice(deviceId);
			return addContextData(coreObject, deviceId);
		}catch(CoreDependencyException coreException) {
    		logger.debug("Resolution failled for core dependency, no device can be found.");
    		if(systemClock.isRemote()) {
    			systemClock.stopRemoteSync(coreProxy);
    		}
    		if(deviceId.contentEquals(systemClock.getAbstractObjectId())) {
    			try {
					devices = addContextData(systemClock.getDescription(), systemClock.getAbstractObjectId());
				} catch (JSONException e) {
					logger.error(e.getMessage());
				}
    		}
		}
		return devices;
	}
	
	@Override
	public JSONArray getDevices(String type) {
		JSONArray devices = new JSONArray();
		try {
			return addContextData(getDevices(type));
		}catch(CoreDependencyException coreException) {
    		logger.debug("Resolution failled for core dependency, no device can be found.");
    		if(systemClock.isRemote()) {
    			systemClock.stopRemoteSync(coreProxy);
    		}
    		try {
    			if(type.contentEquals(systemClock.getSystemClockType())) {
    				devices.put(addContextData(systemClock.getDescription(), systemClock.getAbstractObjectId()));
    			}
			} catch (JSONException e) {
				logger.error(e.getMessage());
			}
    	}
		return devices;
	}
	
	@Override
	public StateDescription getEventsFromState(String objectId, String stateName) {
		JSONObject deviceDetails = coreProxy.getDevice(objectId);
    	StateDescription stateDescription = null;
    	try {
    		JSONObject grammar = devicePropertiesTable.getGrammarFromType(deviceDetails.getString("type"));
    		JSONArray grammarStates = grammar.getJSONArray("states");
    		for (int i = 0; i < grammarStates.length(); i++) {
    			if (grammarStates.getJSONObject(i).getString("name").equalsIgnoreCase(stateName)) {
    				stateDescription = new StateDescription(grammarStates.getJSONObject(i));
    				break;
    			}
    		}
    	}catch (JSONException ex) {
    		logger.error("Grammar not well formatted");
    		return null;
    	}
    	
    	return stateDescription;
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
			
			String placeParent = null;
			if(place.has("parent")){
				placeParent = place.getString("parent");
			}
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
	public void moveService(String serviceId, String srcPlaceId, String destPlaceId) {
		placeManager.moveService(serviceId, srcPlaceId, destPlaceId);
	}

	@Override
	public String getCoreObjectPlaceId(String objId) {
		return placeManager.getCoreObjectPlaceId(objId);
	}
	
	@Override
	public ArrayList<String> getDevicesInSpaces(ArrayList<String> typeList,
			ArrayList<String> spaces) {

		ArrayList<String> coreObjectInPlace = new ArrayList<String>();
		ArrayList<String> coreObjectOfType = new ArrayList<String>();

		// First we get all objects in each place, if the list is empty we get
		// all placed objects.
		if (!spaces.isEmpty()) {
			for (String placeId : spaces) {
				SymbolicPlace place = placeManager.getSymbolicPlace(placeId);
				if (place != null) {
					coreObjectInPlace.addAll(place.getDevices());
				} else {
					logger.warn("No such place found: {}", placeId);
				}
			}
		} else {
			for (SymbolicPlace symbolicPlace : placeManager.getPlaces()) {
				coreObjectInPlace.addAll(symbolicPlace.getDevices());
			}
		}

		// Now we get all identifier of device that match one types of the type
		// list
		try {
			if (!typeList.isEmpty()) {
				for (String type : typeList) {
					JSONArray devicesOfType = coreProxy.getDevices(type);
					int size = devicesOfType.length();
					for (int i = 0; i < size; i++) {
						coreObjectOfType.add(devicesOfType.getJSONObject(i)
								.getString("id"));
					}
				}
			} else {
				JSONArray allDevices = coreProxy.getDevices();
				int size = allDevices.length();
				for (int i = 0; i < size; i++) {
					coreObjectOfType.add(allDevices.getJSONObject(i).getString("id"));
				}
			}

			// We get the intersection between placed object and object of
			// specified type
			coreObjectInPlace.retainAll(coreObjectOfType);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return coreObjectInPlace;
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
		BundleContext ctx = FrameworkUtil.getBundle(EHMIProxyImpl.class)
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
		BundleContext ctx = FrameworkUtil.getBundle(EHMIProxyImpl.class)
				.getBundleContext();
		Bundle systemBundle = ctx.getBundle(0);
		try {
			systemBundle.update();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add contextual data to a object
	 * @param object the object to enrich
	 * @param objectId the identifier of this object
	 * @return the new contextual enrich JSONObject
	 */
	private JSONObject addContextData(JSONObject object, String objectId) {
		try {
			object.put("placeId", getCoreObjectPlaceId(objectId));
			object.put("name", getUserObjectName(objectId, ""));
		} catch (JSONException e) {
			logger.error(e.getMessage());
		}
		return object;
	}
	
	/**
	 * Add contextual data to all object in an JSONArray
	 * @param objects the objects JSONArray
	 * @return a enrich from contextual data JSONArray 
	 */
	private JSONArray addContextData(JSONArray objects) {
		JSONArray contextArray = new JSONArray();
		try{
			int nbObjects = objects.length();
			int i = 0;
			JSONObject coreObject;
			while(i < nbObjects) {
				coreObject = objects.getJSONObject(i);
				contextArray.put(addContextData(coreObject, coreObject.getString("id")));
				i++;
			}
		}catch (JSONException e) {
    		logger.error(e.getMessage());
		}
		return contextArray;
	}

	/**
	 * Find the IP address from system configuration to exposed
	 * it through UPnP
	 */
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
	
	/**
	 * Send notification to all connected clients.
	 * @param notif the notification to transmit
	 */
	public void sendToClients(JSONObject notif){
		sendToClientService.send(notif.toString());
	}
	
    /**
     * Called by ApAM when Notification message comes and forward it to client
     * part by calling the sendService
     *
     * @param notif the notification message from ApAM
     */
    public void gotNotification(NotificationMsg notif) {
        logger.debug("Notification message received, " + notif.JSONize());
        try{
        	sendToClientService.send(notif.JSONize().toString());
    	}catch(ExternalComDependencyException comException) {
    		logger.debug("Resolution failled for send to client service dependency, no message will be sent.");
    	}
    }
    
    /**
	 * Get the EHMI system clock abstraction
	 * @return the  abstract system clock instance
	 */
	public SystemClock getSystemClock() {
		return systemClock;
	}

	 /**
     * Get a command description, resolve the local target reference and return a runnable
     * command object
     *
     * @param clientId client identifier
     * @param method method name to call on objectId
     * @param arguments arguments list form method methodName
     * @param types arguments types list
	 * @param callId the remote call identifier
	 * @return runnable object that can be execute and manage.
     */
	@SuppressWarnings("rawtypes")
	public Runnable executeCommand(int clientId, String method, ArrayList<Object> arguments, ArrayList<Class> types, String callId) {
		return new GenericCommand(this, method, arguments, types, callId, clientId, sendToClientService);
	}
	
	/**
	 * Get a runnable object that can execute command from a remote device manager asynchronously with
	 * a return response
	 * 
	 * @param objIdentifier the identifier of the object on the remote system
	 * @param method the method name to call
	 * @param arguments the arguments values corresponding to the method to invoke
	 * @param types the arguments JAVA types
	 * @param clientId the client connection identifier
	 * @param callId the remote call identifier
	 * @return a runnable object that can be execute and manage.
	 */
	@SuppressWarnings("rawtypes")
	public appsgate.lig.chmi.spec.GenericCommand executeRemoteCommand(String objIdentifier, String method, ArrayList<Object> arguments, ArrayList<Class> types, int clientId, String callId) {
		return coreProxy.executeCommand(clientId, objIdentifier, method, arguments, types, callId);
	}
	
	/**
	 * Get a runnable object that can execute command from a remote device manager asynchronously
	 * 
	 * @param objIdentifier the identifier of the object on the remote system
	 * @param method the method name to call
	 * @param args the arguments list with their types
	 * @return a runnable object that can be execute and manage.
	 */
	public appsgate.lig.chmi.spec.GenericCommand executeRemoteCommand(String objIdentifier, String method, JSONArray args) {
		return coreProxy.executeCommand(objIdentifier, method, args);
	}

	@Override
	public synchronized void addCoreListener(CoreListener coreListener) {
		logger.debug("Adding a core listener...");
        Entry eventKey = new Entry(coreListener);

        //Check if the need to by register in the core clock implementation
        if (systemClock.getAbstractObjectId().contentEquals(coreListener.getObjectId()) && eventKey.getVarName().contentEquals("ClockAlarm") && !eventKey.isEventOnly()) {
            logger.debug("Adding an alarm listener...");
            //Generate calendar java object for core clock
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(coreListener.getValue()));
            //register the alarm
            int alarmId = systemClock.registerAlarm(calendar, new TimeObserver("EHMI listener for clock event"));
            //change the event entry with the alarmId value
            eventKey.setValue(String.valueOf(alarmId));
            //save the alarm identifier
            alarmListenerList.put(eventKey, alarmId);
            logger.debug("Alarm listener added.");
        }

        Set<Entry> keys = eventsListeners.keySet();
        Iterator<Entry> keysIt = keys.iterator();
        boolean added = false;

        while (keysIt.hasNext() && !added) {
            Entry key = keysIt.next();
            if (eventKey.equals(key)) {
                eventsListeners.get(key).add(coreListener);
                logger.debug("Add follower to existing core listener list");
                added = true;
            }
        }

        if (!added) {
            ArrayList<CoreListener> coreListenerList = new ArrayList<CoreListener>();
            coreListenerList.add(coreListener);
            eventsListeners.put(eventKey, coreListenerList);
            logger.debug("Add new event follower list");
        }

	}


	@Override
	public synchronized void deleteCoreListener(CoreListener coreListener) {
		logger.debug("Deleting a core listener...");
        Entry eventKey = new Entry(coreListener);

        Set<Entry> keys = eventsListeners.keySet();
        Iterator<Entry> keysIt = keys.iterator();

        while (keysIt.hasNext()) {
            Entry key = keysIt.next();
            if (key.equals(eventKey)) {
                ArrayList<CoreListener> coreListenerList = eventsListeners.get(key);
                coreListenerList.remove(coreListener);
                if (coreListenerList.isEmpty()) {
                    eventsListeners.remove(key);
                }
                Integer alarmId = alarmListenerList.get(key);
                if (alarmId != null) {
                    logger.debug("Deleting an alarm listener with id: " + alarmId);
                    systemClock.unregisterAlarm(alarmId);
                    alarmListenerList.remove(key);
                }
                break;
            }
        }
        logger.debug("Core listener deleted");
	}

	/**
	 * Return a copy of the key array for core event listeners
	 * @return Entry keys as an array list
	 */
	public synchronized ArrayList<Entry> getCoreEventListenerCopy() {
			ArrayList<Entry> keys = new ArrayList<Entry>();
            Iterator<Entry> tempKeys = eventsListeners.keySet().iterator();
            while (tempKeys.hasNext()) {
                keys.add(tempKeys.next());
            }
            return keys;
	}

	/**
	 * Get the event listener map
	 * @return the HashMap of events listeners
	 */
	public HashMap<Entry, ArrayList<CoreListener>> getEventsListeners() {
		return eventsListeners;
	}
	
	@Override
	public long getCurrentTimeInMillis() {
		return systemClock.getCurrentTimeInMillis();
	}
	
	/**
	 * Set the current time in milliseconds
	 * @param millis the new time to set
	 */
	public void setCurrentTimeInMillis(long millis) {
		logger.warn("Set current time method is not supported yet for local system clock.");
	}
	
	/**
	 * Get the current time flow rate from the local EHMI clock
	 * @return the current time flow rate as a double
	 */
	public double getTimeFlowRate() {
		return systemClock.getTimeFlowRate();
	}
	
	/**
	 * Set the time flow rate 
	 * @return the new time flow rate
	 */
	public double setTimeFlowRate(double rate) {
		logger.warn("Set time flow rate method is not supported yet for local system clock.");
		logger.warn("requested value: "+rate);
		return SystemClock.defaultTimeFlowRate;
	}
	
	/**
	 * Send a clock alarm notification to connected client 
	 * @param msg
	 */
	public void sendClockAlarmNotifcation(ClockAlarmNotificationMsg msg) {
		sendToClients(msg.JSONize());
	}

	/**
	 * Start the remote clock synchronization
	 */
	public void startRemoteClockSync() {
		systemClock.startRemoteSync(coreProxy);
	}

	/**
	 * Stop the remote clock synchronization
	 */
	public void stopRemoteClockSync() {
		systemClock.stopRemoteSync(coreProxy);
	}

}
