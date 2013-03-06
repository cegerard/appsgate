package appsgate.lig.router.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.communication.service.subscribe.AddListenerService;
import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;
import appsgate.lig.manager.location.spec.PlaceManagerSpec;
import appsgate.lig.router.impl.listeners.RouterCommandListener;
import fr.imag.adele.apam.Instance;

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
		sendToClientService.send("newDevice", newObj.getDescription());
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
	 * @param clientID, client identifier
	 * @param objectId, abstract object identifier
	 * @param methodName, method to call on objectId
	 * @param args, arguments list form method methodName
	 */
	public void executeCommand(String clientID, String objectId, String methodName, ArrayList<Object> args) {
		try {
			Object obj = getObjectRefFromID(objectId);
			Object ret = abstractInvoke(obj, args.toArray(), methodName);
			if(ret != null)
				logger.debug(ret.toString()+" / return type: "+ret.getClass().getName());
		} catch (Exception e) {
			logger.debug("The generic method invocation failed --> ");
			e.printStackTrace();
		}
	}

	/**
	 * This method allow the router to invoke methods on an abstract java
	 * object.
	 * 
	 * @param obj
	 *            , the abstract object on which the method will be invoke
	 * @param args
	 *            , all arguments for the method call
	 * @param methodName
	 *            , the method to invoke
	 * @return the result of dispatching the method represented by this object
	 *         on obj with parameters args
	 * @throws Exception
	 */
	public Object abstractInvoke(Object obj, Object[] args, String methodName)
			throws Exception {
		@SuppressWarnings("rawtypes")
		Class[] paramTypes = null;
		if (args != null) {
			paramTypes = new Class[args.length];
			for (int i = 0; i < args.length; ++i) {
				paramTypes[i] = args[i].getClass();
			}
		}
		Method m = obj.getClass().getMethod(methodName, paramTypes);
		return m.invoke(obj, args);
	}
	
	/**
	 * Called by ApAM when Notification message comes
	 * and forward it to client part by calling the sendService
	 * 
	 * @param notif the notification message from ApAM
	 */
	public void gotNotification(NotificationMsg notif) {
		logger.debug("Notification message receive, " + notif.JSONize());
		sendToClientService.send(notif.JSONize().toJSONString());
		//sendToClientService.send("notification", notif.JSONize());
		//executeCommand(null, "ENO57ce7", "getTemperature", new ArrayList<Object>());
	}

	/**
	 * Send all the devices description to one client
	 * @param clientId, the client connection identifier
	 */
	@SuppressWarnings("unchecked")
	public void getDevices(String clientId) {
		Iterator<AbstractObjectSpec> devices = abstractDevice.iterator();
		
		AbstractObjectSpec adev = null;
		if (devices != null) {
			JSONArray jsonDeviceList =  new JSONArray();
			
			while (devices.hasNext()) {
				adev = devices.next();
				jsonDeviceList.add(adev.getDescription());
			}
//			send(clientId, "listDevices", jsonDeviceList);
			sendToClientService.send("listDevices", jsonDeviceList);
		}else{
			logger.debug("No smart object detected.");
		}
	}

	/**
	 * Resolve the location manager dependency and return the reference
	 * @return the location manager
	 */
	public void getLocations() {
		sendToClientService.send("listLocations", locationManager.getJSONLocations());
	}

	/**
	 * Add new location in the place manager
	 * @param jsonObject, the JSON of the new location
	 */
	public void newLocation(JSONObject jsonObject) {
		locationManager.addPlace((String)jsonObject.get("id"), (String)jsonObject.get("name"));
	}
	
	/**
	 * Move an object from source location to destination location.
	 * 
	 * @param objectId, the object reference
	 * @param srcLocationId, the source location identifier
	 * @param destLocationId, the destination location identifier
	 */
	public void moveObject(AbstractObjectSpec objectId, String srcLocationId, String destLocationId) {
		locationManager.moveObject(objectId, srcLocationId, destLocationId);
	}

	/**
	 * update the specified location
	 * 
	 * @param jsonObject, the location update details
	 */
	public void updateLocation(JSONObject jsonObject) {
		//for now we could just rename a location
		locationManager.renameLocation((String)jsonObject.get("id"), (String)jsonObject.get("name"));
	}
}
