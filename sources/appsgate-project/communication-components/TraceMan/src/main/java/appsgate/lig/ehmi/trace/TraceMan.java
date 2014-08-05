package appsgate.lig.ehmi.trace;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.device.properties.table.spec.DevicePropertiesTableSpec;
import appsgate.lig.ehmi.spec.GrammarDescription;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.ehmi.spec.trace.TraceManSpec;
import appsgate.lig.eude.interpreter.spec.ProgramLineNotification;
import appsgate.lig.eude.interpreter.spec.ProgramNotification;
import appsgate.lig.manager.place.spec.PlaceManagerSpec;
import appsgate.lig.manager.place.spec.SymbolicPlace;
import appsgate.lig.persistence.MongoDBConfiguration;
import java.util.ArrayList;

/**
 * This component get CHMI from the EHMI proxy and got notifications for each
 * event in the EHMI layer to merge them into a JSON stream.
 *
 * @author Cedric Gerard
 * @since July 13, 2014
 * @version 1.0.0
 */
public class TraceMan implements TraceManSpec {

    /**
     * The logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(TraceMan.class);

    /**
     * Dependence to the main EHMI component
     */
    private EHMIProxySpec EHMIProxy;

    /**
     * Dependence to the device property table
     */
    private DevicePropertiesTableSpec devicePropTable;

    /**
     * Dependencies to the place manager
     */
    private PlaceManagerSpec placeManager;

    /*
     * The collection containing the links (wires) created, and deleted
     */
    private MongoDBConfiguration myConfiguration;

    /**
     * Map for device id to their user friendly name
     */
    private final HashMap<String, GrammarDescription> deviceGrammar = new HashMap<String, GrammarDescription>();

    /**
     *
     */
    private TraceHistory fileTracer;
    /**
     *
     */
    private TraceHistory dbTracer;

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        fileTracer = new TraceFile();
        if (!fileTracer.init()) {
            LOGGER.warn("Unable to start the tracer");
        }
        dbTracer = new TraceMongo(myConfiguration);
        if (!dbTracer.init()) {
            LOGGER.warn("Unable to start the tracer");
        }
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        fileTracer.close();
        dbTracer.close();
    }

    private void trace(JSONObject o) {
        dbTracer.trace(o);
        fileTracer.trace(o);
        //liveTracer.trace(o);
    }

    @Override
    public synchronized void commandHasBeenPassed(String objectID, String command, String caller) {
        if (deviceGrammar.get(objectID) != null) { //if the equipment has been instantiated from ApAM spec before
            //Create the event description device entry
            JSONObject event = new JSONObject();
            JSONObject deviceJson = getJSONDevice(objectID, event, Trace.getJSONDecoration("write", "user", null, objectID, "User trigger this action using HMI"));
            //Create the notification JSON object
            JSONObject coreNotif = getCoreNotif(deviceJson, null);
            //Trace the notification JSON object in the trace file
            trace(coreNotif);
        }
    }

    @Override
    public synchronized void coreEventNotify(long timeStamp, String srcId, String varName, String value) {
        if (deviceGrammar.get(srcId) != null) { //if the equipment has been instantiated from ApAM spec before
            //Create the event description device entry
            JSONObject event = new JSONObject();
            try {
                event.put("type", "update");
                event.put("state", getDeviceState(srcId, varName, value));
            } catch (JSONException e) {
            }
            JSONObject deviceJson = getJSONDevice(srcId, event, null);
            //Create the notification JSON object
            JSONObject coreNotif = getCoreNotif(deviceJson, null);
            //Trace the notification JSON object in the trace file
            trace(coreNotif);
        }
    }

    private JSONObject getCoreNotif(JSONObject device, JSONObject program) {
        JSONObject coreNotif = new JSONObject();
        try {
            coreNotif.put("timestamp", EHMIProxy.getCurrentTimeInMillis());
            //Create the device tab JSON entry
            JSONArray deviceTab = new JSONArray();
            {
                if (device != null) {
                    deviceTab.put(device);
                }
                coreNotif.put("devices", deviceTab);
            }
            //Create the device tab JSON entry
            JSONArray pgmTab = new JSONArray();
            {
                if (program != null) {
                    pgmTab.put(program);
                }
                coreNotif.put("programs", pgmTab);
            }
        } catch (JSONException e) {

        }
        return coreNotif;
    }

    private JSONObject getJSONDevice(String srcId, JSONObject event, JSONObject cause) {
        JSONObject objectNotif = new JSONObject();
        try {
            objectNotif.put("id", srcId);
            objectNotif.put("name", devicePropTable.getName(srcId, ""));
            objectNotif.put("type", deviceGrammar.get(srcId).getType());
            JSONObject location = new JSONObject();
            location.put("id", placeManager.getCoreObjectPlaceId(srcId));
            SymbolicPlace place = placeManager.getPlaceWithDevice(srcId);
            if (place != null) {
                location.put("name", place.getName());
            }
            objectNotif.put("location", location);
            objectNotif.put("decoration", cause);
            objectNotif.put("event", event);

        } catch (JSONException e) {

        }
        return objectNotif;

    }

    @Override
    public synchronized void coreUpdateNotify(long timeStamp, String srcId, String coreType,
            String userType, String name, JSONObject description, String eventType) {

        //Put the user friendly core type in the map
        GrammarDescription grammar = EHMIProxy.getGrammarFromType(userType);

        deviceGrammar.put(srcId, grammar);

        JSONObject event = new JSONObject();
        JSONObject cause = new JSONObject();
        try {
            if (eventType.contentEquals("new")) {
                event.put("type", "appear");
                cause = Trace.getJSONDecoration("", "technical", null, null, "Equipment appear");
                event.put("state", getDeviceState(srcId, "", ""));

            } else if (eventType.contentEquals("remove")) {
                event.put("type", "disappear");
                cause = Trace.getJSONDecoration("", "technical", null, null, "Equipment disappear");
            }

        } catch (JSONException e) {

        }

        JSONObject jsonDevice = getJSONDevice(srcId, event, cause);
        JSONObject coreNotif = getCoreNotif(jsonDevice, null);
        //Trace the notification JSON object in the trace file
        trace(coreNotif);

    }

    /**
     *
     * @param n
     */
    public synchronized void gotNotification(NotificationMsg n) {
        if (!(n instanceof ProgramNotification)) {
            return;
        }
        if (n instanceof ProgramLineNotification) {
            JSONObject o = getDecorationNotification((ProgramLineNotification) n);
            trace(o);
            return;
        }
        ProgramNotification notif = (ProgramNotification) n;
        //Create the notification JSON object
        //Create a device trace entry
        //Trace the notification JSON object in the trace file
        JSONObject jsonProgram = getJSONProgram(notif.getProgramId(), notif.getProgramName(), notif.getVarName(), notif.getRunningState(), null);

        trace(getCoreNotif(null, jsonProgram));
    }

    @Override
    public JSONArray getTraces(Long timestamp, Integer number) {
        return dbTracer.get(timestamp, number);
    }

    @Override
    public JSONArray getTracesBetweenInterval(Long start, Long end) {
        return dbTracer.getInterval(start, end);
    }

    private JSONObject getJSONProgram(String id, String name, String change, String state, String iid) {
        JSONObject progNotif = new JSONObject();
        try {
            progNotif.put("id", id);
            progNotif.put("name", name);

            //Create the event description device entry
            JSONObject event = new JSONObject();
            JSONObject cause = new JSONObject();
            {
                JSONObject s = new JSONObject();
                s.put("name", state);
                s.put("instruction_id", iid);
                event.put("state", s);
                if (change != null) {
                    if (change.contentEquals("newProgram")) {
                        event.put("type", "appear");
                        cause = Trace.getJSONDecoration("", "user", null, null, "Program has been added");
                    } else if (change.contentEquals("removeProgram")) {
                        event.put("type", "disappaear");
                        cause = Trace.getJSONDecoration("", "user", null, null, "Program has been deleted");

                    } else { //change == "updateProgram"
                        event.put("type", "update");
                    }
                    //Create causality event entry
                    if (change.contentEquals("updateProgram")) {
                        cause = Trace.getJSONDecoration("", "user", null, null, "Program has been updated");
                    }
                }

            }
            progNotif.put("event", event);
            progNotif.put("decoration", cause);
        } catch (JSONException e) {

        }
        return progNotif;
    }

    private JSONObject getDeviceState(String srcId, String varName, String value) {
        JSONObject deviceState = new JSONObject();
        GrammarDescription g = deviceGrammar.get(srcId);
        // If the state of a device is complex

        JSONObject deviceProxyState = EHMIProxy.getDevice(srcId);
        ArrayList<String> props = g.getProperties();
        for (String k : props) {
            if (k != null && !k.isEmpty()) {
                try {
                    deviceState.put(g.getValueVarName(k), deviceProxyState.get(k));
                } catch (JSONException ex) {
                    LOGGER.error("Unable to retrieve key[{}] from {} for {}", k, srcId, g.getType());
                    LOGGER.error("DeviceState: " + deviceProxyState.toString());
                }
            }
        }
        try {
            deviceState.put("status", "2");
        //    deviceState.put(varName, value); // Not sure this is really necessary
        } catch (JSONException ex) {
        }
        return deviceState;
    }

    private JSONObject getDecorationNotification(ProgramLineNotification n) {
        JSONObject p = getJSONProgram(n.getProgramId(), n.getProgramName(), null, n.getRunningState(), n.getInstructionId());
        JSONObject d = getJSONDevice(n.getTargetId(), null, Trace.getJSONDecoration(n.getType(), "Program", n.getSourceId(), null, n.getDescription()));
        try {
            p.put("decoration", Trace.getJSONDecoration(n.getType(), "Program", null, n.getTargetId(), n.getDescription()));
        } catch (JSONException ex) {
        }
        return getCoreNotif(d, p);
    }

}
