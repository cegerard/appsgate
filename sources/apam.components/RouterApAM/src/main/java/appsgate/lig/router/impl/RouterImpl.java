package appsgate.lig.router.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.communication.service.subscribe.AddListenerService;
import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;
import appsgate.lig.manager.location.spec.PlaceManagerSpec;
import appsgate.lig.router.impl.listeners.RouterCommandListener;
import fr.imag.adele.apam.Instance;

/**
 * 
 * @author cedric
 *
 */
public class RouterImpl {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(RouterImpl.class);

	/**
	 * Undefined sensors list, resolved by ApAM
	 */
	Set<AbstractObjectSpec> abstractDevice;

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
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("The router ApAM component has been initialized");

		if (addListenerService.addCommandListener(new RouterCommandListener(this))) {
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
		AbstractObjectSpec newObj = (AbstractObjectSpec)inst.getServiceObject();
		try {
			sendToClientService.send("newDevice", newObj.getDescription());
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
		Iterator<AbstractObjectSpec> it = abstractDevice.iterator();
		AbstractObjectSpec tempAbstarctObjet = null;
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
	 * @param callId the remote call identifier
	 */
	@SuppressWarnings("rawtypes")
	public Runnable executeCommand(int clientId, String objectId, String methodName, ArrayList<Object> args, ArrayList<Class> paramType, String callId) {
			Object obj = getObjectRefFromID(objectId);
			return new GenericCommand(args, paramType, obj, methodName, callId, clientId, sendToClientService);
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
		Iterator<AbstractObjectSpec> devices = abstractDevice.iterator();
		
		AbstractObjectSpec adev = null;
		if (devices != null) {
			JSONArray jsonDeviceList =  new JSONArray();
			
			while (devices.hasNext()) {
				adev = devices.next();
				try {
					jsonDeviceList.put(adev.getDescription());
				} catch (JSONException e) {
					e.printStackTrace();
				}
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
			locationManager.addPlace(jsonObject.getString("id"), jsonObject.getString("name"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Move an object from source location to destination location.
	 * 
	 * @param objectId the object reference
	 * @param srcLocationId the source location identifier
	 * @param destLocationId the destination location identifier
	 */
	public void moveObject(AbstractObjectSpec objectId, String srcLocationId, String destLocationId) {
		locationManager.moveObject(objectId, srcLocationId, destLocationId);
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
}
