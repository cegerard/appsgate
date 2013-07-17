package appsgate.lig.router.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.AddListenerService;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.main.spec.AppsGateSpec;
import appsgate.lig.manager.location.spec.PlaceManagerSpec;
import appsgate.lig.router.impl.listeners.RouterCommandListener;
import appsgate.lig.router.spec.GenericCommand;
import appsgate.lig.router.spec.RouterApAMSpec;
import fr.imag.adele.apam.Instance;

/**
 * This class is use to address with generic means all the devices and services recruited through
 * ApAM middleware.
 * 
 * @author Cédric Gérard
 * @since  February 14, 2013
 * @version 1.0.0
 *
 */
public class RouterImpl implements RouterApAMSpec {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(RouterImpl.class);
	
	private RouterCommandListener commandListener;

	/**
	 * Undefined sensors list, resolved by ApAM
	 */
	Set<CoreObjectSpec> abstractDevice;

	/**
	 * Service to be notified when clients send commands
	 */
	private AddListenerService addListenerService;

	/**
	 * Service to communicate with clients
	 */
	private SendWebsocketsService sendToClientService;
	
	/**
	 * The place manager ApAM component to handle the object location
	 */
	private PlaceManagerSpec locationManager;
	
	/**
	 * The main AppsGate component to call for every request
	 */
	private AppsGateSpec appsgate;

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("The router ApAM component has been initialized");
		commandListener = new RouterCommandListener(this);
		if (addListenerService.addCommandListener(commandListener)) {
			logger.info("Listeners services dependency resolved.");
		} else {
			logger.info("Listeners services dependency resolution failed.");
		}
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("The router ApAM component has been stopped");
	}

	/**
	 * Called by ApAM when an undefined instance is added to the set.
	 * 
	 * @param inst
	 *            , the new undefined instance
	 */
	public void addAbstractObject(Instance inst) {
		logger.debug("New abstract device added: " + inst.getName());
		
		//notify that a new object appeared
		CoreObjectSpec newObj = (CoreObjectSpec)inst.getServiceObject();
		sendToClientService.send("newDevice", getObjectDescription(newObj, ""));
	}

	/**
	 * Called by ApAM when an undefined instance is removed from the set
	 * 
	 * @param inst
	 *            , the removed undefined instance
	 */
	public void removedAbstractObject(Instance inst) {
		logger.debug("Abstract device removed: " + inst.getName());
	}

	/**
	 * Get the AbstractObjectSpec reference corresponding to the id objectID
	 * 
	 * @param objectID
	 *            , the AbstractObjectSpec identifier
	 * @return an AbstractObjectSpec object that have objectID as identifier
	 */
	public Object getObjectRefFromID(String objectID) {
		Iterator<CoreObjectSpec> it = abstractDevice.iterator();
		CoreObjectSpec tempAbstarctObjet = null;
		String id;
		boolean notFound = true;

		while (it.hasNext() && notFound) {
			tempAbstarctObjet = it.next();
			id = tempAbstarctObjet.getAbstractObjectId();
			if (objectID.equalsIgnoreCase(id)) {
				notFound = false;
			}
		}

		if (!notFound)
			return tempAbstarctObjet;
		else
			return null;
	}

	/**
	 * Get a command description, resolve the target reference and make the call.
	 * 
	 * @param clientId client identifier
	 * @param objectId abstract object identifier
	 * @param methodName method to call on objectId
	 * @param args arguments list form method methodName
	 * @param paramType argument type list 
	 * @param callId the remote call identifier
	 */
	@SuppressWarnings("rawtypes")
	public Runnable executeCommand(int clientId, String objectId, String methodName, ArrayList<Object> args, ArrayList<Class> paramType, String callId) {
		Object obj;	
		if(objectId.contentEquals("main")) {
			logger.info("retreive AppsGate reference: "+appsgate.toString());
			obj = appsgate;
		}else {
			obj = getObjectRefFromID(objectId);
		}
		return new GenericCommand(args, paramType, obj, methodName, callId, clientId, sendToClientService);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public GenericCommand executeCommand(String objectId, String methodName, ArrayList<Object> args, ArrayList<Class> paramType) {
			Object obj;
			if(objectId.contentEquals("main")) {
				logger.info("retreive AppsGate reference: "+appsgate.toString());
				obj = appsgate;
			}else {
				obj = getObjectRefFromID(objectId);
			}
			return new GenericCommand(args, paramType, obj, methodName);
	}
	
	@Override
	public GenericCommand executeCommand(String objectId, String methodName, JSONArray args) {
		ArrayList<Object> arguments    = new ArrayList<Object>();
		@SuppressWarnings("rawtypes")
		ArrayList<Class> argumentsType = new ArrayList<Class>();
		
		commandListener.loadArguments(args, arguments, argumentsType);
		
		return executeCommand(objectId, methodName, arguments, argumentsType);
	}
	
	/**
	 * Called by ApAM when Notification message comes
	 * and forward it to client part by calling the sendService
	 * 
	 * @param notif the notification message from ApAM
	 */
	public void gotNotification(NotificationMsg notif) {
		try {
			logger.debug("Notification message receive, " + notif.JSONize());
			sendToClientService.send(notif.JSONize().toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send all the devices description to one client
	 * @param clientId the client connection identifier
	 */
	public void getDevices(int clientId) {
		Iterator<CoreObjectSpec> devices = abstractDevice.iterator();
		
		CoreObjectSpec adev = null;
		if (devices != null) {
			JSONArray jsonDeviceList =  new JSONArray();
			
			while (devices.hasNext()) {
				adev = devices.next();
				jsonDeviceList.put(getObjectDescription(adev, ""));
			}
			sendToClientService.send(clientId, "listDevices", jsonDeviceList);
		}else{
			logger.debug("No smart object detected.");
		}
	}

	/**
	 * Resolve the location manager dependency and return the reference
	 * 
	 * @param clientId the client identifier
	 */
	public void getLocations(int clientId) {
		sendToClientService.send(clientId, "listLocations", locationManager.getJSONLocations());
	}

	/**
	 * Add new location in the place manager
	 * @param jsonObject the JSON of the new location
	 */
	public void newLocation(JSONObject jsonObject) {
		try {
			String placeId = jsonObject.getString("id");
			locationManager.addPlace(placeId, jsonObject.getString("name"));
			JSONArray devices = jsonObject.getJSONArray("devices");
			int size = devices.length();
			int i = 0;
			while(i < size) {
				String objId = (String)devices.get(i);
				locationManager.moveObject(objId, locationManager.getCoreObjectLocationId(objId), placeId);
				i++;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Move an object from source location to destination location.
	 * 
	 * @param object the object referenced
	 * @param srcLocationId the source location identifier
	 * @param destLocationId the destination location identifier
	 */
	public void moveObject(CoreObjectSpec object, String srcLocationId, String destLocationId) {
		locationManager.moveObject(object.getAbstractObjectId(), srcLocationId, destLocationId);
	}

	/**
	 * update the specified location
	 * 
	 * @param jsonObject the location update details
	 */
	public void updateLocation(JSONObject jsonObject) {
		//for now we could just rename a location
		try {
			locationManager.renameLocation(jsonObject.getString("id"), jsonObject.getString("name"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method get the auto description of an object and add
	 * the contextual information associate to this object for a specified user
	 * @param obj the object from which to get the description
	 * @param user the user from who to get the context
	 * @return the complete contextual description of an object
	 */
	private JSONObject getObjectDescription(CoreObjectSpec obj, String user) {
		JSONObject JSONDescription = null;
		try {
			//Get object auto description
			JSONDescription = obj.getDescription();
			//Add context description for this abject
			JSONDescription.put("name", appsgate.getUserObjectName(obj.getAbstractObjectId(), user));
			JSONDescription.put("locationId", locationManager.getCoreObjectLocationId(obj.getAbstractObjectId()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return JSONDescription;
	}
	
}
