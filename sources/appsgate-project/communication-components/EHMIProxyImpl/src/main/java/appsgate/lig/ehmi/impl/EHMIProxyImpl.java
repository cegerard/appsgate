package appsgate.lig.ehmi.impl;

import appsgate.lig.manager.place.spec.PlaceManagerSpec;
import appsgate.lig.manager.place.spec.SymbolicPlace;
import appsgate.lig.scheduler.SchedulerSpec;
import appsgate.lig.scheduler.SchedulingException;
import appsgate.lig.weather.spec.WeatherAdapterSpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.*;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsgate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsgate.lig.manager.client.communication.service.subscribe.ListenerService;
import appsgate.lig.chmi.spec.CHMIProxySpec;
import appsgate.lig.chmi.spec.listeners.CoreEventsListener;
import appsgate.lig.chmi.spec.listeners.CoreUpdatesListener;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.context.dependency.spec.DependencyManagerSpec;
import appsgate.lig.context.device.properties.table.spec.DevicePropertiesTableSpec;
import appsgate.lig.context.userbase.spec.UserBaseSpec;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.ehmi.spec.GrammarDescription;
import appsgate.lig.ehmi.exceptions.CoreDependencyException;
import appsgate.lig.ehmi.exceptions.ExternalComDependencyException;
import appsgate.lig.ehmi.impl.listeners.EHMICommandListener;
import appsgate.lig.ehmi.impl.listeners.ObjectEventListener;
import appsgate.lig.ehmi.impl.listeners.ObjectUpdateListener;
import appsgate.lig.ehmi.impl.listeners.TimeObserver;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.SpokObject;
import appsgate.lig.ehmi.spec.StateDescription;
import appsgate.lig.ehmi.spec.listeners.CoreListener;
import appsgate.lig.ehmi.spec.messages.ClockAlarmNotificationMsg;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.ehmi.spec.trace.TraceManSpec;
import appsgate.lig.eude.interpreter.spec.EUDE_InterpreterSpec;

import java.net.*;
import java.util.*;

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
    private final static Logger logger = LoggerFactory
            .getLogger(EHMIProxyImpl.class);

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
     * The space manager ApAM component to handle the user space
     */
    private UserBaseSpec userManager;

    /**
     * The user manager ApAM component to handle the user base
     */
    private TraceManSpec traceManager;

    /**
     * Reference on the remote proxy service to execute command on
     * devices/services
     */
    private CHMIProxySpec coreProxy;

    /**
     * Reference to the EUDE interpreter to manage end user programs
     */
    private EUDE_InterpreterSpec interpreter;

    /**
     * Reference to the Dependency manager
     */
    private DependencyManagerSpec dependency;

    /**
     * The user manager ApAM component to handle the user base
     */
    private WeatherAdapterSpec weatherAdapter;

    /**
     * Service to be notified when clients send commands
     */
    private ListenerService addListenerService;

    /**
     * Service to communicate with clients
     */
    private SendWebsocketsService sendToClientService;

    private final String wsPort = "8087";

    private final BundleContext context;

    private SchedulerSpec schedulerService;

    /**
     * Listener for EHMI command from clients
     */
    private final EHMICommandListener commandListener;

    /**
     * Object update state event listener
     */
    private final CoreEventsListener objectEventsListener;

    /**
     * object discovery listener
     */
    private final CoreUpdatesListener objectUpdatesListener;

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
    private CoreClockSpec systemClock;
    /**
     * This is the system clock to deal with time in AppsGate EHMI
     */
    private CoreObjectSpec systemClockCoreObject;

    Object lock = new Object();

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
        logger.debug("UPnP Device instanciated");
        retrieveLocalAdress();
        logger.info("EHMI instanciated");
    }

    /**
     * Called by APAM when an instance of this implementation is created.
     */
    public void newInst() {
        logger.debug("EHMI is starting");

        if (httpService != null) {
            final HttpContext httpContext = httpService
                    .createDefaultHttpContext();
            final Dictionary<String, String> initParams = new Hashtable<String, String>();
            initParams.put("from", "HttpService");
            try {
                httpService.registerResources("/spok", "/WEB/spok",
                        httpContext);
                logger.debug("Registered URL : "
                        + httpContext.getResource("/WEB/spok"));
                logger.info("SPOK HTML pages registered.");
            } catch (NamespaceException ex) {
                logger.error("NameSpace exception");
            }
        }

        try {
            if (addListenerService.addCommandListener(commandListener, "EHMI")) {
                logger.info("EHMI command listener deployed.");
            } else {
                logger.error("EHMI command listener subscription failed.");
            }
        } catch (ExternalComDependencyException comException) {
            logger.debug("Resolution failed for listener service dependency, the EHMICommandListener will not be registered");
        }

    }

    boolean synchroContext = false;
    boolean synchroCoreProxy = false;

    private final long TIMEOUT = 1000 * 60;

    private synchronized boolean synchroCoreProxy() {
        logger.trace("synchroCoreProxy()...");

        if (synchroCoreProxy && synchroContext) {
            return true;
        }

        if (coreProxy != null) {
            logger.trace("... coreProxy is there");

            synchroCoreProxy = true;

            try {
                // before subscribing to new Core Device, synchronize the
                // existing ones
                JSONArray devicesArray = coreProxy.getDevicesDescription();
                for (int i = 0; i < devicesArray.length(); i++) {
                    try {
                        logger.trace("synchroCoreProxy(), synchro for : " + devicesArray.getJSONObject(i).toString());
                        if (devicesArray.getJSONObject(i).has("type")
                                && devicesArray.getJSONObject(i).has("id")) {
                            logger.trace("adding device : " + devicesArray.getJSONObject(i));
                            String type = devicesArray.getJSONObject(i).getString(
                                    "type");
                            String id = devicesArray.getJSONObject(i).getString(
                                    "id");

                            if (!"21".equals(type)) {
                                devicePropertiesTable.addGrammarForDevice(id, type, new GrammarDescription(coreProxy.getDeviceBehaviorFromType(type)));
                            }
                        }
                    } catch (JSONException e) {
                        logger.error(e.getMessage());
                    }

                }

                if (coreProxy.CoreEventsSubscribe(objectEventsListener)) {
                    logger.debug("Core event listener deployed.");
                } else {
                    logger.error("Core event deployement failed.");
                }
                if (coreProxy.CoreUpdatesSubscribe(objectUpdatesListener)) {
                    logger.debug("Core updates listener deployed.");
                } else {
                    logger.error("Core updates listener deployement failed.");
                }
                return true;
            } catch (CoreDependencyException coreException) {
                logger.warn("Resolution failed for core dependency, no notification subscription can be set.");
                return false;
            }

        } else {
            logger.trace("... coreProxy is not (yet) there");
            
            return false;
        }
    }

    private void traceManBound() {
        logger.trace("traceManBound()");
        ((ObjectEventListener) this.objectEventsListener).setTraceManager(traceManager);
        ((ObjectUpdateListener) this.objectUpdatesListener).setTraceManager(traceManager);
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        httpService.unregister("/spok");
        try {
            addListenerService.removeCommandListener("EHMI");
        } catch (ExternalComDependencyException comException) {
            logger.warn("Resolution failed for listener service dependency, the EHMICommandListener will not be unregistered");
        }

        try {
            coreProxy.CoreEventsUnsubscribe(objectEventsListener);
            coreProxy.CoreUpdatesUnsubscribe(objectUpdatesListener);

        } catch (CoreDependencyException coreException) {
            logger.warn("Resolution failed for core dependency, no notification subscription can be delete.");
        }

        logger.info("EHMI has been stopped.");
    }

    /**
     * @return
     */
    public DependencyManagerSpec getDependencyManager() {
        return this.dependency;
    }

    @Override
    public JSONArray getDevices() {
        waitForContext();

        JSONArray devices = new JSONArray();
        if (! synchroCoreProxy()){
            logger.debug("No core proxy found");
            return devices;
        }
            
        try {
            return addContextData(coreProxy.getDevicesDescription());
        } catch (CoreDependencyException coreException) {
            logger.debug("Resolution failed for core dependency, no device can be found.");

            try {
                devices.put(addContextData(systemClockCoreObject.getDescription(),
                        systemClockCoreObject.getAbstractObjectId()));
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }
        }
        return devices;
    }

    private void waitForContext() {
        logger.trace("waitForContext()");
        long timeStamp = System.currentTimeMillis();
        while (!synchroContext && ((System.currentTimeMillis() - timeStamp) < TIMEOUT)) {
            try {
                if (devicePropertiesTable == null || placeManager == null) {
                    Thread.sleep(500);
                } else {
                    synchroContext = true;
                }
            } catch (InterruptedException e) {
                logger.trace("waiting context DB Connexion");
            }
        }

        if (!synchroContext) {
            logger.trace("waitForContext(), context DB for name and places not found (research timeout)");
        }
    }

    @Override
    public JSONObject getDevice(String deviceId) {
        waitForContext();

        synchroCoreProxy();
        JSONObject devices = new JSONObject();
        try {
            JSONObject coreObject = coreProxy.getDeviceDescription(deviceId);
            return addContextData(coreObject, deviceId);
        } catch (CoreDependencyException coreException) {
            logger.debug("Resolution failed for core dependency, no device can be found.");

            if (deviceId.contentEquals(systemClockCoreObject.getAbstractObjectId())) {
                try {
                    devices = addContextData(systemClockCoreObject.getDescription(),
                            systemClockCoreObject.getAbstractObjectId());
                } catch (JSONException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return devices;
    }

    @Override
    public JSONArray getDevices(String type) {
        waitForContext();

        synchroCoreProxy();
        JSONArray devices = new JSONArray();
        try {
            return addContextData(coreProxy.getDevicesDescriptionFromType(type));
        } catch (CoreDependencyException coreException) {
            logger.debug("Resolution failed for core dependency, no device can be found.");

            try {
                if (type.contentEquals(systemClockCoreObject.getUserType())) {
                    devices.put(addContextData(systemClockCoreObject.getDescription(),
                            systemClockCoreObject.getAbstractObjectId()));
                }
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }
        }
        return devices;
    }

    @Override
    public StateDescription getEventsFromState(String objectId, String stateName) {
        if (devicePropertiesTable == null) {
            logger.error("no context data available");
            return null;
        }
        CoreObjectSpec device = coreProxy.getCoreDevice(objectId);
        try {
            GrammarDescription grammar = getGrammarFromType(device.getUserType());
            if (grammar == null) {
                logger.error("Unable to get grammar for device: {}", objectId);
                return null;
            }
            return new StateDescription(grammar.getStateDescription(stateName));
        } catch (JSONException ex) {
            logger.error("Grammar not well formatted for: {}", objectId);
            return null;
        }

    }


    @Override
    public GrammarDescription getGrammarFromType(String deviceType) {
        if (devicePropertiesTable == null) {
            logger.error("getGrammarFromType({}): No context data available", deviceType);
            return null;
        }
        GrammarDescription desc = devicePropertiesTable.getGrammarFromType(deviceType);
        if (desc == null) {
            logger.warn("getGrammarFromType({}): the devicePropertyTable did not contain", deviceType);
            desc = new GrammarDescription(coreProxy.getDeviceBehaviorFromType(deviceType));
            devicePropertiesTable.addGrammarForDevice(null, deviceType, new GrammarDescription(coreProxy.getDeviceBehaviorFromType(deviceType)));
        } else {
        }
        return desc;
    }

    @Override
    public JSONArray getPlaces() {
        if (placeManager == null) {
            logger.error("no context data available");
            return null;
        }
        return placeManager.getJSONPlaces();
    }

    @Override
    public void moveDevice(String objId, String srcPlaceId, String destPlaceId) {
        if (placeManager == null) {
            logger.error("no context data available");
            return;
        }
        if (dependency != null) {
            dependency.buildGraph();
        }

        placeManager.moveObject(objId, srcPlaceId, destPlaceId);
    }
    

    @Override
    public String getCoreObjectPlaceId(String objId) {
        if (placeManager == null) {
            logger.error("no context data available");
            return null;
        }
        return placeManager.getCoreObjectPlaceId(objId);
    }

    @Override
    public ArrayList<String> getDevicesInSpaces(ArrayList<String> typeList,
            ArrayList<String> spaces) {
        if (placeManager == null) {
            logger.error("no context data available");
            return null;
        }

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
            //for (SymbolicPlace symbolicPlace : placeManager.getPlaces()) {
            //	coreObjectInPlace.addAll(symbolicPlace.getDevices());
            //}
            JSONArray devices = coreProxy.getDevicesId();
            for (int i = 0; i < devices.length(); i++) {
                coreObjectInPlace.add(devices.optString(i));
            }
        }

        // Now we get all identifier of device that match one types of the type
        // list
        if (!typeList.isEmpty()) {
            for (String type : typeList) {
                JSONArray devicesOfType = coreProxy.getDevicesIdFromType(type);
                int size = devicesOfType.length();
                for (int i = 0; i < size; i++) {
                    coreObjectOfType.add(devicesOfType.optString(i));
                }
            }
        } else {
            JSONArray allDevices = coreProxy.getDevicesId();
            int size = allDevices.length();
            for (int i = 0; i < size; i++) {
                coreObjectOfType.add(allDevices.optString(i));
            }
        }

        // We get the intersection between placed object and object of
        // specified type
        coreObjectInPlace.retainAll(coreObjectOfType);

        return coreObjectInPlace;
    }


    @Override
    public JSONArray addLocationObserver(String location) {
        if (weatherAdapter == null) {
            logger.error("no weather service available");
            return null;
        }
        weatherAdapter.addLocationObserver(location);
        return getAllLocationsObservers();
    }

    @Override
    public JSONArray removeLocationObserver(String location) {
        if (weatherAdapter == null) {
            logger.error("no weather service available");
            return null;
        }
        weatherAdapter.removeLocationObserver(location);
        return getAllLocationsObservers();
    }

    @Override
    public JSONArray getActiveLocationsObservers() {
        if (weatherAdapter == null) {
            logger.error("no weather service available");
            return null;
        }
        return new JSONArray(weatherAdapter.getActiveLocationsObservers());
    }

    @Override
    public JSONArray getAllLocationsObservers() {
        if (weatherAdapter == null) {
            logger.error("no weather service available");
            return null;
        }
        return new JSONArray(weatherAdapter.getAllLocationsObservers());
    }

    @Override
    public JSONObject checkLocation(String location) {
        if (weatherAdapter == null) {
            logger.error("no weather service available");
            return null;
        }
        if (weatherAdapter != null) {
            return weatherAdapter.checkLocation(location);
        }
        return new JSONObject();
    }

    @Override
    public JSONArray checkLocationsStartingWith(String firstLetters) {
        if (weatherAdapter == null) {
            logger.error("no weather service available");
            return null;
        }
        if (weatherAdapter != null) {
            return weatherAdapter.checkLocationsStartingWith(firstLetters);
        }
        return new JSONArray();
    }

    @Override
    public JSONArray addLocationObserverFromWOEID(String woeid) {
        if (weatherAdapter == null) {
            logger.error("no weather service available");
            return null;
        }
        weatherAdapter.addLocationObserverFromWOEID(woeid);
        return getAllLocationsObservers();
    }



    @Override
    public boolean removeProperty(String placeId, String key) {
        if (placeManager == null) {
            logger.error("no context data available");
            return false;
        }

        interpreter.checkReferences();
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
    public JSONObject getGraph(Boolean buildGraph) {
        if (buildGraph) {
            dependency.buildGraph();
        }
        return dependency.getGraph().getJSONDescription();
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
     *
     * @param object the object to enrich
     * @param objectId the identifier of this object
     * @return the new contextual enrich JSONObject
     */
    public JSONObject addContextData(JSONObject object, String objectId) {
        logger.trace("addContextData(JSONObject object : {}, String objectId : {})", object, objectId);
        try {
            object.put("placeId", getCoreObjectPlaceId(objectId));
            object.put("name", devicePropertiesTable.getName(objectId, ""));
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
        return object;
    }

    /**
     * Add contextual data to all object in an JSONArray
     *
     * @param objects the objects JSONArray
     * @return a enrich from contextual data JSONArray
     */
    private JSONArray addContextData(JSONArray objects) {
        logger.trace("addContextData(JSONArray objects :" + objects.toString());
        JSONArray contextArray = new JSONArray();
        try {
            int nbObjects = objects.length();
            int i = 0;
            JSONObject coreObject;
            while (i < nbObjects) {
                coreObject = objects.getJSONObject(i);
                logger.trace("Trying to add : " + coreObject + "...");
                if (coreObject != null && coreObject.has("id")) {
                    contextArray.put(addContextData(coreObject,
                            coreObject.getString("id")));
                    logger.trace("... successfully added. With context : " + contextArray.getJSONObject(i));

                }
                i++;
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
        return objects;
    }

    /**
     * Find the IP address from system configuration to exposed it through UPnP.
     */
    private void retrieveLocalAdress() {
        // initiate UPnP state variables
        try {
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
                    if (!netint.getDisplayName().contentEquals("tun0")) {
                        logger.debug(
                                "The newtwork interface {} will be inspected.",
                                netint.getDisplayName());
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
            }

        } catch (UnknownHostException e) {
            logger.debug("Unknown host: {}", e.getMessage());
        } catch (SocketException e) {
            logger.debug("Socket exception for UPnP: {}", e.getMessage());
        }
    }

    /**
     * Send notification to all connected clients.
     *
     * @param notif the notification to transmit
     */
    public void sendToClients(JSONObject notif) {
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
        try {
            sendToClientService.send(notif.JSONize().toString());
        } catch (ExternalComDependencyException comException) {
            logger.debug("Resolution failed for send to client service dependency, no message will be sent.");
        }
    }

    /**
     * Get a command description, resolve the local target reference and return
     * a runnable command object
     *
     * @param clientId client identifier
     * @param method method name to call on objectId
     * @param arguments arguments list form method methodName
     * @param types arguments types list
     * @param callId the remote call identifier
     * @return runnable object that can be execute and manage.
     */
    @SuppressWarnings("rawtypes")
    public Runnable executeCommand(int clientId, String method,
            ArrayList<Object> arguments, ArrayList<Class> types, String callId) {
        return new EHMICommand(this, method, arguments, types, callId,
                clientId, sendToClientService);
    }

    /**
     * Get a runnable object that can execute command from a remote device
     * manager asynchronously with a return response
     *
     * @param objIdentifier the identifier of the object on the remote system
     * @param method the method name to call
     * @param arguments the arguments values corresponding to the method to
     * invoke
     * @param types the arguments JAVA types
     * @param clientId the client connection identifier
     * @param callId the remote call identifier
     * @return a runnable object that can be execute and manage.
     */
    @SuppressWarnings("rawtypes")
    public appsgate.lig.chmi.spec.GenericCommand executeRemoteCommand(
            String objIdentifier, String method, ArrayList<Object> arguments,
            ArrayList<Class> types, int clientId, String callId) {
        if (traceManager != null) {
            traceManager.commandHasBeenPassed(objIdentifier, method, "user", arguments, getCurrentTimeInMillis());
        }
        return coreProxy.executeCommand(clientId, objIdentifier, method,
                arguments, types, callId);
    }

    /**
     * Get a runnable object that can execute command from a remote device
     * manager asynchronously
     *
     * @param objIdentifier the identifier of the object on the remote system
     * @param method the method name to call
     * @param args the arguments list with their types
     * @return a runnable object that can be execute and manage.
     */
    @Override
    public appsgate.lig.chmi.spec.GenericCommand executeRemoteCommand(
            String objIdentifier, String method, JSONArray args) {
        // traceManager.commandHasBeenPassed(objIdentifier, method, "PROGRAM");
        return coreProxy.executeCommand(objIdentifier, method, args);
    }

    @Override
    public synchronized void addCoreListener(CoreListener coreListener) {
        logger.debug("Adding a core listener...");
        Entry eventKey = new Entry(coreListener);

        // Check if the need to by register in the core clock implementation
        if (systemClockCoreObject.getAbstractObjectId().contentEquals(
                coreListener.getObjectId())
                && eventKey.getVarName().contentEquals("ClockAlarm")
                && !eventKey.isEventOnly()) {
            logger.debug("Adding an alarm listener...");
            // Generate calendar java object for core clock
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(coreListener.getValue()));
            // register the alarm
            int alarmId = systemClock.registerAlarm(calendar, new TimeObserver("EHMI listener for clock event"));
            // change the event entry with the alarmId value
            //eventKey.setValue(String.valueOf(alarmId));
            eventKey = new TimeEntry(eventKey, alarmId);
            // save the alarm identifier
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
                ArrayList<CoreListener> coreListenerList = eventsListeners
                        .get(key);
                coreListenerList.remove(coreListener);
                if (coreListenerList.isEmpty()) {
                    eventsListeners.remove(key);
                }
                Integer alarmId = alarmListenerList.get(key);
                if (alarmId != null) {
                    logger.debug("Deleting an alarm listener with id: "
                            + alarmId);
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
     *
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
     *
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
     *
     * @param millis the new time to set
     */
    public void setCurrentTimeInMillis(long millis) {
        systemClock.setCurrentTimeInMillis(millis);
    }

    /**
     * Get the current time flow rate from the local EHMI clock
     *
     * @return the current time flow rate as a double
     */
    public double getTimeFlowRate() {
        return systemClock.getTimeFlowRate();
    }

    /**
     * Set the time flow rate
     *
     * @return the new time flow rate
     */
    public double setTimeFlowRate(double rate) {
        return systemClock.setTimeFlowRate(rate);
    }

    /**
     * Send a clock alarm notification to connected client
     *
     * @param msg
     */
    public void sendClockAlarmNotifcation(ClockAlarmNotificationMsg msg) {
        sendToClients(msg.JSONize());
    }

    public void getLog(long timeStart, long timeEnd) {

    }

    public void newDeviceStatus(String objectId, Boolean bool) {
        if (interpreter != null) {
            interpreter.newDeviceStatus(objectId, bool);
        }
        if (dependency != null) {
            dependency.buildGraph();
        }
    }

    @Override
    public GrammarDescription getGrammarFromDevice(String deviceId) {
        if (devicePropertiesTable == null) {
            logger.error("no context data available");
            return null;
        }

        GrammarDescription grammar = devicePropertiesTable
                .getGrammarFromDevice(deviceId);
        if (grammar != null) {
            return grammar;
        }
        // Add the grammar to the table
        CoreObjectSpec device = coreProxy.getCoreDevice(deviceId);

        if (device != null) {
            String type = device.getUserType();
            devicePropertiesTable.setType(deviceId, type);
            grammar = getGrammarFromType(type);
            devicePropertiesTable.addGrammarForDevice(deviceId, type, grammar);
            return grammar;

        } else {
            logger.error("Device {} is not available, this is not normal, ask the matrix architect why", deviceId);
        }
        return null;
    }

    @Override
    public boolean addClientConnexion(CommandListener cmdListener, String name,
            int port) {
        return addListenerService
                .createDedicatedServer(cmdListener, name, port);
    }

    @Override
    public boolean removeClientConnexion(String name) {
        return addListenerService.removeDedicatedServer(name);
    }

    @Override
    public void sendFromConnection(String name, String msg) {
        sendToClientService.sendTo(name, msg);
    }

    @Override
    public void sendFromConnection(String name, int clientId, String msg) {
        sendToClientService.sendTo(name, clientId, msg);
    }

    @Override
    public int startDebugger() {
        if (traceManager != null) {
            return traceManager.startDebugger();
        } else {
            return 0;
        }
    }

    @Override
    public boolean stopDebugger() {
        if (traceManager != null) {
            return traceManager.stopDebugger();
        } else {
            return false;
        }
    }

    @Override
    public JSONObject getTraceManStatus() {
        return traceManager.getStatus();
    }

    @Override
    public void scheduleProgram(String eventName, String programId,
            boolean startOnBegin, boolean stopOnEnd) {
        if (schedulerService == null) {
            logger.error("No scheduling service, aborting)");
        } else {
            try {
                String eventId = schedulerService.createEvent(eventName,
                        programId, startOnBegin, stopOnEnd);
                if (eventId != null) {
                    logger.trace(" Program successfully scheduled with event ID : "
                            + eventId);
                } else {
                    logger.error("Program not scheduled");
                }
            } catch (SchedulingException exc) {
                logger.error("Error when adding an event to the scheduler : "
                        + exc.getMessage());
            }
        }
    }

    @Override
    public Set<?> listEventsSchedulingProgramId(String programId, String startPeriod, String endPeriod) {
        logger.trace("listEventsSchedulingProgramId("
                + "String programId : {},"
                + "String startPeriod : {}"
                + ", String endPeriod) : {}", programId, startPeriod, endPeriod);
        if (schedulerService == null) {
            logger.error("No scheduling service, aborting)");
        } else {
            try {
                return schedulerService.listEventsSchedulingProgramId(programId, startPeriod, endPeriod);
            } catch (SchedulingException exc) {
                logger.error("Error when when checking the scheduler : "
                        + exc.getMessage());
            }
        }
        return null;
    }

    @Override
    public boolean checkProgramIdScheduled(String programId) {
        if (schedulerService == null) {
            logger.error("No scheduling service, aborting)");
        } else {
            try {
                return schedulerService.checkProgramIdScheduled(programId);
            } catch (SchedulingException exc) {
                logger.error("Error when checking the scheduler : "
                        + exc.getMessage());
            }
        }
        return false;
    }

    @Override
    public JSONArray checkProgramsScheduled() {
        JSONArray programsScheduled = new JSONArray();
        if (schedulerService == null) {
            logger.error("No scheduling service, aborting)");
        } else {
            try {
                programsScheduled = schedulerService.checkProgramsScheduled();
            } catch (SchedulingException exc) {
                logger.error("Error when checking the scheduler : "
                        + exc.getMessage());
            }
        }
        return programsScheduled;
    }

    @Override
    public SpokObject getProgramDependencies(String programid) {
        return dependency.getDependencies(programid);
    }

    @Override
    public SpokObject getProgram(String programid) {
        return interpreter.getNodeProgram(programid);
    }
    
    public DevicePropertiesTableSpec getDevicePropertiesTable() {
		return devicePropertiesTable;
	}

	public PlaceManagerSpec getPlaceManager() {
		return placeManager;
	}
}
