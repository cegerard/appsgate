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

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.StateVariable;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.control.QueryListener;
import org.cybergarage.upnp.device.InvalidDescriptionException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.device.name.table.spec.DeviceNameTableSpec;
import appsgate.lig.context.userbase.spec.UserBaseSpec;
import appsgate.lig.eude.interpreter.spec.EUDE_InterpreterSpec;
import appsgate.lig.main.spec.AppsGateSpec;
import appsgate.lig.manager.place.spec.PlaceManagerSpec;
import appsgate.lig.router.spec.RouterApAMSpec;

/**
 * This class is the central component for AppsGate server. It allow client part
 * to make methods call from HMI managers.
 * 
 * It expose Appsgate server as an UPnP device to gather informations about it
 * through the SSDP discovery protocol
 * 
 * @author Cédric Gérard
 * @since April 23, 2013
 * @version 1.0.0
 * 
 */
public class Appsgate extends Device implements AppsGateSpec, ActionListener,
		QueryListener {

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

	/**
	 * UPnP device description xml file relative path.
	 */
	private static String descriptionFileName = "/conf/device/description.xml";

	/**
	 * default web socket connection port
	 */
	private static String wsPort = "8087";

	/**
	 * Table for deviceId, user and device name association
	 */
	private DeviceNameTableSpec deviceNameTable;

	/**
	 * The place manager ApAM component to handle the object place
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

	/**
	 * Default constructor for Appsgate java object. it load UPnP device and
	 * services profiles and subscribes the corresponding listeners.
	 * 
	 * @throws InvalidDescriptionException
	 */
	public Appsgate() throws InvalidDescriptionException {
		super(System.getProperty("user.dir") + "/" + descriptionFileName);

		// Set UPnP action listening
		Action action = getAction("getIP");
		action.setActionListener(this);

		action = getAction("getURL");
		action.setActionListener(this);

		action = getAction("getWebsocket");
		action.setActionListener(this);

		// initiate UPnP state variables
		try {
			StateVariable stateVar;
			StateVariable serverIP;
			// server IP initialization
			stateVar = getStateVariable("serverIP");

			Inet4Address localAddress = (Inet4Address) InetAddress
					.getLocalHost();
			Enumeration<NetworkInterface> nets = NetworkInterface
					.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)) {
				if (!netint.isLoopback() && !netint.isVirtual()
						&& netint.isUp()) { // TODO check also if its the local
											// network. but It will difficult to
											// find automatically the right
											// network interface
					Enumeration<InetAddress> addresses = netint
							.getInetAddresses();
					for (InetAddress address : Collections.list(addresses)) {
						if (address instanceof Inet4Address) {
							localAddress = (Inet4Address) address;
							break;
						}
					}
				}
			}
			stateVar.setValue(localAddress.getHostAddress());
			stateVar.setQueryListener(this);
			serverIP = stateVar;

			// server access URL initialization
			stateVar = getStateVariable("serverURL");
			stateVar.setValue("http://" + serverIP.getValue() + "/index.html");
			stateVar.setQueryListener(this);

			// server web socket connection entry variable initialization
			stateVar = getStateVariable("serverWebsocket");
			stateVar.setValue("http://" + serverIP.getValue() + ":" + wsPort
					+ "/");
			stateVar.setQueryListener(this);

		} catch (UnknownHostException e) {
			logger.debug("Unknown host: ");
			e.printStackTrace();
		} catch (SocketException e) {
			logger.debug("Socket exception for UPnP: ");
			e.printStackTrace();
		}

		logger.info("AppsGate instanciated");
	}

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		this.start();
		logger.info("AppsGate is started");

		if (httpService != null) {
			final HttpContext httpContext = httpService
					.createDefaultHttpContext();
			final Dictionary<String, String> initParams = new Hashtable<String, String>();
			initParams.put("from", "HttpService");
			try {
				httpService.registerResources("/appsgate", "/WEB", httpContext);
				logger.info("Appsgate mains HTML pages registered.");
			} catch (NamespaceException ex) {
				logger.error("NameSpace exception");
			}
		}
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		this.stop();
		logger.info("AppsGate has stopped");
		httpService.unregister("/appsgate");
	}

	/**
	 * Method call when an UPnP state variable is modify
	 */
	@Override
	public boolean queryControlReceived(StateVariable stateVar) {
		String varName = stateVar.getName();
		if (varName.contentEquals("serverIP")) {

			// stateVar.setValue(currTimeStr);
			return true;
		}

		// stateVar.setStatus(UPnP::INVALID_VAR, “.....”);
		return false;
	}

	/**
	 * Method call when an UPnP action is triggered
	 */
	@Override
	public boolean actionControlReceived(Action action) {
		ArgumentList argList = action.getArgumentList();
		String actionName = action.getName();

		if (actionName.contentEquals("getIP")) {
			Argument out_serverIP = argList.getArgument("serverIP");
			out_serverIP.setValue(getStateVariable("serverIP").getValue());
			return true;

		} else if (actionName.contentEquals("getURL")) {
			Argument out_serverURL = argList.getArgument("serverURL");
			out_serverURL.setValue(getStateVariable("serverURL").getValue());
			return true;
		} else if (actionName.contentEquals("getWebsocket")) {
			Argument out_serverWS = argList.getArgument("serverWebsocket");
			out_serverWS.setValue(getStateVariable("serverWebsocket")
					.getValue());
			return true;
		}

		action.setStatus(401, "invalid action");
		return false;
	}
	
	@Override
	public JSONArray getDevices() {
		return router.getDevices();
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
			String placeId = place.getString("id");
			placeManager.addPlace(placeId, place.getString("name"));
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
			placeManager.renamePlace(place.getString("id"),place.getString("name"));
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
	public boolean createUser(String id, String password, String lastName, String firstName, String role) {
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
		} catch (JSONException e) {e.printStackTrace();}
		
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
	public boolean pauseProgram(String programId){
		 return interpreter.pauseProgram(programId);
	}

	@Override
	public JSONArray getPrograms() {
		HashMap<String, JSONObject> map = interpreter.getListPrograms();
		JSONArray programList = new JSONArray();
		for(String key : map.keySet()) {
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
		BundleContext ctx = FrameworkUtil.getBundle(Appsgate.class).getBundleContext();
		Bundle systemBundle = ctx.getBundle(0);
		try {
			systemBundle.stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void restart() {
		BundleContext ctx = FrameworkUtil.getBundle(Appsgate.class).getBundleContext();
		Bundle systemBundle = ctx.getBundle(0);
		try {
			systemBundle.update();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}
}
