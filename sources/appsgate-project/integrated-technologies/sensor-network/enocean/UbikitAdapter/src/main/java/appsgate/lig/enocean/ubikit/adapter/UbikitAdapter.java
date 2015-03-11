package appsgate.lig.enocean.ubikit.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.enocean.ubikit.adapter.source.event.*;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubikit.PhysicalEnvironmentItem;
import org.ubikit.PhysicalEnvironmentItem.Type;
import org.ubikit.event.impl.EventGateImpl;
import org.ubikit.pem.event.*;
import org.ubikit.service.RootPhysicalEnvironmentModelService;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
import appsgate.lig.enocean.ubikit.adapter.listeners.EnOceanCommandListener;
import appsgate.lig.enocean.ubikit.adapter.spec.EnOceanPairingService;
import appsgate.lig.enocean.ubikit.adapter.spec.UbikitAdapterService;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.immotronic.ubikit.pems.enocean.event.in.ActuatorUpdateEvent;
import fr.immotronic.ubikit.pems.enocean.event.in.TurnOffActuatorEvent;
import fr.immotronic.ubikit.pems.enocean.event.in.TurnOnActuatorEvent;

/**
 * This class is used to connect the ubikit system to the ApAM environment. An
 * object of this class translate "NewItemEvent" to ApAM instances with the
 * adapted ApAM implementation.
 * 
 * This class it is also an OSGi/iPOJO bundle, that provide two services and
 * instanciate itself automatically.
 * 
 * @author Cédric Gérard
 * @since January 8, 2013
 * @version 1.0.0
 * 
 * 
 * @provide EnOceanPairingService to manage the pairing mode for Ubikit
 * @provide EnOceanService to get all the actually paired sensors and manage the
 *          sensor configuration.
 * 
 * @see EnOceanPairingService
 * @see UbikitAdapterService
 * 
 */

public class UbikitAdapter extends CoreObjectBehavior implements
		UbikitAdapterService, EnOceanPairingService, CoreObjectSpec {

	/**
	 * Event management members
	 */
	private EventGateImpl eventGate;
	private ScheduledExecutorService executorService;
	private ScheduledExecutorService instanciationService;
	private HashMap<String, Instance> sidToInstanceName;
	private HashMap<String, ArrayList<EnOceanProfiles>> tempEventCapabilitiesMap;

	/**
	 * iPOJO EnOcean PEM resolution
	 */
	private RootPhysicalEnvironmentModelService enoceanBridge;

	/**
	 * Service to be notified when clients send commands
	 */
	private ListenerService listenerService;

	/**
	 * Service to communicate with clients
	 */
	private SendWebsocketsService sendToClientService;

	/**
	 * HTTP service dependency resolve by iPojo. Allow to register HTML
	 * resources to the Felix HTTP server
	 */
	private HttpService httpService;

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(UbikitAdapter.class);

	public static String CONFIG_TARGET = "ENOCEAN";

	/**
	 * constructor to initiate event members
	 */
	public UbikitAdapter() {
	}

	/**
	 * Called by iPOJO when all dependencies are available
	 */
	// @Validate
	public void newInst() {
        eventGate = new EventGateImpl();
        logger.debug("EventGate instanciated.");
        executorService = Executors.newScheduledThreadPool(5); // only one
        // thread for
        // the events
        // gate
        instanciationService = Executors.newScheduledThreadPool(1);
        logger.debug("ExecutorService instanciated.");
        sidToInstanceName = new HashMap<String, Instance>();
        tempEventCapabilitiesMap = new HashMap<String, ArrayList<EnOceanProfiles>>();


		logger.debug("PEM service = " + enoceanBridge.toString());
		logger.debug("Set as EnOcean observer");

		enoceanBridge.linkTo(eventGate);
		logger.debug("EnOcean PEM linked to the event gate connector");
		executorService.execute(eventGate.getEventDelivererTask());
		logger.debug("Event gate thread started");

		// The events gate is listening for all event coming
		// from paired sensors
//		eventGate.addListener(new enoceanPemListener());
        eventGate.addListener(new PEMSimpleListener(sendToClientService, this));
        logger.info("Simple listener registered");

		eventGate.addListener(new CO2Event(this));
		eventGate.addListener(new VOCEvent(this));
		eventGate.addListener(new HumidityEvent(this));
		eventGate.addListener(new TempEvent(this));
		eventGate.addListener(new SwitchEvent(this));
		eventGate.addListener(new LumEvent(this));
		eventGate.addListener(new MotionEvent(this));
		eventGate.addListener(new SetPointEvent(this));
		eventGate.addListener(new KeyCardEvent(this));
		eventGate.addListener(new ContactEvent(this));
		eventGate.addListener(new PairingModeEvent(this));
		eventGate.addListener(new MeteringEvent(this));
		logger.info("EnOcean proxy deployed and instanciated.");

		if (listenerService.addCommandListener(new EnOceanCommandListener(this), CONFIG_TARGET)) {
			logger.info("Configuration listeners deployed.");
			if (httpService != null) {
				logger.debug("HTTP service dependency resolved");
				final HttpContext httpContext = httpService
						.createDefaultHttpContext();
				final Dictionary<String, String> initParams = new Hashtable<String, String>();
				initParams.put("from", "HttpService");
				try {
					httpService.registerResources(
							"/configuration/sensors/enocean", "/WEB",
							httpContext);
					logger.info("Sensors configuration HTML GUI sources registered.");
				} catch (NamespaceException ex) {
					logger.error("NameSpace exception");
				}
			}

		} else {
			logger.info("Configuration listeners deployement failed.");
		}

		// Retrieve existing paired sensors from Ubikit and instanciate them.
		Collection<PhysicalEnvironmentItem> itemList = enoceanBridge
				.getAllItems();
        logger.debug("already paired sensors"+ enoceanBridge.getAllItems());
		if (itemList != null && !itemList.isEmpty()) {
			// Create thread that take the list in parameter and instanciate all
			// Ubikit items
			instanciationService.schedule(new ItemInstanciation(this,itemList), 15,
					TimeUnit.SECONDS);
		}

	}

	/**
	 * Called by iPOJO when the bundle is not available
	 */
	public void delInst() {
		// logger.info("Removed PEM service = " + enoceanBridge.toString());

		eventGate.unlinkAll();
		executorService.shutdownNow();
		instanciationService.shutdown();
		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
			instanciationService.awaitTermination(5, TimeUnit.SECONDS);
			executorService = null;
			// executorService has terminated.
			logger.debug("Event gate thread terminated");
		} catch (InterruptedException e) {
			// executorService has probably terminated, but some problem
			// happened.
			logger.debug("Event gate thread crash at termination");
		}

		if (listenerService.removeCommandListener(CONFIG_TARGET)) {
			logger.info("EnOcean configuration listener removed.");
		} else {
			logger.warn("EnOcean configuration listener remove failed.");
		}

		logger.info("EnOcean PEM connector removed");
	}

	//TODO To delete, log method is death code
//	/**
//	 * Log all the EnOcean event from the EnOcean dongle.
//	 *
//	 */
//    public void log(String arg0) {
//		logger.info("!EnOcean event! " + arg0);
//
//		/****************************
//		 * EnOcean telegram parsing * for not supported event *
//		 ****************************/
//		// RECV < 55 | 0 7 7 1 | 7a | f6 70 0 27 b3 ed 30 1 ff ff ff ff 2d 0 |
//		// 76 > FROM 27b3ed (-45 dBm)
//		String[] splited = arg0.split("FROM");
//		if (splited.length > 1) { // if arg0 is a received message FROM xxxx
//			String split1 = splited[1];
//			String id = "";
//			for (int i = 1; i < 7; i++) {
//				id += split1.charAt(i);
//			}
//			id = "ENO" + id;
//			Instance inst = getSensorInstance(id);
//			if (inst != null) {
//				logger.info("Paired sensor found " + id);
//				// TODO replace the use of userType by something directly on
//				// EnOceanProfile
//				String userType = inst.getProperty("userType");
//				if (userType.contentEquals("2")) { // Switch sensor
//					// Check the event
//					String split0 = splited[0];
//					String[] splited1 = split0.split("<");
//					String enoceanTg = splited1[1].trim();
//
//					if (enoceanTg.charAt(23) == '0') {
//						// The switch is set to neutral position
//						String switchNumber = inst.getProperty("switchNumber");
//						logger.info("The switch " + id
//								+ ", state changed to neutral with button  "
//								+ switchNumber);
//						inst.setProperty("buttonStatus", "none");
//						inst.setProperty("switchNumber", switchNumber);
//						inst.setProperty("switchState", "true");
//					}
//				}
//
//				splited = split1.split("\\(");
//				String signalDBM = splited[1];
//				signalDBM = signalDBM.substring(0, 3);
//				inst.setProperty("signal", signalDBM);
//			}
//		}
//	}

	/**
	 * Get all the EnOcean paired item from Ubikit use when the bundle restart.
	 * 
	 * @see UbikitAdapterService
	 */
	@Override
	public JSONArray getAllItem() {
		Collection<PhysicalEnvironmentItem> enOceanDeviceList = enoceanBridge
				.getAllItems();

		Iterator<PhysicalEnvironmentItem> it = enOceanDeviceList.iterator();
		PhysicalEnvironmentItem pei;
		// Instance apamInst;
		JSONArray allJSONItem = new JSONArray();

		while (it.hasNext()) {
			pei = it.next();
			// apamInst = sidToInstanceName.get(pei.getUID());
			// logger.debug(apamInst.getAllProperties().keySet().toString());
			allJSONItem.put(pei.getUID());
		}
        logger.debug("getAllItem(), returning"+allJSONItem.toString());


        return allJSONItem;
	}

	/**
	 * Get the specified ubikit item.
	 * 
	 * @param id
	 *            , the sensor item id.
	 * @see UbikitAdapterService
	 */
	@Override
	public JSONObject getItem(String id) {
		PhysicalEnvironmentItem item = enoceanBridge.getItem(id);
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", item.getUID());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * Get a JSON description of the capabilities associate to the specified
	 * item id
	 */
	@Override
	public JSONArray getItemCapabilities(String id) {
		ArrayList<EnOceanProfiles> capas = tempEventCapabilitiesMap.get(id);

		Iterator<EnOceanProfiles> it = capas.iterator();
		EnOceanProfiles ep;
		JSONArray capList = new JSONArray();
		HashMap<String, String> map;

		while (it.hasNext()) {
			ep = it.next();
			map = new HashMap<String, String>();
			map.put("profile", ep.name());
			map.put("type", ep.getUserFriendlyName());
			capList.put(new JSONObject(map));
		}
		return capList;
	}

	@Override
	public void turnOnActuator(String targetID) {
		logger.debug("Turn on --> " + targetID);
		eventGate.postEvent(new TurnOnActuatorEvent(targetID));
	}

	@Override
	public void turnOffActuator(String targetID) {
		logger.debug("Turn off --> " + targetID);
		eventGate.postEvent(new TurnOffActuatorEvent(targetID));
	}

	@Override
	public void sendActuatorUpdateEvent(String targetID) {
		logger.debug("Actuator update --> " + targetID);
		eventGate.postEvent(new ActuatorUpdateEvent(targetID));
	}

	/**
	 * Send the pairing mode event corresponding to pair parameter.
	 * 
	 * @param pair
	 *            , the new pairing mode
	 * @see EnOceanPairingService
	 */
	@Override
	public void setPairingMode(boolean pair) {
		logger.debug("Set pairing mode to: " + pair);
		if (pair) {
			eventGate.postEvent(new EnterPairingModeEvent());
			logger.debug("pairing mode on event sent");

		} else {
			eventGate.postEvent(new ExitPairingModeEvent());
			logger.debug("pairing mode off event sent");
		}

		// TODO removed this call when PairingModeEvent work
		pairingModeChanged(pair);
	}

	/**
	 * Method call to validate sensor configuration and validate the pairing
	 * sequence with Ubikit.
	 * 
	 * @param sensorID
	 *            , the item id
	 * @param capList
	 *            , the EnOcean capabilities list
	 * @param doesCapabilitiesHaveToBeSelected
	 *            , boolean that indicate if we have to specified capabilities
	 *            for this sensor (if it ambiguous)
	 */
	@Override
	public void validateItem(String sensorID, ArrayList<String> capList,
			boolean doesCapabilitiesHaveToBeSelected) {
		logger.debug("validateItem(String sensorID = " + sensorID
        +", ArrayList<String> capList = "+(capList==null?null:capList.toArray())
        + ", boolean doesCapabilitiesHaveToBeSelected = "+doesCapabilitiesHaveToBeSelected
        + ")");
		AddItemEvent addItEvent = new AddItemEvent(sensorID);

		if (doesCapabilitiesHaveToBeSelected) {
			logger.debug("item which capabilities have to be selected");
			addItEvent.addCapabilities(capList.toArray(new String[2]));

			ArrayList<EnOceanProfiles> profilesList = new ArrayList<EnOceanProfiles>();
			Iterator<String> capIt = capList.iterator();
			String capa;

			while (capIt.hasNext()) {
				capa = capIt.next();
				profilesList.add(EnOceanProfiles.getEnOceanProfile(capa));
			}

			tempEventCapabilitiesMap.put(sensorID, profilesList);
			logger.debug("Capability selected");
		}


        eventGate.postEvent(addItEvent);
		logger.debug("item validated " + sensorID);
	}

	/**
	 * Unpair a device based on its UID
	 * @param uid
	 * @return returns 'true' case the device was removes, or 'false' otherwise
	 */
	public boolean unpairDevice (String uid){
		logger.debug("Unparing device with UID {} from ubikit",uid);
		PhysicalEnvironmentItem item=enoceanBridge.removeItem(uid);
		Boolean result=item!=null;
		logger.debug("Unparing device with UID {} resulted in {}",uid,result);
		Instance apamInstance=sidToInstanceName.remove(uid);
		((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(apamInstance.getName());
		return result;
	}

	/**
	 * Instantiate an already configure item from Ubikit database Pretty similar
	 * to the action done with ItemAddedEvent received but does not notify
	 * client just push the instance to ApAM layer.
	 * 
	 * @param item
	 *            the device to instantiate
	 */
	public void instanciateItem(PhysicalEnvironmentItem item) {
		logger.debug("instanciateItem(PhysicalEnvironmentItem item, UID = "+(item==null?null:item.getUID())
                +", capabilities = "+(item==null?null:item.getCapabilities())   );
		EnOceanProfiles ep = EnOceanProfiles.EEP_00_00_00;
		Implementation impl = null;
		Map<String, String> properties = new HashMap<String, String>();

		if (item.getType().equals(Type.SENSOR)
				|| item.getType().equals(Type.SENSOR_AND_ACTUATOR)) {
			if (item.getCapabilities().length == 1) {
				String capabilitie = item.getCapabilities()[0];
				ep = EnOceanProfiles.getEnOceanProfile(capabilitie);
			} else {
				ArrayList<EnOceanProfiles> profilesList = tempEventCapabilitiesMap
						.get(item.getUID());
				// TODO manage for multiple profiles sensors.
				ep = profilesList.iterator().next();
				tempEventCapabilitiesMap.remove(item.getUID());
			}
			properties.put("isPaired", "true");

		} else if (item.getType().equals(Type.ACTUATOR)) {
			String capabilitie = item.getCapabilities()[0];
			ep = EnOceanProfiles.getEnOceanProfile(capabilitie);
			properties.put("isPaired", "false");
		}

		int nbTry = 0;
		while (nbTry < 5) {
			impl = CST.apamResolver.findImplByName(null,
					ep.getApAMImplementation());
			if (impl != null) {
				properties.put("deviceName", ep.getUserFriendlyName());
				properties.put("deviceId", item.getUID());
				properties.put("deviceType", ep.name());

				Instance createInstance = impl.createInstance(null, properties);
				sidToInstanceName.put(item.getUID(), createInstance);
				nbTry = 5;
			} else {
				synchronized (this) {
					try {
						logger.error("No " + ep.getApAMImplementation()
								+ " found ! -- " + nbTry + " try");
						wait(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				nbTry++;
			}
		}
	}

	/**
	 * Get the ApAM instance corresponding to a specified sensor ID
	 * 
	 * @param uid
	 *            , the id of the sensor
	 * @return an ApAM instance
	 */
	public Instance getSensorInstance(String uid) {
		return sidToInstanceName.get(uid);
	}

	/**
	 * Send the new pairing mode to client part.
	 * 
	 * @param mode
	 *            the new pairing status
	 */
	public void pairingModeChanged(boolean mode) {
		JSONObject pairingState = new JSONObject();
		JSONObject enoceanMsg = new JSONObject();
		try {
            logger.debug("pairingModeChanged(boolean mode : "+mode+" )");
			pairingState.put("pairingMode", mode);
			enoceanMsg.put("pairingModeChanged", pairingState);
			enoceanMsg.put("TARGET", UbikitAdapter.CONFIG_TARGET);
			fireNotificationMessage("pairingMode", null, String.valueOf(mode));

		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendToClientService.send(enoceanMsg.toString());
	}
	
	public NotificationMsg fireNotificationMessage(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this.getAbstractObjectId());
	}


	// /**
	// * Create and send the newActuator event to ubikit
	// *
	// * @param profile the actuator profile
	// * @param name the actuator name
	// * @param place the place where it be
	// */
	// public void createActuator(String profile, String name, String place, int
	// clientId) {
	// ActuatorProfile ap = EnOceanProfiles.getActuatorProfile(profile);
	// if(ap != null) {
	//
	// CreateNewActuatorEvent ev = new CreateNewActuatorEvent(ap, name, place);
	// eventGate.postEvent(ev);
	// }else {
	// JSONObject error = new JSONObject();
	// try {
	// error.put("code", "0001");
	// error.put("description",
	// "No ubikit<>appsgate actuator profile found !");
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// sendToClientService.send(clientId, "actuatorError", error);
	// }
	// }

	/**
	 * Send all paired actuators and all existing actuator profiles
	 */
	public void getConfDevices(int clientId) {
		JSONObject enoceanConfJSON = new JSONObject();
		JSONObject resp = new JSONObject();
		// JSONArray actuatorsProfiles = new JSONArray();

		// actuatorsProfiles.put(EnOceanProfiles.getActuatorProfiles());

		try {
			//actuatorsJSON.put("actuatorProfiles", EnOceanProfiles.getActuatorProfiles());
			enoceanConfJSON.put("enoceanDevices", getAllItem());
			//TODO Get the real EnOcean PEM pairing state
			setPairingMode(false);
			enoceanConfJSON.put("pairingMode", false);
			//TODO Get serial information from pemi string

			resp.put("TARGET", UbikitAdapter.CONFIG_TARGET);
			resp.put("confDevices", enoceanConfJSON);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		logger.debug(enoceanConfJSON.toString());
		sendToClientService.send(clientId, resp.toString());
	}



    public void addSidToInstance(String sid, Instance instance) {
        sidToInstanceName.put(sid, instance);
    }

    public boolean containSid(String sid) {
        return sidToInstanceName.containsKey(sid);
    }

    public void addTempEventCapability(String sid,ArrayList<EnOceanProfiles> tempCapList ) {
        tempEventCapabilitiesMap.put(sid,
                tempCapList);
    }

    public ArrayList<EnOceanProfiles> getTempEventCapability(String sid ) {
        return tempEventCapabilitiesMap.get(sid);
    }

    public void removeTempEventCapability(String sid ) {
        tempEventCapabilitiesMap.remove(sid);
    }
    
    
    /**
     * CoreObject Stuff
     */
	private String serviceId;
	private String userType;
	private int status;

	@Override
	public String getAbstractObjectId() {
		return serviceId;
	}

	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public int getObjectStatus() {
		return status;
	}


	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", serviceId);
		descr.put("type", userType);
		descr.put("status", status);
		descr.put("pairingMode", "unknown");

		return descr;
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.ADAPTER;
	}

}