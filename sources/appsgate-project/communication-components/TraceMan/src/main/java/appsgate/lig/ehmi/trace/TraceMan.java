package appsgate.lig.ehmi.trace;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.device.properties.table.spec.DevicePropertiesTableSpec;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.GrammarDescription;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.ehmi.spec.trace.TraceManSpec;
import appsgate.lig.ehmi.trace.listener.TraceCmdListener;
import appsgate.lig.ehmi.trace.queue.TraceQueue;
import appsgate.lig.eude.interpreter.spec.ProgramCommandNotification;
import appsgate.lig.eude.interpreter.spec.ProgramNotification;
import appsgate.lig.manager.place.spec.PlaceManagerSpec;
import appsgate.lig.manager.place.spec.SymbolicPlace;
import appsgate.lig.persistence.MongoDBConfiguration;

/**
 * This component get CHMI from the EHMI proxy and got notifications for each
 * event in the EHMI layer to merge them into a JSON stream.
 *
 * @author Cedric Gerard
 * @since July 13, 2014
 * @version 1.1.0
 *
 * Compliant with the version 4 of trace specification
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

    /**
     * The collection containing the links (wires) created, and deleted
     */
    private MongoDBConfiguration myConfiguration;

    /**
     * Default tracer use to have complete trace history Only simple trace (no
     * aggregation) are log in.
     */
    private TraceHistory dbTracer;

    /**
     * Boolean for file tracer activation
     */
    private boolean fileTraceActivated = false;

    /**
     * Trace log in file use to inspect trace
     */
    private TraceHistory fileTracer;

    /**
     * Boolean for live tracer activation
     */
    private boolean liveTraceActivated = false;

    /**
     * Trace log in real time
     */
    private TraceRT liveTracer;

    /**
     * The buffer queue for AppsGate simple traces
     */
    private TraceQueue traceQueue;

    /**
     * Last trace time stamp use to avoid collisions
     */
    private long lastTimeStamp;

    /**
     * Debugger connection name
     */
    private final String DEBUGGER_COX_NAME = "debugger";

    /**
     * Debugger default port connection
     */
    private final int DEBUGGER_DEFAULT_PORT = 8090;

    /**
     * The grouping policy
     */
    private String grouping = "type";

    /**
     * The focus identifier
     */
    private String focus = NOFOCUS;

    /**
     * The focus type
     */
    private String focusType;

    /**
     * time line delta value for aggregation
     */
    private long timeLineDelta;

    /**
     * TraceMan socket state
     */
    private boolean state;

    /**
     * No filtering for traces (i.e. all trace are returned)
     */
    public static final String NOFOCUS = "NONE";

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {

        dbTracer = new TraceMongo(myConfiguration);
        if (!dbTracer.init()) {
            LOGGER.warn("Unable to start the tracer");
        }

        //TraceQueue initialization with no aggregation
        traceQueue = new TraceQueue(this, 0);
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {

        if (traceQueue != null) {
            traceQueue.stop();
        }
        if (fileTracer != null) {
            fileTracer.close();
        }

        EHMIProxy.removeClientConnexion(DEBUGGER_COX_NAME);
        dbTracer.close();
    }

    /**
     * Request the trace man instance to trace event. Add the time stamp to the
     * trace and put it in the queue
     *
     * @param o the event to trace
     */
    private void trace(JSONObject o) {
        synchronized (traceQueue) {
            try {
                o.put("timestamp", getCurrentTimeInMillis());

                //Delayed in queue to by aggregate by policy if real time tracing is actived
                if (liveTraceActivated || fileTraceActivated) {
                    traceQueue.offer(o);
                }
                //Simple trace always save in data base
                dbTracer.trace(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send the trace into destinations Destination can not be dbTracer
     *
     * @param trace the event to write exception trace time stamp must be
     * greater than one deltaTinMilis + previous written time stamp value
     */
    private synchronized void sendTrace(JSONObject trace) {
        try {
            long timeStamp = trace.getLong("timestamp");

            if (timeStamp > lastTimeStamp) {
                lastTimeStamp = timeStamp;

                if (liveTraceActivated) {
                    liveTracer.trace(trace); //Send trace packet to client side
                }

                if (fileTraceActivated) {
                    fileTracer.trace(trace); //Save into local file
                }

            } else {
                LOGGER.error("Multiple trace request with the same time stamp value: " + timeStamp + ". Entry are skipped.");
                throw new Error("Multiple trace request with the same time stamp value. Entry with time stamp " + timeStamp + " are skipped.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send an array of traces into the destinations using sendTrace method
     *
     * @param traceArray the event array to write
     */
    public synchronized void sendTraces(JSONArray traceArray) {
        int nbTraces = traceArray.length();

        for (int i = 0; i < nbTraces; i++) {
            try {
                sendTrace(traceArray.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void commandHasBeenPassed(String objectID, String command, String caller, JSONObject context) {
        //if the equipment has been instantiated from ApAM spec before
        GrammarDescription grammar = EHMIProxy.getGrammarFromDevice(objectID);
        if (grammar != null) {

            JSONObject deviceJson = getJSONDevice(objectID, null,
                    Trace.getJSONDecoration("write", caller, null, objectID,
                            grammar.getTraceMessageFromCommand(command), context));
            //Create the notification JSON object
            JSONObject coreNotif = getCoreNotif(deviceJson, null);
            //Trace the notification JSON object in the trace file
            trace(coreNotif);
        }
    }

    @Override
    public synchronized void coreEventNotify(long timeStamp, String srcId, String varName, String value) {

        GrammarDescription desc = EHMIProxy.getGrammarFromDevice(srcId);
        if (desc != null && applyFilters(desc, srcId, varName, value) && desc.generateTrace()) {
            //Create the event description device entry
            JSONObject event = new JSONObject();
            JSONObject JDecoration = null;
            try {

                if (varName.equalsIgnoreCase("status")) {
                    if (value.equalsIgnoreCase("2")) {
                        event.put("type", "connection");
                        event.put("picto", Trace.getConnectionPicto());
                        JDecoration = Trace.getJSONDecoration(
                                "connection", "technical", srcId, null, "decorations.connection", null);
                    } else if (value.equalsIgnoreCase("0")) {
                        event.put("type", "disconnection");
                        event.put("picto", Trace.getDisconnectionPicto());
                        JDecoration = Trace.getJSONDecoration(
                                "disconnection", "technical", srcId, null, "decorations.disconnection", null);
                    } else {
                        event.put("type", "update");
                        JDecoration = Trace.getJSONDecoration(
                                "error", "technical", srcId, null, "decorations.error", null);
                    }
                } else {
                    event.put("type", "update");
                    JSONObject context = Trace.addJSONPair(new JSONObject(), "text", value);
                    Trace.addJSONPair(context, "var", varName);
                    JDecoration = Trace.getJSONDecoration(
                            "update", "technical", srcId, null, "decorations.change" + varName, context);
                }

                JSONObject jsonState = getDeviceState(srcId, varName, value);

                if (event.getString("type").equalsIgnoreCase("update")) {
                    event.put("picto", Trace.getPictoState(desc.getType(), varName, value, jsonState));
                }

                event.put("state", jsonState);

            } catch (JSONException e) {
            }

            JSONObject deviceJson = getJSONDevice(srcId, event, JDecoration);
            //Create the notification JSON object
            JSONObject coreNotif = getCoreNotif(deviceJson, null);
            //Trace the notification JSON object in the trace file
            trace(coreNotif);
        }
    }

    private JSONObject getCoreNotif(JSONObject device, JSONObject program) {
        JSONObject coreNotif = new JSONObject();
        try {
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

    /**
     * Method to build a trace for an event on a device
     *
     * @param srcId
     * @param event
     * @param cause
     * @return
     */
    private JSONObject getJSONDevice(String srcId, JSONObject event, JSONObject cause) {
        JSONObject objectNotif = new JSONObject();
        try {
            objectNotif.put("id", srcId);
            objectNotif.put("name", devicePropTable.getName(srcId, ""));
            GrammarDescription g = EHMIProxy.getGrammarFromDevice(srcId);
            if (g != null) {
                objectNotif.put("type", g.getType());
            } else {
                LOGGER.error("Unable to build a trace on an unknown type for {}", srcId);
                LOGGER.debug("No trace have been produced for {} with cause: {}", event, cause);
                return null;
            }
            JSONObject location = new JSONObject();
            location.put("id", placeManager.getCoreObjectPlaceId(srcId));
            SymbolicPlace place = placeManager.getPlaceWithDevice(srcId);
            if (place != null) {
                location.put("name", place.getName());
            } else {
                LOGGER.warn("Place not found for this device {}", srcId);
            }

            objectNotif.put("location", location);
            objectNotif.put("decorations", new JSONArray().put(cause));

            if (event != null) {
                objectNotif.put("event", event);
            }

        } catch (JSONException e) {

        }
        return objectNotif;

    }

    @Override
    public synchronized void coreUpdateNotify(long timeStamp, String srcId, String coreType,
            String userType, String name, JSONObject description, String eventType) {

        JSONObject event = new JSONObject();
        JSONObject cause = new JSONObject();
        try {
            if (eventType.contentEquals("new")) {
                event.put("type", "appear");
                cause = Trace.getJSONDecoration(
                        "appear", "technical", srcId, null, "decorations.appear",
                        Trace.addJSONPair(new JSONObject(), "name", name));
                event.put("state", getDeviceState(srcId, "", ""));

            } else if (eventType.contentEquals("remove")) {
                event.put("type", "disappear");
                cause = Trace.getJSONDecoration(
                        "disappear", "technical", srcId, null, "decorations.remove",
                        Trace.addJSONPair(new JSONObject(), "name", name));
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
        if (n instanceof ProgramCommandNotification) {
            JSONObject o = getDecorationNotification((ProgramCommandNotification) n);
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
        JSONArray tracesTab = dbTracer.get(timestamp, number);
        if (traceQueue.getDeltaTinMillis() == 0) { //No aggregation
            return tracesTab;
        } else { // Apply aggregation policy
            try {
                //filteringOnFocus(tracesTab);
                traceQueue.stop();
                traceQueue.loadTraces(tracesTab);
                return traceQueue.applyAggregationPolicy(timestamp, null); //Call with default aggregation policy (id and time)
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return tracesTab;
    }

    @Override
    public void getTracesBetweenInterval(Long from, Long to, boolean withEventLine, JSONObject request) {
        JSONObject requestResult = new JSONObject();
        JSONArray tracesTab = dbTracer.getInterval(from, to);
        JSONObject result = new JSONObject();
        try {

            result.put("groups", computeGroupsFromPolicy(tracesTab)); //First whole traces tab browse
            if (withEventLine) {
                result.put("eventline", eventLineComputation(tracesTab, from, to));//Second whole traces tab browse
            }

            if (traceQueue.getDeltaTinMillis() == 0) { //No aggregation
                result.put("data", tracesTab);
            } else { // Apply aggregation policy
                //filteringOnFocus(tracesTab);
                traceQueue.stop();
                traceQueue.loadTraces(tracesTab);//Third whole traces tab browse
                result.put("data", traceQueue.applyAggregationPolicy(from, null) /*Call with default aggregation policy (id and time)*/); //Fourth whole traces tab browse + in detail browsing
            }

            requestResult.put("result", result);
            requestResult.put("request", request);

            EHMIProxy.sendFromConnection(DEBUGGER_COX_NAME, request.getInt("clientId"), requestResult.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compute groups to display By default the type is to make group. If a
     * focus is define, the gourping policy can be type of dep
     *
     * @param tracesTab the trace tab use to compute group from
     * @return a JSONArray containing each group
     * @throws JSONException
     */
    private JSONArray computeGroupsFromPolicy(JSONArray tracesTab) throws JSONException {

        JSONArray groups = new JSONArray();
        HashMap<String, GroupTuple> groupFollower = new HashMap<String, GroupTuple>();
        int l = tracesTab.length();

        if (focus.equalsIgnoreCase(TraceMan.NOFOCUS)) { //No specific focus required

            if (grouping.equalsIgnoreCase("type")) { //One group for each type	
                for (int i = 0; i < l; i++) {
                    JSONObject superTrace = tracesTab.getJSONObject(i);
                    ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);

                    for (JSONObject trace : innerTraces) {
                        String type = "program"; //Defaut it is a program
                        int order = 4; //Programm order
                        if (trace.has("type")) { //in fact it is an equipment
                            type = trace.getString("type");
                            order = 2; //Device order
                        }
                        if (!groupFollower.containsKey(type)) {
                            if (trace.has("id")) {
                                JSONArray objs = new JSONArray();
                                objs.put(trace.get("id"));
                                groupFollower.put(type, new GroupTuple(order, objs));
                            }
                        } else {
                            JSONArray objs = groupFollower.get(type).getMembers();
                            if (trace.has("id")) {
                                if (!objs.toString().contains(trace.getString("id"))) {
                                    objs.put(trace.get("id"));
                                }
                            }
                        }
                    }
                }
            } else { //just the all group
                groupFollower.put("all", new GroupTuple(2, new JSONArray()));
                for (int i = 0; i < l; i++) {
                    JSONObject superTrace = tracesTab.getJSONObject(i);
                    ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);

                    for (JSONObject trace : innerTraces) {
                        JSONArray objs = groupFollower.get("all").getMembers();
                        if (trace.has("id") && !objs.toString().contains(trace.getString("id"))) {
                            objs.put(trace.get("id"));
                        }
                    }
                }
            }

        } else { //Focus required check the kind of focus

            if (focusType.equalsIgnoreCase("id")) { //Focus on something (equipment or program)
                groupFollower.put("focus", new GroupTuple(1, new JSONArray().put(focus)));
                groupFollower.put("others", new GroupTuple(3, new JSONArray()));

                if (grouping.equalsIgnoreCase("dep")) {//Group based on id dependency (focus, dependencies, others)
                    groupFollower.put("dependencies", new GroupTuple(2, new JSONArray()));

                    for (int i = 0; i < l; i++) {
                        JSONObject superTrace = tracesTab.getJSONObject(i);
                        ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);

                        for (JSONObject trace : innerTraces) {
                            JSONArray objs = null;

                            if (trace.has("id") && !trace.getString("id").equalsIgnoreCase(focus)) {//Not a trace from the focused id
                                if (trace.toString().contains(focus)) { //dep
                                    objs = groupFollower.get("dependencies").getMembers();
                                    //Remove dependency id from others array
                                    JSONArray others = new JSONArray();

                                    for (int j = 0; j < groupFollower.get("others").getMembers().length(); j++) {
                                        String id = groupFollower.get("others").getMembers().getString(j);
                                        if (trace.has("id") && !id.equalsIgnoreCase(trace.getString("id"))) {
                                            others.put(id);
                                        }
                                    }

                                    groupFollower.get("others").setMembers(others);

                                } else { //others
                                    if (trace.has("id") && !groupFollower.get("dependencies").toString().contains(trace.getString("id"))) {
                                        objs = groupFollower.get("others").getMembers();
                                    } else {
                                        objs = groupFollower.get("dependencies").getMembers();
                                    }
                                }

                                if (trace.has("id") && !objs.toString().contains(trace.getString("id"))) { //Check if the id is already in the array
                                    objs.put(trace.get("id"));
                                }
                            }
                        }
                    }

                } else { //One group focus and all in other
                    JSONArray objs = groupFollower.get("others").getMembers();
                    for (int i = 0; i < l; i++) {
                        JSONObject superTrace = tracesTab.getJSONObject(i);
                        ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);

                        for (JSONObject trace : innerTraces) {
                            if (trace.has("id") && !trace.getString("id").equalsIgnoreCase(focus) && !objs.toString().contains(trace.getString("id"))) {
                                objs.put(trace.get("id"));
                            }
                        }
                    }
                }

            } else if (focusType.equalsIgnoreCase("location")) { //focus on location name (location name, others)

                groupFollower.put(focus, new GroupTuple(1, new JSONArray()));
                groupFollower.put("others", new GroupTuple(3, new JSONArray()));

                for (int i = 0; i < l; i++) {
                    JSONObject superTrace = tracesTab.getJSONObject(i);
                    ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);

                    for (JSONObject trace : innerTraces) {
                        JSONArray objs = null;
                        if (trace.has("location")) { //Equipment
                            JSONObject loc = trace.getJSONObject("location");

                            if (loc.has("id") && loc.getString("id").equalsIgnoreCase("-1")) {
                                if (!groupFollower.get(focus).toString().contains(trace.getString("id"))) {
                                    objs = groupFollower.get("others").getMembers();
                                } else {
                                    objs = groupFollower.get("focus").getMembers();
                                }
                            } else {
                                if (loc.getString("name").equalsIgnoreCase(focus)) {
                                    objs = groupFollower.get(focus).getMembers();
                                    //Remove dependency id from others array
                                    JSONArray others = new JSONArray();
                                    for (int j = 0; j < groupFollower.get("others").getMembers().length(); j++) {
                                        String id = groupFollower.get("others").getMembers().getString(j);
                                        if (trace.has("id") && !id.equalsIgnoreCase(trace.getString("id"))) {
                                            others.put(id);
                                        }
                                    }
                                    groupFollower.get("others").setMembers(others);
                                } else {
                                    if (trace.has("id") && !groupFollower.get(focus).toString().contains(trace.getString("id"))) {
                                        objs = groupFollower.get("others").getMembers();
                                    } else {
                                        objs = groupFollower.get("focus").getMembers();
                                    }
                                }
                            }
                        } else { //Program
                            objs = groupFollower.get("others").getMembers();
                        }

                        if (trace.has("id") && !objs.toString().contains(trace.getString("id"))) {
                            objs.put(trace.get("id"));
                        }
                    }
                }

            } else if (focusType.equalsIgnoreCase("type")) { //focus on type (type, others)
                groupFollower.put(focus, new GroupTuple(1, new JSONArray()));
                groupFollower.put("others", new GroupTuple(3, new JSONArray()));

                for (int i = 0; i < l; i++) {
                    JSONObject superTrace = tracesTab.getJSONObject(i);
                    ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);

                    for (JSONObject trace : innerTraces) {
                        JSONArray objs = null;

                        String type = "program"; //Defaut it is a program
                        if (trace.has("type")) { //in fact it is an equipment
                            type = trace.getString("type");
                        }

                        if (type.equalsIgnoreCase(focus)) {
                            objs = groupFollower.get(focus).getMembers();
                        } else {
                            objs = groupFollower.get("others").getMembers();
                        }

                        if (trace.has("id") && !objs.toString().contains(trace.getString("id"))) {
                            objs.put(trace.get("id"));
                        }
                    }
                }
            }
        }

        //Fill the JSONArray with HashMap
        for (String key : groupFollower.keySet()) {
            JSONObject obj = new JSONObject();
            obj.put("name", getIntKey(key));
            GroupTuple group = groupFollower.get(key);
            obj.put("order", group.getOrder());
            obj.put("members", group.getMembers());
            groups.put(obj);
        }
        return groups;
    }

    /**
     * Compute the event line for debugger
     *
     * @param traces default traces tab
     * @param from start time stamp
     * @param to end time stamp
     * @return the event line as a JSONArray
     * @throws JSONException
     */
    private JSONArray eventLineComputation(JSONArray traces, long from, long to) throws JSONException {

        JSONArray eventLine = new JSONArray();
        int size = traces.length();
        JSONObject trace;
        long beg = from;
        long end = from + timeLineDelta;
        ArrayList<JSONObject> interval = new ArrayList<JSONObject>();

        if (size > 0) {

            if (traces.getJSONObject(0).getLong("timestamp") > from) {
                JSONObject firstEntry = new JSONObject();
                firstEntry.put("timestamp", from);
                firstEntry.put("value", 0);
                eventLine.put(firstEntry);
            }

            for (int i = 0; i < size; i++) {

                trace = traces.getJSONObject(i);
                long ts = trace.getLong("timestamp");

                if (ts >= beg && ts < end) {
                    interval.add(trace);
                } else {
                    if (!interval.isEmpty()) {
                        JSONObject entry = new JSONObject();
                        entry.put("timestamp", beg);
                        int nbEvent = 0;
                        for (JSONObject tr : interval) {
                            nbEvent += tr.getJSONArray("programs").length() + tr.getJSONArray("devices").length();
                        }
                        entry.put("value", nbEvent);
                        eventLine.put(entry);
                        interval.clear();
                    }
                    i--; //Ensure that all trace are placed in time stamp interval
                    beg = end;
                    end += timeLineDelta;
                }
            }

            if (!interval.isEmpty()) {
                JSONObject entry = new JSONObject();
                entry.put("timestamp", beg);
                int nbEvent = 0;
                for (JSONObject tr : interval) {
                    nbEvent += tr.getJSONArray("programs").length() + tr.getJSONArray("devices").length();
                }
                entry.put("value", nbEvent);
                eventLine.put(entry);
            }

        } else {
            JSONObject firstEntry = new JSONObject();
            firstEntry.put("timestamp", from);
            firstEntry.put("value", 0);
            eventLine.put(firstEntry);
        }

        JSONObject lastEntry = new JSONObject();
        lastEntry.put("timestamp", to);
        lastEntry.put("value", 0);
        eventLine.put(lastEntry);

        return eventLine;
    }

//	/**
//     * Filter trace on focus identifier
//     * @param tracesTab the focuses equipment identifier
//     * @throws JSONException 
//     */
//    private void filteringOnFocus(JSONArray tracesTab) throws JSONException {
//    	if(!focus.equalsIgnoreCase(TraceMan.NOFOCUS)) {
//    		int l = tracesTab.length();
//    		int i = 0;
//    	
//    		JSONArray filteredArray = new JSONArray();
//    	
//    		while(i < l) {
//    			JSONObject obj = tracesTab.getJSONObject(i);
//    			if(obj.toString().contains(focus)){
//    				filteredArray.put(obj);
//    			}
//    			i++;
//    		}
//    	
//    		tracesTab = filteredArray;
//    	}
//	}
    /**
     * Get the key use for internationalization from a type
     *
     * @param type the group name
     * @return the morph name from type to internationalization key
     */
    private String getIntKey(String type) {
        return "groups." + type.toLowerCase();
    }

    /**
     * Merge programs and equipment traces from a super traces into a simple
     * arraylist of JSONbject
     *
     * @param superTrace the super traces from any sources
     * @return an ArrayList<JSONObject> of all inner traces
     * @throws JSONException
     */
    private ArrayList<JSONObject> mergeInnerTraces(JSONObject superTrace) throws JSONException {
        ArrayList<JSONObject> innerTraces = new ArrayList<JSONObject>();

        JSONArray pgms = superTrace.getJSONArray("programs");
        JSONArray devices = superTrace.getJSONArray("devices");

        int nbPgms = pgms.length();
        int nbDev = devices.length();

        for (int i = 0; i < nbPgms; i++) {
            innerTraces.add(pgms.getJSONObject(i));
        }

        for (int j = 0; j < nbDev; j++) {
            innerTraces.add(devices.getJSONObject(j));
        }

        return innerTraces;
    }

    private JSONObject getJSONProgram(String id, String name, String change, String state, String iid) {
        JSONObject progNotif = new JSONObject();
        try {
            progNotif.put("id", id);
            progNotif.put("name", name);

            //Create the event description device entry
            JSONObject event = new JSONObject();
            JSONObject cause = null;
            {
                JSONObject s = new JSONObject();

                if (state.equalsIgnoreCase("deployed")) {
                    s.put("name", "disabled");
                } else if (state.equalsIgnoreCase("invalid")) {
                    s.put("name", state.toLowerCase());
                } else {
                    s.put("name", "enabled");
                }

                s.put("instruction_id", iid);
                event.put("state", s);
                if (change != null) {
                    JSONObject pName = Trace.addJSONPair(new JSONObject(), "name", name);
                    if (change.contentEquals("newProgram")) {
                        event.put("type", "appear");
                        cause = Trace.getJSONDecoration(
                                "newProgram", "user", name, null, "decorations.program_added", pName);
                    } else if (change.contentEquals("removeProgram")) {
                        event.put("type", "disappear");
                        cause = Trace.getJSONDecoration(
                                "removeProgram", "user", name, null, "decorations.program_deleted", pName);

                    } else { //change == "updateProgram"
                        event.put("type", "update");
                        cause = Trace.getJSONDecoration(
                                "updateProgram", "user", name, null, "decorations.program_saved", pName);
                    }
                }

            }
            progNotif.put("event", event);
            progNotif.put("decorations", new JSONArray().put(cause));

        } catch (JSONException e) {

        }
        return progNotif;
    }

    private JSONObject getDeviceState(String srcId, String varName, String value) {
        JSONObject deviceState = new JSONObject();
        GrammarDescription g = EHMIProxy.getGrammarFromDevice(srcId);
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
            if (varName.equalsIgnoreCase("status")) {
                deviceState.put("status", value);
            } else {
                deviceState.put("status", "2");
            }

        } catch (JSONException ex) {
        }
        return deviceState;
    }

    private JSONObject getDecorationNotification(ProgramCommandNotification n) {
        JSONObject p = getJSONProgram(n.getProgramId(), n.getProgramName(), null, n.getRunningState(), n.getInstructionId());
        JSONObject context = null;
        String desc = "decorations.defaultMessage";
        GrammarDescription gram = EHMIProxy.getGrammarFromDevice(n.getTargetId());
        if (gram != null) {
            context = gram.getContextFromParams(n.getDescription(), n.getParams());
            desc = gram.getTraceMessageFromCommand(n.getDescription());
        }
        JSONObject d = getJSONDevice(n.getTargetId(), null,
                Trace.getJSONDecoration(n.getType(), "Program", n.getSourceId(), null, desc, context));
        try {
            p.put("decorations", new JSONArray().put(
                    Trace.getJSONDecoration(n.getType(), "Program", null, n.getTargetId(), desc, context)));
        } catch (JSONException ex) {
        }
        return getCoreNotif(d, p);
    }

    /**
     * Get the current delta time for trace aggregation
     *
     * @return the delta time in milliseconds
     */
    public long getDeltaT() {
        return traceQueue.getDeltaTinMillis();
    }

    /**
     * Set the delta time for traces aggregation
     *
     * @param deltaTinMillis the new delta time value
     */
    public void setDeltaT(long deltaTinMillis) {
        traceQueue.setDeltaTinMillis(deltaTinMillis);
    }

    /**
     * Set the current time line delta value
     *
     * @param timeLineDelta the new time line delta value
     */
    public void setTimeLineDelta(long timeLineDelta) {
        this.timeLineDelta = timeLineDelta;
    }

    /**
     * Get the current time line delta time for trace aggregation
     *
     * @return the delta time in milliseconds
     */
    public long getTimeLineDelta() {
        return timeLineDelta;
    }

    /**
     * set the grouping policy
     *
     * @param order the policy to make group from
     */
    public void setGroupingOrder(String order) {
        this.grouping = order;
    }

    /**
     * Set the filtering identifier for trace
     *
     * @param focus the identifier use to filter trace
     * @param focusType the type of focus (location, type, equipment)
     */
    public void setFocusEquipment(String focus, String focusType) {
        this.focus = focus;
        this.focusType = focusType;
    }

    /**
     * Filter trace that not need to be trace in EHMI point view
     *
     * @param descr the equipment details
     * @param srcId the equipement identifier
     * @param varName the vriable name thaht change
     * @param value the new value to the variable
     * @return true if the trace can be trace, false otherwise
     */
    private boolean applyFilters(GrammarDescription descr, String srcId, String varName, String value) {
        //Filter on those conditions
        if (descr.getType().equalsIgnoreCase("ColorLight") && (varName.contentEquals("x")
                || varName.contentEquals("y")
                || varName.contentEquals("ct")
                || varName.contentEquals("speed")
                || varName.contentEquals("mode"))
                || descr.getType().equalsIgnoreCase("Temperature") && (varName.contentEquals("change"))
                || descr.getType().equalsIgnoreCase("Illumination") && (varName.contentEquals("label"))) {
            return false;
        }

        //Trace no need to be filtered
        return true;
    }

    @Override
    public int startDebugger() {
        //Socket and live trace initialization
        if (EHMIProxy.addClientConnexion(new TraceCmdListener(this), DEBUGGER_COX_NAME, DEBUGGER_DEFAULT_PORT)) {
            this.state = true;
            return DEBUGGER_DEFAULT_PORT;
        } else {
            if (this.state) {
                return DEBUGGER_DEFAULT_PORT;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean stopDebugger() {
        if (liveTracer != null) {
            liveTraceActivated = false;
            liveTracer.close();
            liveTracer = null;
        }

        if (fileTracer != null) {
            fileTraceActivated = false;
            fileTracer.close();
            fileTracer = null;
        }
        traceQueue.stop();

        if (EHMIProxy.removeClientConnexion(DEBUGGER_COX_NAME)) {
            state = false;
        }

        return !state;
    }

    /**
     * Initiate the live tracer
     *
     * @param refreshRate use to set up auto notification intervall
     * @return true if the live tracer is ready, false otherwise
     */
    public boolean initLiveTracer(long refreshRate) {
        liveTracer = new TraceRT(DEBUGGER_COX_NAME, EHMIProxy);
        liveTraceActivated = true;

        if (!traceQueue.isInitiated()) {
            if (refreshRate == 0) {
                traceQueue.initTraceExec();
            } else {
                traceQueue.initTraceExec(refreshRate);
            }
        }

        return liveTraceActivated;
    }

    /**
     * Initiate the file tracer
     *
     * @return true if the file tracer is initiated, false otherwise
     */
    public boolean initFileTracer() {
        fileTracer = new TraceFile();
        if (!fileTracer.init()) {
            LOGGER.warn("Unable to start the tracer");
            fileTraceActivated = false;
        } else {
            fileTraceActivated = true;
        }

        if (!traceQueue.isInitiated()) {
            traceQueue.initTraceExec();
        }

        return fileTraceActivated;
    }

    /**
     * Stop initiated lives mode
     *
     * @return true if all live mehtod are close
     */
    public boolean stopLive() {
        if (liveTracer != null) {
            liveTraceActivated = false;
            liveTracer.close();
            liveTracer = null;
        }

        if (fileTracer != null) {
            fileTraceActivated = false;
            fileTracer.close();
            fileTracer = null;
        }

        traceQueue.stop();

        return true;
    }

    @Override
    public JSONObject getStatus() {
        JSONObject status = new JSONObject();

        try {
            status.put("port", DEBUGGER_DEFAULT_PORT);
            status.put("state", this.state);

            String mode = "history";
            if (fileTraceActivated) {
                mode += "file ";
            }

            if (liveTraceActivated) {
                mode += "live ";
            }

            status.put("mode", mode);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return status;
    }

    /**
     * Get the systeme current time in milliseconds
     *
     * @return the current time as a long
     */
    public long getCurrentTimeInMillis() {
        return EHMIProxy.getCurrentTimeInMillis();
    }

}
