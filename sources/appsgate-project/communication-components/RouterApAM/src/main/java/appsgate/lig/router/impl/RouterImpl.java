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
import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.core.object.spec.CoreObjectSpec.CORE_TYPE;
import appsgate.lig.main.spec.AppsGateSpec;
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
	private ListenerService addListenerService;

	/**
	 * Service to communicate with clients
	 */
	private SendWebsocketsService sendToClientService;
	
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
		try{
			//notify that a new device, service or simulated instance appeared
			CoreObjectSpec newObj = (CoreObjectSpec)inst.getServiceObject();
			if(newObj.getCoreType().equals(CORE_TYPE.DEVICE)) {
				sendToClientService.send("newDevice", getObjectDescription(newObj, ""));
				appsgate.addNewDeviceSpace(getObjectDescription(newObj, ""));
			}else if (newObj.getCoreType().equals(CORE_TYPE.SERVICE)) {
				sendToClientService.send("newService", getObjectDescription(newObj, ""));
				appsgate.addNewServiceSpace(getObjectDescription(newObj, ""));
			}else if (newObj.getCoreType().equals(CORE_TYPE.SIMULATED_DEVICE)) {
				sendToClientService.send("newSimulatedDevice", getObjectDescription(newObj, ""));
				//TODO manage the simulated device
				logger.debug("Simulated device core type not supported yet for EHMI");
			}else if (newObj.getCoreType().equals(CORE_TYPE.SIMULATED_SERVICE)) {
				sendToClientService.send("newSimulatedService", getObjectDescription(newObj, ""));
				//TODO manage the simulated service
				logger.error("Simulated service core type not supported yet for EHMI");
			}
		}catch(Exception ex) {
			logger.error("If getCoreType method error trace appeare below it is because the service or the device doesn't implement all methode in" +
					"the CoreObjectSpec interface but this error doesn't impact the EHMI.");
			ex.printStackTrace();
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
		String deviceId = inst.getProperty("deviceId");
		JSONObject obj = new JSONObject();
		try {
			obj.put("objectId", deviceId);
		} catch (JSONException e) {e.printStackTrace();}
		CoreObjectSpec rmObj = (CoreObjectSpec)inst.getServiceObject();
		
		if(rmObj.getCoreType().equals(CORE_TYPE.DEVICE)) {
			sendToClientService.send("removeDevice",  obj);
			appsgate.removeDeviceSpace(deviceId, inst.getProperty("userType"));
		}else if (rmObj.getCoreType().equals(CORE_TYPE.SERVICE)) {
			sendToClientService.send("removeService",  obj);
			appsgate.removeServiceSpace(deviceId, inst.getProperty("userType"));
		}else if (rmObj.getCoreType().equals(CORE_TYPE.SIMULATED_DEVICE)) {
			sendToClientService.send("removeSimulatedDevice",  obj);
			//TODO manage the simulated device
		}else if (rmObj.getCoreType().equals(CORE_TYPE.SIMULATED_SERVICE)) {
			sendToClientService.send("removeSimulatedService",  obj);
			//TODO manage the simulated service
		}
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
			logger.debug("Notification message received, " + notif.JSONize());
			sendToClientService.send(notif.JSONize().toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send all the devices description to one client
	 */
	@Override
	public JSONArray getDevices() {
		Iterator<CoreObjectSpec> devices = abstractDevice.iterator();
		
		CoreObjectSpec adev = null;
		
		if (devices != null) {
			JSONArray jsonDeviceList =  new JSONArray();
			
			while (devices.hasNext()) {
				adev = devices.next();
				jsonDeviceList.put(getObjectDescription(adev, ""));
			}
			
			return jsonDeviceList;
			
		}else{
			logger.debug("No smart object detected.");
			return new JSONArray();
		}
	}
	
	@Override
	public JSONObject getDevice(String objectId) {
		
		Object obj = getObjectRefFromID(objectId);
		
		if(obj != null){
			CoreObjectSpec objSpec =(CoreObjectSpec)obj;
			return  getObjectDescription(objSpec, "");
		}
		
		return new JSONObject();
	}
	
	@Override
	public JSONArray getDevices(String type) {
		Iterator<CoreObjectSpec> devices = abstractDevice.iterator();
		
		CoreObjectSpec adev = null;
		
		if (devices != null) {
			JSONArray jsonDeviceList =  new JSONArray();
			
			while (devices.hasNext()) {
				adev = devices.next();
				if(type.contentEquals(adev.getUserType())) {
					jsonDeviceList.put(getObjectDescription(adev, ""));
				}
			}
			
			return jsonDeviceList;
			
		}else{
			logger.debug("No smart object detected.");
			return new JSONArray();
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
			// Get object auto description
			JSONDescription = obj.getDescription();
			
			// Get the user type of the device and put 
			// a string that allow internationalization
			// of the type string 
			int userType = Integer.valueOf(obj.getUserType());
			switch(userType) {
			
				/** Devices  **/
				case 0: //Temperature
					JSONDescription.put("name", "devices.temperature.name.singular");
					break;
				
				case 1: //Illumination
					JSONDescription.put("name", "devices.illumination.name.singular");
					break;
				
				case 2: //Switch
					JSONDescription.put("name", "devices.switch.name.singular");
					break;
				
				case 3: //Contact
					JSONDescription.put("name", "devices.contact.name.singular");
					break;
				
				case 4: //Key card Switch
					JSONDescription.put("name", "devices.keycard-reader.name.singular");
					break;
					
				case 5: //Occupancy
					JSONDescription.put("name", "devices.occupancy.name.singular");
					break;
				
				case 6: //Smart plug
					JSONDescription.put("name", "devices.plug.name.singular");
					break;
				
				case 7: //PhilipsHUE
					JSONDescription.put("name", "devices.lamp.name.singular");
					break;
					
				case 8: //On/Off actuator
					JSONDescription.put("name", "devices.actuator.name.singular");
					break;
				
				case 9: //CO2
					JSONDescription.put("name", "devices.co2.name.singular");
					break;
				/** AppsGate System services **/
				case 21: //System clock
					JSONDescription.put("name", "devices.clock.name.singular");
					break;
					
				/** UPnP devices **/
				case 31: //Media player
					JSONDescription.put("name", "devices.mediaplayer.name.singular");
					break;
					
				case 36: //Media browser
					JSONDescription.put("name", "devices.mediabrowser.name.singular");
					break;
					
				/** UPnP services **/
				case 415992004: //AV Transport
					JSONDescription.put("name", "devices.avtransport.name.singular");
					break;
				case 794225618: //Content directory
					JSONDescription.put("name", "devices.contentdirectory.name.singular");
					break;
				case 2052964255: //Connection manager
					JSONDescription.put("name", "devices.connectionManager.name.singular");
					break;
				case -164696113: //Rendering control
					JSONDescription.put("name", "devices.renderingControl.name.singular");
					break;
				case -532540516: //???
					JSONDescription.put("name", "devices.unknow");
					break;
				case -1943939940: //???
					JSONDescription.put("name", "devices.unknow");
					break;
				
				/** Web services **/
				case 101: //Google calendar
					JSONDescription.put("name", "devices.googlecalendar.name.singular");
					break;
				case 102: //Mail
					JSONDescription.put("name", "devices.mail.name.singular");
					break;
				case 103: //Weather
					JSONDescription.put("name", "devices.weather.name.singular");
					break;
					
				/** Default **/
				default:
					JSONDescription.put("name", "devices.device-no-name");
				
			}

		} catch (JSONException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error("ApAM error");
			logger.error(e.getMessage());
		}
		return JSONDescription;
	}
	
}
