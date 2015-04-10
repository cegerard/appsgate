package appsgate.lig.ehmi.trace;

import appsgate.lig.context.dependency.spec.Dependencies;

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
import appsgate.lig.eude.interpreter.spec.ProgramNotification;
import appsgate.lig.eude.interpreter.spec.ProgramTraceNotification;
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
    private TraceMongo dbTracer;

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
    private void trace(JSONObject o, long timeStamp) {
        try {
            o.put("timestamp", timeStamp);

            //Delayed in queue to by aggregate by policy if real time tracing is actived
            if (liveTraceActivated || fileTraceActivated) {
                traceQueue.offer(o);
            }
            //Simple trace always save in data base
            dbTracer.trace(o);
        } catch (JSONException e) {
            //Exception won't occur
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
            LOGGER.error("No timestamp attached to the trace: {}", trace);
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
                LOGGER.error("SendTraces: No Object {}", i);
                return;
            }
        }
    }

    @Override
    public void commandHasBeenPassed(String objectID, String command, String caller, JSONArray jsonArgs, long timeStamp) {
        //if the equipment has been instantiated from ApAM spec before
        GrammarDescription grammar = getGrammar(objectID);
        if (grammar != null && grammar.generateTrace()) {
            JSONObject jsonDecoration = Trace.getJSONDecoration(Trace.DECORATION_TYPE.access, "write", caller, timeStamp, null, objectID, null, this.getDeviceName(objectID),
                    grammar.getTraceMessageFromCommand(command), grammar.getContextFromParams(command, jsonArgs));
            JSONObject deviceJson = Trace.getJSONDevice(objectID, null, jsonDecoration, grammar, this);
            //Create the notification JSON object
            JSONObject coreNotif = Trace.getCoreNotif(deviceJson, null);
            //Trace the notification JSON object in the trace file
            trace(coreNotif, timeStamp);
        } else {
            LOGGER.debug("This command [{}] to {} from [{}] does not generate a trace", command, objectID, caller);
        }    	
    }

    
    @Override
    public synchronized void commandHasBeenPassed(String objectID, String command, String caller, ArrayList<Object> args, long timeStamp) {
        //if the equipment has been instantiated from ApAM spec before
        GrammarDescription grammar = getGrammar(objectID);
        if (grammar != null && grammar.generateTrace()) {
            JSONObject jsonDecoration = Trace.getJSONDecoration(Trace.DECORATION_TYPE.access, "write", caller, timeStamp, null, objectID, null, this.getDeviceName(objectID),
                    grammar.getTraceMessageFromCommand(command), grammar.getContextFromParams(command, args));
            JSONObject deviceJson = Trace.getJSONDevice(objectID, null, jsonDecoration, grammar, this);
            //Create the notification JSON object
            JSONObject coreNotif = Trace.getCoreNotif(deviceJson, null);
            //Trace the notification JSON object in the trace file
            trace(coreNotif, timeStamp);
        } else {
            LOGGER.debug("This command [{}] to {} from [{}] does not generate a trace", command, objectID, caller);
        }
    }

    @Override
    public synchronized void coreEventNotify(long timeStamp, String srcId, String varName, String value) {
        LOGGER.debug("coreEventNotify(long timeStamp : {}, String srcId :{}, String varName : {}, String value : {})", timeStamp, srcId, varName, value);

        GrammarDescription desc = getGrammar(srcId);
        if (srcId != null && varName != null && value != null
                && desc != null && applyFilters(desc, srcId, varName, value) && desc.generateTrace()) {
            //Create the event description device entry
            JSONObject event = new JSONObject();
            JSONObject JDecoration;
            try {

                if (varName.equalsIgnoreCase("status")) {
                    if (value.equalsIgnoreCase("2")) {
                        event.put("type", "connection");
                        event.put("picto", Trace.getConnectionPicto());
                        JDecoration = Trace.getJSONDecoration(
                            Trace.DECORATION_TYPE.state, "connection", "technical", timeStamp, srcId, null, getDeviceName(srcId), null, "decorations.connection", null);
                    } else if (value.equalsIgnoreCase("0")) {
                        event.put("type", "disconnection");
                        event.put("picto", Trace.getDisconnectionPicto());
                        JDecoration = Trace.getJSONDecoration(
                                Trace.DECORATION_TYPE.state, "disconnection", "technical", timeStamp, srcId, null, getDeviceName(srcId), null,  "decorations.disconnection", null);
                    } else {
                        event.put("type", "update");
                        JDecoration = Trace.getJSONDecoration(
                                Trace.getDecorationType(desc.getType(), varName), "error", "technical", timeStamp, srcId, null, getDeviceName(srcId), null,  "decorations.error", null);
                    }
                } else {
                    event.put("type", "update");
                    String msg = "decorations." + desc.getType() + ".change." + varName;
                    JSONObject context = Trace.addString(new JSONObject(), value);
                    Trace.addJSONPair(context, "var", varName);
                    JDecoration = Trace.getJSONDecoration(
                            Trace.getDecorationType(desc.getType(), varName), "update", "technical", timeStamp, srcId, null, getDeviceName(srcId), null,  msg, context);
                }

                JSONObject jsonState = Trace.getDeviceState(srcId, varName, value, this);

                if (event.getString("type").equalsIgnoreCase("update")) {
                    String pictoState = Trace.getPictoState(desc.getType(), varName, value, jsonState);
                    event.put("picto", pictoState);
                    JDecoration.put("picto", pictoState);
                }

                event.put("state", jsonState);

                JSONObject deviceJson = Trace.getJSONDevice(srcId, event, JDecoration, desc, this);
                //Check if the trace is correclty formatted (v4)
                if (!deviceJson.getString("type").equalsIgnoreCase("")) {
                    //Create the notification JSON object
                    JSONObject coreNotif = Trace.getCoreNotif(deviceJson, null);
                    //Trace the notification JSON object in the trace file
                    trace(coreNotif, timeStamp);
                }

            } catch (JSONException e) {
                LOGGER.warn("Trace misformatted or data missing to build a valide trace: " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void coreUpdateNotify(long timeStamp, String srcId, String coreType,
            String userType, String name, JSONObject description, String eventType) {
        LOGGER.debug("coreUpdateNotify(long timeStamp : {}, String srcId :{}, String coreType,"
                + " String userType, String name, JSONObject description, String eventType)", timeStamp, srcId, coreType, userType, name, description, eventType);

        try {

            if (coreType.equalsIgnoreCase("newService")) {
                return;
            }
            if (filterType(userType)) {
                return;
            }

            JSONObject event = new JSONObject();
            JSONObject cause = new JSONObject();
            switch (eventType) {
                case "new":
                    event.put("type", "appear");
                    cause = Trace.getJSONDecoration(
                            Trace.DECORATION_TYPE.state, "appear", "technical", timeStamp, srcId, null, getDeviceName(srcId), null,  "decorations.appear",
                            Trace.addJSONPair(new JSONObject(), "name", name));
                    event.put("state", Trace.getDeviceState(srcId, "", "", this));
                    break;
                case "remove":
                    event.put("type", "disappear");
                    cause = Trace.getJSONDecoration(
                            Trace.DECORATION_TYPE.state, "disappear", "technical", timeStamp, srcId, getDeviceName(srcId), null,  null, "decorations.remove",
                            Trace.addJSONPair(new JSONObject(), "name", name));
                    break;
            }
            JSONObject jsonDevice = Trace.getJSONDevice(srcId, event, cause, getGrammar(srcId), this);
            JSONObject coreNotif = Trace.getCoreNotif(jsonDevice, null);
            //Trace the notification JSON object in the trace file
            trace(coreNotif, timeStamp);

        } catch (Exception e) {
            LOGGER.error("coreUpdateNotify(...), Exception occured : ", e);
        }

    }

    /**
     *
     * @param n
     */
    public synchronized void gotNotification(NotificationMsg n) {
        if (!(n instanceof ProgramNotification)) {
            return;
        }

        long timeStamp = getCurrentTimeInMillis();

        if (n instanceof ProgramTraceNotification) {
            JSONObject o = Trace.getDecorationNotification((ProgramTraceNotification) n, timeStamp, this);
            trace(o, timeStamp);
            return;
        }
        ProgramNotification notif = (ProgramNotification) n;
        //Create the notification JSON object
        //Create a device trace entry
        //Trace the notification JSON object in the trace file
        JSONObject jsonProgram = Trace.getJSONProgram(notif.getProgramId(), notif.getProgramName(), notif.getVarName(), notif.getRunningState(), null, timeStamp);

        trace(Trace.getCoreNotif(null, jsonProgram), timeStamp);
    }

    @Override
    public JSONArray getTraces(Long timestamp, Integer number) {
        JSONArray tracesTab = dbTracer.get(timestamp, number);
        if (traceQueue.getDeltaTinMillis() == 0) { //No aggregation
            return tracesTab;
        } else { // Apply aggregation policy
            //filteringOnFocus(tracesTab);
            traceQueue.stop();
            traceQueue.loadTraces(tracesTab);
            return traceQueue.applyAggregationPolicy(timestamp, null); //Call with default aggregation policy (id and time)
        }

    }

    /**
     * Send the last traces from now to windows milliseconds in the past
     *
     * @param dateNow the start date for data base request
     * @param window the time window in millisecond
     * @param obj the request from client
     */
    private void sendWindowPastTrace(long dateNow, long window, JSONObject obj) {
        JSONObject requestResult = new JSONObject();
        JSONArray tracesTab = dbTracer.getInterval(dateNow - window, dateNow);
        JSONObject result = new JSONObject();

        try {
            setGroupingOrder("type");
            result.put("groups", computeGroupsFromPolicy(tracesTab));
            result.put("eventline", eventLineComputation(tracesTab, dateNow - window, dateNow));
            result.put("data", tracesTab);
            requestResult.put("result", result);
            requestResult.put("request", obj);

            EHMIProxy.sendFromConnection(DEBUGGER_COX_NAME, obj.getInt("clientId"), requestResult.toString());

        } catch (JSONException e) {
            LOGGER.error("Unable to send windowPastTraces, missing argument in JSON: {}", obj);
            LOGGER.debug("Error message was: {}", e.getMessage());
        }
    }

    @Override
    public void getTracesBetweenInterval(Long from, Long to, boolean withEventLine, JSONObject request) {
        JSONObject requestResult = new JSONObject();
        JSONArray tracesTab;
        try {
            tracesTab = dbTracer.getLastState(request.getJSONObject("args").optJSONArray("ids"), from);
        } catch (JSONException ex) {
            LOGGER.error("Unable to get args");
            return;
        }
        //JSONArray tracesTab = dbTracer.getInterval(from, to);
        tracesTab = dbTracer.appendTraces(tracesTab, from, to);

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
                if (liveTracer != null) {
                    liveTracer.clearSubscribers();
                }
                traceQueue.loadTraces(tracesTab);//Third whole traces tab browse
                result.put("data", traceQueue.applyAggregationPolicy(from, null) /*Call with default aggregation policy (id and time)*/); //Fourth whole traces tab browse + in detail browsing
            }

            requestResult.put("result", result);
            requestResult.put("request", request);

            EHMIProxy.sendFromConnection(DEBUGGER_COX_NAME, request.getInt("clientId"), requestResult.toString());

        } catch (JSONException e) {
            LOGGER.error("Unable to getTracesBetweenIntervall, missing argument in JSON: {}", request);
            LOGGER.debug("Error message was: {}", e.getMessage());
        }
    }

    /**
     * Compute groups to display By default the type is to make group. If a
     * focus is define, the grouping policy can be type of dep
     *
     * @param tracesTab the trace tab use to compute group from
     * @return a JSONArray containing each group
     * @throws JSONException
     */
    private JSONArray computeGroupsFromPolicy(JSONArray tracesTab) throws JSONException {

        HashMap<String, GroupTuple> groupFollower;
        int l = tracesTab.length();

        if (focus.equalsIgnoreCase(TraceMan.NOFOCUS)) { //No specific focus required

            if (grouping.equalsIgnoreCase("type")) { //One group for each type	
                groupFollower = TraceTools.getTracesByType(tracesTab);
            } else { //just the all group
                groupFollower = new HashMap<>();

                groupFollower.put("all", new GroupTuple(2, new JSONArray()));
                for (int i = 0; i < l; i++) {
                    JSONObject superTrace = tracesTab.getJSONObject(i);
                    ArrayList<JSONObject> innerTraces = TraceTools.mergeInnerTraces(superTrace);

                    for (JSONObject trace : innerTraces) {
                        JSONArray objs = groupFollower.get("all").getMembers();
                        if (trace.has("id") && !objs.toString().contains(trace.getString("id"))) {
                            objs.put(trace.get("id"));
                        }
                    }
                }
            }

        } else { //Focus required check the kind of focus

            switch (focusType) {
                case "id":
                    if (grouping.equalsIgnoreCase("dep")) { //Group based on id dependency (focus, dependencies, others)
                        Dependencies programDependencies = (Dependencies) EHMIProxy.getProgramDependencies(focus);
                        groupFollower = TraceTools.getTracesByDep(tracesTab, focus, programDependencies);

                    } else { //One group focus and all in other
                        groupFollower = new HashMap<>();
                        groupFollower.put("focus", new GroupTuple(1, new JSONArray().put(focus)));
                        groupFollower.put("others", new GroupTuple(3, new JSONArray()));
                        JSONArray objs = groupFollower.get("others").getMembers();
                        for (int i = 0; i < l; i++) {
                            JSONObject superTrace = tracesTab.getJSONObject(i);
                            ArrayList<JSONObject> innerTraces = TraceTools.mergeInnerTraces(superTrace);

                            for (JSONObject trace : innerTraces) {
                                if (trace.has("id") && !trace.getString("id").equalsIgnoreCase(focus) && !objs.toString().contains(trace.getString("id"))) {
                                    objs.put(trace.get("id"));
                                }
                            }
                        }
                    }
                    break;
                case "location":
                    groupFollower = TraceTools.getTracesByLocation(tracesTab, focus);
                    break;
                case "type":
                    groupFollower = TraceTools.getTracesByType(tracesTab, focus);
                    break;
                default:
                    LOGGER.error("FocusType not recognized: {}", focusType);
                    groupFollower = new HashMap<>();
                    break;

            }
        }

        JSONArray groups = new JSONArray();

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
        ArrayList<JSONObject> interval = new ArrayList<>();

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
     * @param srcId the equipment identifier
     * @param varName the variable name that change
     * @param value the new value to the variable
     * @return true if the trace can be trace, false otherwise
     */
    private boolean applyFilters(GrammarDescription descr, String srcId, String varName, String value) {
        //Filter on those conditions

        switch (descr.getType().toUpperCase()) {
            case "COLORLIGHT":
                return !varName.contentEquals("x")
                        && !varName.contentEquals("y")
                        && !varName.contentEquals("ct")
                        && !varName.contentEquals("speed")
                        && !varName.contentEquals("mode");
            case "ILLUMINATION":
                return !varName.contentEquals("label");
            case "DOMICUBE":
                return varName.contentEquals("activeFace");
            case "TEMPERATURE":
                return !varName.contentEquals("change");
            case "WEATHEROBSERVER":
            case "MEDIAPLAYER":
            case "CLOCK":
                return false;
            default:
                return true;
        }
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
     * @param dateNow now time in milisecond
     * @param window the window time width
     * @param obj the live trace request received
     * @return true if the live tracer is ready, false otherwise
     */
    public boolean initLiveTracer(long refreshRate, long dateNow, long window, JSONObject obj) {
        if (liveTracer == null) {
            liveTracer = new TraceRT(DEBUGGER_COX_NAME, EHMIProxy);
        }
        liveTraceActivated = true;

        //send the first message concerning the window last milisecond data
        sendWindowPastTrace(dateNow, window, obj);

        //Save the clients id that request the live mode in order to send live trace to them only
        try {
            liveTracer.addSubscriber(obj.getInt("clientId"));
        } catch (JSONException e) {
            LOGGER.error("No clientId in initLiveTracer: {}", obj);
            liveTracer.close();
            liveTraceActivated = false;
            return false;
        }

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
            // Won't happen
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

    // TODO: do something cleaner than that
    private boolean filterType(String userType) {
        Integer type = Integer.getInteger(userType, 0);
        switch (type) {
            case 1: // temperature sensor
            case 2: // light sensor
            case 210: // domicube
            case 3: // switch
            case 4: // contact sensor
            case 5: // key card reader
            case 6: // plug
            case 7: // lamp
                return false;
            default: //Otherwise filter it
                return true;

        }

    }

    /**
     *
     * @param srcId
     * @return
     */
    public String getDeviceName(String srcId) {
        return devicePropTable.getName(srcId, "");
    }

    /**
     *
     * @param srcId
     * @return
     */
    public String getPlaceId(String srcId) {
        return placeManager.getCoreObjectPlaceId(srcId);
    }

    /**
     *
     * @param srcId
     * @return
     */
    public String getPlaceName(String srcId) {
        SymbolicPlace place = placeManager.getPlaceWithDevice(srcId);
        if (place != null) {
            return place.getName();
        }
        LOGGER.trace("Place not found for this device {}", srcId);
        return null;

    }

    /**
     *
     * @param id
     * @return
     */
    public GrammarDescription getGrammar(String id) {
        return EHMIProxy.getGrammarFromDevice(id);
    }

    /**
     *
     * @param srcId
     * @return
     */
    JSONObject getDevice(String srcId) {
        return EHMIProxy.getDevice(srcId);
    }
}
