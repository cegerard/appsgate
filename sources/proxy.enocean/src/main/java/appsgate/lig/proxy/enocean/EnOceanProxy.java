package appsgate.lig.proxy.enocean;

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

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubikit.PhysicalEnvironmentItem;
import org.ubikit.PhysicalEnvironmentModelObserver;
import org.ubikit.pem.event.*;
import org.ubikit.pem.event.NewItemEvent.CapabilitySelection;
import org.ubikit.service.PhysicalEnvironmentModelService;
import org.ubikit.PhysicalEnvironmentItem.Type;
import org.ubikit.event.impl.EventGateImpl;

import appsGate.lig.manager.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.communication.service.subscribe.AddListenerService;
import appsgate.lig.proxy.enocean.source.event.ContactEvent;
import appsgate.lig.proxy.enocean.source.event.KeyCardEvent;
import appsgate.lig.proxy.enocean.source.event.LumEvent;
import appsgate.lig.proxy.enocean.source.event.MotionEvent;
import appsgate.lig.proxy.enocean.source.event.PairingModeEvent;
import appsgate.lig.proxy.enocean.source.event.SetPointEvent;
import appsgate.lig.proxy.enocean.source.event.SwitchEvent;
import appsgate.lig.proxy.enocean.source.event.TempEvent;
import appsgate.lig.proxy.listeners.EnOceanConfigListener;
import appsgate.lig.proxy.services.EnOceanPairingService;
import appsgate.lig.proxy.services.EnOceanService;

import fr.immotronic.ubikit.pems.enocean.ActuatorProfile;
import fr.immotronic.ubikit.pems.enocean.event.in.CreateNewActuatorEvent;
import fr.immotronic.ubikit.pems.enocean.event.in.TurnOffActuatorEvent;
import fr.immotronic.ubikit.pems.enocean.event.in.TurnOnActuatorEvent;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;

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
 * @see EnOceanService
 * 
 */
@Component
@Instantiate
@Provides(specifications = { EnOceanPairingService.class, EnOceanService.class })
public class EnOceanProxy implements PhysicalEnvironmentModelObserver,
		EnOceanService, EnOceanPairingService, NewItemEvent.Listener,
		ItemAddedEvent.Listener, UnsupportedNewItemEvent.Listener,
		ItemAddingFailedEvent.Listener {

	/**
	 * Event management members
	 */
	private EventGateImpl eventGate;
	private ScheduledExecutorService executorService;
	private HashMap<String, Instance> sidToInstanceName;
	private HashMap<String, ArrayList<EnOceanProfiles>> tempEventCapabilitiesMap;

	/**
	 * iPOJO EnOcean PEM resolution
	 */
	private PhysicalEnvironmentModelService enoceanBridge;

	/**
	 * Service to be notified when clients send commands
	 */
	private AddListenerService addListenerService;

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
	private static Logger logger = LoggerFactory.getLogger(EnOceanProxy.class);

	/**
	 * constructor to initiate event members
	 */
	public EnOceanProxy() {
		eventGate = new EventGateImpl();
		logger.debug("EventGate instanciated.");
		executorService = Executors.newScheduledThreadPool(1); // only one
																// thread for
																// the events
																// gate
		logger.debug("ExecutorService instanciated.");
		sidToInstanceName = new HashMap<String, Instance>();
		tempEventCapabilitiesMap = new HashMap<String, ArrayList<EnOceanProfiles>>();
	}

	/**
	 * Called by iPOJO when all dependencies are available
	 */
	@Validate
	public void newInst() {
		logger.debug("PEM service = " + enoceanBridge.toString());
		enoceanBridge.setObserver(this);
		logger.debug("Set as EnOcean observer");

		enoceanBridge.linkTo(eventGate);
		logger.debug("EnOcean PEM linked to the event gate connector");
		executorService.execute(eventGate.getEventDelivererTask());
		logger.debug("Event gate thread started");

		// The events gate is listening for all event coming
		// from paired sensors
		eventGate.addListener(this);
		eventGate.addListener(new TempEvent(this));
		eventGate.addListener(new SwitchEvent(this));
		eventGate.addListener(new LumEvent(this));
		eventGate.addListener(new MotionEvent(this));
		eventGate.addListener(new SetPointEvent(this));
		eventGate.addListener(new KeyCardEvent(this));
		eventGate.addListener(new ContactEvent(this));
		eventGate.addListener(new PairingModeEvent(this));
		
		if (httpService != null) {
			final HttpContext httpContext = httpService.createDefaultHttpContext();
			final Dictionary<String, String> initParams = new Hashtable<String, String>();
			initParams.put("from", "HttpService");
			try {
				httpService.registerResources("/configuration/sensors", "/WEB", httpContext);
				logger.info("Sensors configuration HTML GUI sources registered.");
			} catch (NamespaceException ex) {
				logger.error("NameSpace exception");
			}
		}
		logger.info("EnOcean proxy deployed and instanciated.");
		
		logger.info("Getting the listeners services...");
		if(addListenerService.addConfigListener(new EnOceanConfigListener(this))){
			logger.info("Listeners services dependency resolved.");
		}else{
			logger.info("Listeners services dependency resolution failed.");
		}

	}

	/**
	 * Called by iPOJO when the bundle is not available
	 */
	@Invalidate
	public void deleteInst() {
		// logger.info("Removed PEM service = " + enoceanBridge.toString());

		eventGate.unlinkAll();
		executorService.shutdownNow();
		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
			executorService = null;
			// executorService has terminated.
			logger.debug("Event gate thread terminated");
		} catch (InterruptedException e) {
			// executorService has probably terminated, but some problem
			// happened.
			logger.debug("Event gate thread crash at termination");
		}

		logger.info("EnOcean PEM connector removed");
	}

	/**
	 * Get the EnOcean PEM service form OSGi/iPOJO. This service is required.
	 * 
	 * @param PEMService
	 *            , the physical environment model OSGi service
	 */
	@Bind(optional = false)
	public void bindPEMService(PhysicalEnvironmentModelService PEMService) {
		enoceanBridge = PEMService;
		logger.debug("EnOcean PEM service dependency resolved");
	}

	/**
	 * Call when the EnOcean proxy release the required physical environment
	 * model OSGi service.
	 * 
	 * @param PEMService
	 *            , the released physical environment model OSGi service
	 */
	@Unbind(optional = false)
	public void unbindPEMService(PhysicalEnvironmentModelService PEMService) {
		enoceanBridge = null;
		logger.debug("EnOcean PEM service dependency not available");
	}

	/**
	 * Get the subcribe service form OSGi/iPOJO. This service is optional.
	 * 
	 * @param addListenerService
	 *            , the subscription service
	 */
	@Bind(optional = true)
	public void bindSubscriptionService(AddListenerService addListenerService) {
		this.addListenerService = addListenerService;
		logger.debug("Communication subscription service dependency resolved");
	}

	/**
	 * Call when the EnOcean proxy release the optional subscription service.
	 * 
	 * @param addListenerService
	 *            , the released subscription service
	 */
	@Unbind(optional = true)
	public void unbindSubscriptionService(AddListenerService addListenerService) {
		this.addListenerService = null;
		logger.debug("Subscription service dependency not available");
	}

	/**
	 * Get the communication service from OSGi/iPojo. This service is optional.
	 * 
	 * @param sendToClientService
	 *            , the communication service
	 */
	@Bind(optional = true)
	public void bindCommunicationService(SendWebsocketsService sendToClientService) {
		this.sendToClientService = sendToClientService;
		logger.debug("Communication service dependency resolved");
	}
	
	/**
	 * Call when the EnOcean proxy release the communication service.
	 * 
	 * @param sendToClientService
	 *            , the communication service
	 */
	@Unbind(optional = true)
	public void unbindCommunicationService(SendWebsocketsService sendToClientService) {
		this.sendToClientService = null;
		logger.debug("Communication service dependency not available");
	}
	
	/**
	 * Get the HTTP service form OSGi/iPojo. This service is optional.
	 * 
	 * @param httpService, the HTTP service
	 */
	@Bind(optional = true)
	public void bindHTTPService(HttpService httpService) {
		this.httpService = httpService;
		logger.debug("HTTP service dependency resolved");
	}
	
	/**
	 * Call when the EnOcean proxy release the HTTP service.
	 * 
	 * @param httpService, the HTTP service
	 */
	@Unbind(optional = true)
	public void unbindHTTPService(HttpService httpService) {
		this.httpService = null;
		logger.debug("HTTP service dependency not available");
	}

	/**
	 * Log all the EnOcean event from the EnOcean dongle.
	 * 
	 * @see PhysicalEnvironmentModelObserver
	 */
	// @Override
	public void log(String arg0) {
		logger.info("!EnOcean event! " + arg0);
	}

	/**
	 * Get all the EnOcean paired item from Ubikit use when the bundle restart.
	 * 
	 * @see EnOceanService
	 */
	// Override
	@SuppressWarnings("unchecked")
	public JSONArray getAllItem() {
		Collection<PhysicalEnvironmentItem> enOceanDeviceList = enoceanBridge.getAllItems();
		
		Iterator<PhysicalEnvironmentItem> it = enOceanDeviceList.iterator();
		PhysicalEnvironmentItem pei;
		Instance apamInst;
		JSONArray allJSONItem = new JSONArray();
		
		while(it.hasNext()) {
			pei = it.next();
			apamInst = sidToInstanceName.get(pei.getUID());
			logger.debug(apamInst.getAllProperties().keySet().toString());
			allJSONItem.add(pei.getUID());
		}
		
		return allJSONItem;
	}

	/**
	 * Get the specified ubikit item.
	 * 
	 * @param id
	 *            , the sensor item id.
	 * @see EnOceanService
	 */
	// Override
	@SuppressWarnings("unchecked")
	public JSONObject getItem(String id) {
		PhysicalEnvironmentItem item = enoceanBridge.getItem(id);
		JSONObject obj = new JSONObject();
		obj.put("id", item.getUID());
		return obj;
	}

	/**
	 * Get a JSON description of the capabilities associate to the specified
	 * item id
	 */
	// @Override
	@SuppressWarnings("unchecked")
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
			capList.add(new JSONObject(map));
		}
		return capList;
	}

	/**
	 * Post the turn on event to the specified target id.
	 * 
	 * @param targetID
	 *            , the actuator id
	 */
	//@Override
	public void turnOnActuator(String targetID) {
		eventGate.postEvent(new TurnOnActuatorEvent(targetID));
	}

	/**
	 * Post the turn off event to the specified target id.
	 * 
	 * @param targetID
	 *            , the actuator id.
	 */
	//@Override
	public void turnOffActuator(String targetID) {
		eventGate.postEvent(new TurnOffActuatorEvent(targetID));
	}

	/**
	 * Send the pairing mode event corresponding to pair parameter.
	 * 
	 * @param pair
	 *            , the new pairing mode
	 * @see EnOceanPairingService
	 */
	// @Override
	public void setPairingMode(boolean pair) {
		logger.debug("Set pairing mode to: " + pair);
		if (pair) {
			eventGate.postEvent(new EnterPairingModeEvent());
			logger.debug("pairing mode on event sent");

		} else {
			eventGate.postEvent(new ExitPairingModeEvent());
			logger.debug("pairing mode off event sent");
		}
		
		//TODO removed this call when PairingModeEvent work
		pairingModeChanged(pair);
	}

	/**
	 * This method is a listener method call when Ubikit is in paring mode and
	 * detect a new sensor.
	 * 
	 * @see NewItemEvent
	 */
	// @Override
	@SuppressWarnings("unchecked")
	public void onEvent(NewItemEvent newItEvent) {
		logger.debug("!NewItemEvent! from " + newItEvent.getSourceItemUID()
				+ " to " + newItEvent.getPemUID() + ", type="
				+ newItEvent.getItemType());

		if (!sidToInstanceName.containsKey(newItEvent.getSourceItemUID())) {

			sidToInstanceName.put(newItEvent.getSourceItemUID(), null);

			CapabilitySelection cs = newItEvent.doesCapabilitiesHaveToBeSelected();

			if (cs == CapabilitySelection.NO) {			
				validateItem(newItEvent.getSourceItemUID(), null, false);

			} else if (cs == CapabilitySelection.SINGLE) {

				String[] capabilities = newItEvent.getCapabilities();
				int capListLength = capabilities.length;
				int i = 0;
				ArrayList<EnOceanProfiles> tempCapList = new ArrayList<EnOceanProfiles>();
				while (i < capListLength) {
					tempCapList.add(EnOceanProfiles.getEnOceanProfile(capabilities[i]));
					i++;
				}
				tempEventCapabilitiesMap.put(newItEvent.getSourceItemUID(),tempCapList);
				JSONObject newUndefinedMsg =  new JSONObject();
				newUndefinedMsg.put("id", newItEvent.getSourceItemUID());
				newUndefinedMsg.put("capabilities", getItemCapabilities(newItEvent.getSourceItemUID()));
				sendToClientService.send("newUndefinedSensor", newUndefinedMsg);

			} else if (cs == CapabilitySelection.MULTIPLE) {
				logger.error("Multiple capabality not supported yet for "
						+ newItEvent.getSourceItemUID() + " to "
						+ newItEvent.getPemUID() + ", type="
						+ newItEvent.getItemType());
			}
		}
	}
	
	/**
	 * This method is a listener method call when Ubikit is in paring mode and
	 * notify that it detect a undefined sensor.
	 * 
	 * @see UnsupportedNewItemEvent
	 */
	// @Override
	@SuppressWarnings("unchecked")
	public void onEvent(UnsupportedNewItemEvent unsupportedItEvent) {
		logger.debug("!UnsupportedNewItemEvent! from "
				+ unsupportedItEvent.getSourceItemUID() + " to "
				+ unsupportedItEvent.getPemUID() + ", type"
				+ unsupportedItEvent.getItemType());
		
		JSONObject newUnsupportedMsg =  new JSONObject();
		newUnsupportedMsg.put("id", unsupportedItEvent.getSourceItemUID());
		newUnsupportedMsg.put("capabilities", getItemCapabilities(unsupportedItEvent.getSourceItemUID()));
		sendToClientService.send("newUnsupportedSensor", newUnsupportedMsg);
		
	}

	/**
	 * This method is a listener method call when Ubikit is in paring mode and
	 * notify that a new sensor is paired.
	 * 
	 * @see ItemAddedEvent
	 */
	// @Override
	@SuppressWarnings("unchecked")
	public void onEvent(ItemAddedEvent addItEvent) {
		logger.debug("!ItemAddedEvent! from " + addItEvent.getSourceItemUID() + " to " + addItEvent.getPemUID() + ", type "+ addItEvent.getItemType());
		
		EnOceanProfiles ep = EnOceanProfiles.EEP_00_00_00;
		Implementation impl = null;
		Map<String, String> properties = new HashMap<String, String>();
		
		if(addItEvent.getItemType().equals(Type.SENSOR)) {
			if (addItEvent.getCapabilities().length == 1) {
				String capabilitie = addItEvent.getCapabilities()[0];
				ep = EnOceanProfiles.getEnOceanProfile(capabilitie);	
			} else {
				ArrayList<EnOceanProfiles> profilesList = tempEventCapabilitiesMap.get(addItEvent.getSourceItemUID());
				//TODO manage for multiple profiles sensors.
				ep = profilesList.iterator().next();
				tempEventCapabilitiesMap.remove(addItEvent.getSourceItemUID());
			}
			properties.put("isPaired", "true");
			
		}else if(addItEvent.getItemType().equals(Type.ACTUATOR)) {
			String capabilitie = addItEvent.getCapabilities()[0];
			ep = EnOceanProfiles.getEnOceanProfile(capabilitie);
			properties.put("isPaired", "false");
			try {
				org.json.JSONObject obj = addItEvent.getUserProperties();
				properties.put("userName", obj.getString("CustomName"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		impl = CST.apamResolver.findImplByName(null, ep.getApAMImplementation());
		
		properties.put("deviceName", ep.getUserFriendlyName());
		properties.put("deviceId", addItEvent.getSourceItemUID());
		properties.put("deviceType", ep.name());
		
		Instance createInstance = impl.createInstance(null, properties);
		sidToInstanceName.put(addItEvent.getSourceItemUID(), createInstance);
		
		//Notify configuration UI
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("id", addItEvent.getSourceItemUID());
		jsonObj.put("name", properties.get("userName"));
		jsonObj.put("type", addItEvent.getItemType().name());
		jsonObj.put("deviceType", ep.name());
		jsonObj.put("paired", properties.get("isPaired"));
		sendToClientService.send("newObject", jsonObj);
	}

	/**
	 * This method is a listener method call when Ubikit is in paring mode and
	 * notify that the pairing sequence failed for this sensor.
	 * 
	 * @see ItemAddingFailedEvent
	 */
	// @Override
	@SuppressWarnings("unchecked")
	public void onEvent(ItemAddingFailedEvent addFailedEvent) {
		logger.debug("!ItemAddingFailedEvent! from "
				+ addFailedEvent.getSourceItemUID() + " Error Code = "
				+ addFailedEvent.getErrorCode() + ", Reason = "
				+ addFailedEvent.getReason());
		JSONObject pairingFailedMsg =  new JSONObject();
		pairingFailedMsg.put("id", addFailedEvent.getSourceItemUID());
		pairingFailedMsg.put("capabilities", getItemCapabilities(addFailedEvent.getSourceItemUID()));
		pairingFailedMsg.put("code", addFailedEvent.getErrorCode());
		pairingFailedMsg.put("reason", addFailedEvent.getReason());
		sendToClientService.send("pairingFailed", pairingFailedMsg);
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
	// @Override
	public void validateItem(String sensorID, ArrayList<String> capList, boolean doesCapabilitiesHaveToBeSelected) {

		logger.debug("validateItem call received for " + sensorID);
		AddItemEvent addItEvent = new AddItemEvent(sensorID);

		if (doesCapabilitiesHaveToBeSelected) {
			addItEvent.addCapabilities(capList.toArray(new String[2]));
			
			ArrayList<EnOceanProfiles> profilesList = new ArrayList<EnOceanProfiles>();
			Iterator<String> capIt= capList.iterator();
			String capa;
			
			while(capIt.hasNext()) {
				capa = capIt.next();
				profilesList.add(EnOceanProfiles.getEnOceanProfile(capa));
			}
			
			tempEventCapabilitiesMap.put(sensorID, profilesList);
		}
		
		eventGate.postEvent(addItEvent);
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
	 * @param pairingState, the new pairing status
	 */
	@SuppressWarnings("unchecked")
	public void pairingModeChanged(boolean mode) {
		JSONObject pairingState = new JSONObject();
		pairingState.put("pairingMode", mode);
		sendToClientService.send("pairingModeChanged", pairingState);
	}
	
	/**
	 * Create and send the newActuator event to ubikit
	 * 
	 * @param profile, the actuator profile
	 * @param name, the actuator name
	 * @param place, the place where it be
	 */
	@SuppressWarnings("unchecked")
	public void createActuator(String profile, String name, String place) {
		ActuatorProfile ap = EnOceanProfiles.getActuatorProfile(profile);
		if(ap != null) {
			
			CreateNewActuatorEvent ev = new CreateNewActuatorEvent(ap, name, place);
			eventGate.postEvent(ev);
		}else {
			JSONObject error = new JSONObject();
			error.put("code", "0001");
			error.put("deescription", "No ubikit<>appsgate actuator profile found !");
			sendToClientService.send("actuatorError", error);
		}
	}
	
	/**
	 * Send all paired actuators and all existing actuator profiles
	 */
	@SuppressWarnings("unchecked")
	public void getActuator() {
		JSONObject actuatorsJSON = new JSONObject();
		JSONArray actuatorsProfiles = new JSONArray();
		
		actuatorsProfiles.addAll(EnOceanProfiles.getActuatorProfiles());
		
		actuatorsJSON.put("actuatorProfiles", actuatorsProfiles);
		actuatorsJSON.put("enoceanDevices", getAllItem());
		logger.debug(actuatorsJSON.toJSONString());
		sendToClientService.send("confDevices",actuatorsJSON);
	}

}
