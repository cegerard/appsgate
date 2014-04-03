package appsgate.lig.eude.interpreter.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.proxy.listeners.CoreListener;
import appsgate.lig.context.proxy.spec.ContextProxySpec;
import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.EndEventListener;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEventListener;
import appsgate.lig.eude.interpreter.langage.nodes.NodeEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram.RUNNING_STATE;
import appsgate.lig.eude.interpreter.spec.EUDE_InterpreterSpec;
import appsgate.lig.main.spec.CHMIProxySpec;
import appsgate.lig.main.spec.GenericCommand;
import appsgate.lig.manager.propertyhistory.services.PropertyHistoryManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * This class is the interpreter component for end user development environment.
 *
 * @author Cédric Gérard
 * @author Rémy Dautriche
 *
 * @since April 26, 2013
 * @version 1.0.0
 *
 */
public class EUDEInterpreter implements EUDE_InterpreterSpec, StartEventListener, EndEventListener {

    /**
     * Static class member uses to log what happened in each instances
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EUDEInterpreter.class);

    /**
     * Reference to the ApAM context proxy. Used to be notified when something
     * happen.
     */
    private ContextProxySpec contextProxy;

    /**
     * Reference to the ApAM router. Used to send action to the objects
     */
    private CHMIProxySpec chmiProxy;

    /**
     * Context history pull service to get past table state
     */
    private DataBasePullService contextHistory_pull;

    /**
     * Context history push service to save the current state
     */
    private DataBasePushService contextHistory_push;

    /**
     * Hash map containing the nodes and the events they are listening
     */
    private final HashMap<CoreEventListener, ArrayList<NodeEvent>> mapCoreNodeEvent;

    /**
     * HashMap that contains all the existing programs under a JSON format
     */
    private final HashMap<String, NodeProgram> mapPrograms;

    /**
     * The root program of the interpreter
     */
    private final NodeProgram root;

    /**
     *
     */
    public ClockProxy clock;

    /**
     * The property history proxy
     */
    private PropertyHistoryManager propHistoryManager;

    /**
     * Constructor. Initialize the list of programs and of events
     *
     */
    public EUDEInterpreter() {
        mapPrograms = new HashMap<String, NodeProgram>();
        mapCoreNodeEvent = new HashMap<CoreEventListener, ArrayList<NodeEvent>>();
        root = initRootProgram();
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        LOGGER.debug("A new instance of Mediator is created");
        restorePrograms();
        LOGGER.debug("The interpreter component is initialized");
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        LOGGER.debug("The router interpreter components has been stopped");
        // delete the event listeners from the context
        for (CoreEventListener listener : mapCoreNodeEvent.keySet()) {
            contextProxy.deleteListener(listener);
        }

        //save program map state
        contextHistory_push.pushData_change(this.getClass().getSimpleName(), "interpreter", "start", "stop", getProgramsDesc());
    }

    @Override
    public boolean addProgram(JSONObject programJSON) {
        NodeProgram p = putProgram(programJSON);
        if (p == null) {
            LOGGER.warn("Unable to add program.");
            return false;
        }
        //save program map state
        if (contextHistory_push.pushData_add(this.getClass().getSimpleName(), p.getId(), p.getProgramName(), getProgramsDesc())) {
            p.setDeployed();
            notifyAddProgram(p.getId(), p.getRunningState().toString(), p.getJSONDescription(), p.getUserSource());
            return true;
        } else {
            mapPrograms.remove(p.getId());
            return false;
        }
    }

    @Override
    public boolean removeProgram(String programId) {
        NodeProgram p = mapPrograms.get(programId);

        if (p == null) {
            LOGGER.error("The program " + programId + " does not exist.");
            return false;
        }
        if (p == root) {
            LOGGER.error("trying to remove the root program : this operation is not authorized");
            return false;
        }
        p.stop();
        p.removeEndEventListener(this);
        // remove the sub program from its father
        NodeProgram parent = (NodeProgram) p.getParent();
        if (parent == null) {
            LOGGER.warn("trying to remove a program without parent");
        } else {
            parent.removeSubProgram(programId);
        }
        mapPrograms.remove(programId);

        //save program map state
        if (contextHistory_push.pushData_remove(this.getClass().getSimpleName(), p.getId(), p.getProgramName(), getProgramsDesc())) {
            notifyRemoveProgram(p.getId());
            return true;
        }
        LOGGER.debug("Unable to warn save the state");

        return false;
    }

    @Override
    public boolean update(JSONObject jsonProgram) {
        String prog_id;
        try {
            prog_id = jsonProgram.getString("id");
        } catch (JSONException e) {
            LOGGER.error("EUDE error - updating programm - NO ID in JSON DESCRIPTION");
            return false;
        }
        NodeProgram p = mapPrograms.get(prog_id);

        if (p == null) {
            LOGGER.error("The program {} does not exist.", prog_id);
            return false;
        }

        try {
            if (p.getRunningState() == NodeProgram.RUNNING_STATE.STARTED
                    || p.getRunningState() == NodeProgram.RUNNING_STATE.PAUSED) {
                p.removeEndEventListener(this);
                p.stop();
            }

            if (p.update(jsonProgram)) {
                notifyUpdateProgram(p.getId(), p.getRunningState().toString(), p.getJSONDescription(), p.getUserSource());
                //save program map state

                if (contextHistory_push.pushData_add(this.getClass().getSimpleName(), p.getId(), p.getProgramName(), getProgramsDesc())) {
                    return true;
                }
            }

        } catch (SpokException ex) {
            LOGGER.error("Unable to update the program with new properties. NodeException catched: {}", ex.getMessage());
        }

        return false;
    }

    @Override
    public boolean callProgram(String programId) {
        return callProgram(programId, null);
    }

    /**
     *
     * @param programId the id of the program to call
     * @param args the args of the program
     * @return true if the program has been called
     */
    @Override
    public boolean callProgram(String programId, JSONArray args) {
        NodeProgram p = mapPrograms.get(programId);
        JSONObject calledStatus = null;

        if (p != null) {
            p.getSymbolTable().fillWith(args);
            p.addEndEventListener(this);
            calledStatus = p.call();
        }

        return (calledStatus != null);

    }

    @Override
    public boolean stopProgram(String programId) {
        NodeProgram p = mapPrograms.get(programId);

        if (p != null) {
            p.stop();
            return true;
        }

        return false;
    }

    @Override
    public boolean pauseProgram(String programId) {
        NodeProgram p = mapPrograms.get(programId);

        if (p != null) {
            return p.pause();
        }

        return false;
    }

    /**
     *
     * @param node a program node
     * @return the list of id that contains a program
     */
    public List<String> getListProgramIds(NodeProgram node) {
        List<String> list = new ArrayList<String>();
        list.add(node.getId());
        for (NodeProgram n : node.getSubPrograms()) {
            list.addAll(getListProgramIds(n));
        }
        return list;
    }

    @Override
    public HashMap<String, JSONObject> getListPrograms() {
        HashMap<String, JSONObject> mapProgramJSON = new HashMap<String, JSONObject>();
        for (NodeProgram p : mapPrograms.values()) {
            mapProgramJSON.put(p.getId(), p.getJSONDescription());
        }

        return mapProgramJSON;
    }

    @Override
    public boolean isProgramActive(String programId) {
        NodeProgram p = mapPrograms.get(programId);

        if (p != null) {
            return (p.getRunningState() == RUNNING_STATE.STARTED);
        }

        return false;
    }

    /**
     * Getter for a node program. Used for the event
     *
     * @param programId identifier of program to get
     * @return Node of the program if found, null otherwise
     */
    public NodeProgram getNodeProgram(String programId) {
        return mapPrograms.get(programId);
    }

    /**
     * Execute a method call on the router
     *
     * @param objectId the id of the object on which applying the command
     * @param methodName the name of the command
     * @param args the args to pass to the command
     * @return the command to be executed
     */
    public GenericCommand executeCommand(String objectId, String methodName, JSONArray args) {
        return chmiProxy.executeCommand(objectId, methodName, args);
    }

    /**
     * @return the current time in milliseconds
     */
    public Long getTime() {
        LOGGER.trace("getTime called");
        GenericCommand cmd = executeCommand(getClock().getId(), "getCurrentTimeInMillis", new JSONArray());
        LOGGER.debug("cmd: " + cmd.toString());
        cmd.run();
        Long time = (Long) cmd.getReturn();
        LOGGER.info("Time is: " + time);
        return time;
    }

    /**
     * Add a node to notify when the specified event has been caught. The node
     * is notified only when the event is received
     *
     * @param nodeEvent Node to notify when the event is received
     */
    public synchronized void addNodeListening(NodeEvent nodeEvent) {
        // instantiate a core listener

        CoreEventListener listener = new CoreEventListener(nodeEvent.getSourceId(), nodeEvent.getEventName(), nodeEvent.getEventValue(), this);

        Set<CoreEventListener> keyset = mapCoreNodeEvent.keySet();
        Iterator<CoreEventListener> it = keyset.iterator();
        boolean contains = false;
        CoreEventListener cel = null;

        while (it.hasNext() && !contains) {
            cel = it.next();
            if (cel.equals(listener)) {
                contains = true;
            }
        }

        // if the event is already listened by other nodes
        if (contains) {
            mapCoreNodeEvent.get(cel).add(nodeEvent);
            LOGGER.debug("Add node event to listener list.");
            // if the event is not listened yet
        } else {
            // create the list of nodes to notify
            ArrayList<NodeEvent> nodeList = new ArrayList<NodeEvent>();
            nodeList.add(nodeEvent);

            // add the listener to the context
            contextProxy.addListener(listener);

            // fill the map with the new entry
            mapCoreNodeEvent.put(listener, nodeList);
            LOGGER.debug("Add node event listener list.{}", nodeEvent.getEventName());
        }
    }

    /**
     * Remove a node to notify
     *
     * @param nodeEvent Node to remove
     */
    public synchronized void removeNodeListening(NodeEvent nodeEvent) {

        CoreEventListener listener;

        listener = new CoreEventListener(nodeEvent.getSourceId(), nodeEvent.getEventName(), nodeEvent.getEventValue(), this);

        Set<CoreEventListener> keyset = mapCoreNodeEvent.keySet();
        Iterator<CoreEventListener> it = keyset.iterator();
        boolean contains = false;
        CoreEventListener cel = null;

        while (it.hasNext() && !contains) {
            cel = it.next();
            if (cel.equals(listener)) {
                contains = true;
            }
        }

        if (contains) {
            ArrayList<NodeEvent> nodeEventList = mapCoreNodeEvent.get(cel);
            nodeEventList.remove(nodeEvent);
            LOGGER.debug("Remove nodeEvent from listener list.");
            // remove the listener if there is no node any more to notify
            if (nodeEventList.isEmpty()) {
                contextProxy.deleteListener(cel);
                mapCoreNodeEvent.remove(cel);
                LOGGER.debug("Remove node event listener list.");
            }
        }
    }

    /**
     *
     * @param id
     * @param runningState
     * @param source
     * @param userSource
     */
    private void notifyUpdateProgram(String id, String runningState, JSONObject source, String userSource) {
        notifyChanges(new ProgramNotification("updateProgram", id, runningState, source, userSource));
    }

    /**
     *
     * @param id
     * @param runningState
     * @param source
     * @param userSource
     */
    private void notifyAddProgram(String id, String runningState, JSONObject source, String userSource) {
        notifyChanges(new ProgramNotification("newProgram", id, runningState, source, userSource));
    }

    /**
     *
     * @param id
     */
    private void notifyRemoveProgram(String id) {
        notifyChanges(new ProgramNotification("removeProgram", id, RUNNING_STATE.STOPPED.toString(), null, ""));
    }

    /**
     * This method uses the ApAM message model. Each call produce a
     * ProgramStateNotificationMsg object and notifies ApAM that a new message
     * has been released.
     *
     * @return nothing, it just notifies ApAM that a new message has been
     * posted.
     */
    public NotificationMsg notifyChanges(NotificationMsg notif) {
        return notif;

    }

    /**
     * @return the clock proxy
     */
    public ClockProxy getClock() {
        if (clock == null) {
            JSONArray devices = chmiProxy.getDevices();
            for (int i = 0; i < devices.length(); i++) {
                try {
                    if (devices.getJSONObject(i).optInt("type") == 21) {
                        clock = new ClockProxy(devices.getJSONObject(i));
                    }
                } catch (JSONException ex) {
                    LOGGER.warn("A Json Exception occured during parsing device list");
                    clock = null;
                }
            }
        }
        return clock;
    }

    /**
     *
     * @return the description of the programs for internal use
     */
    private ArrayList<Entry<String, Object>> getProgramsDesc() {
        ArrayList<Entry<String, Object>> properties = new ArrayList<Entry<String, Object>>();
        for (NodeProgram subProgram : root.getSubPrograms()) {
            for (String key : getListProgramIds(subProgram)) {
                properties.add(new AbstractMap.SimpleEntry<String, Object>(key, mapPrograms.get(key).getJSONDescription().toString()));
            }
        }
        return properties;
    }

    /**
     * Retrieve the programs from database and put them in the interpreter
     */
    private void restorePrograms() {
        LOGGER.debug("Restore interpreter program list from database");
        JSONObject userbase = contextHistory_pull.pullLastObjectVersion(this.getClass().getSimpleName());
        if (userbase != null) {
            try {
                JSONArray state = userbase.getJSONArray("state");
                for (int i = 0; i < state.length(); i++) {
                    JSONObject obj = state.getJSONObject(i);
                    String key = (String) obj.keys().next();
                    NodeProgram np;
                    np = putProgram(new JSONObject(obj.getString(key)));
                    if (np == null) {
                        LOGGER.error("Unable to restore a program");
                        return;
                    }
                    if (np.getRunningState() == RUNNING_STATE.STARTED) {
                        //TODO:Restore complete interpreter and programs state
                        this.callProgram(np.getId());
                    }
                }
            } catch (JSONException e) {
                LOGGER.warn("JSONException: {}", e.getMessage());
            }
        }

    }

    /**
     *
     * @return the root nodeProgram, null if it has not been inited
     */
    private NodeProgram initRootProgram() {
        try {
            InputStream in = this.getClass().getResourceAsStream("root.json");
            if (in == null) {
                LOGGER.error("unable to read root.json");
                return null;
            }
            InputStreamReader is = new InputStreamReader(in);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(is);
            String read = br.readLine();

            while (read != null) {
                //System.out.println(read);
                sb.append(read);
                read = br.readLine();

            }

            JSONObject o = new JSONObject(sb.toString());
            return new NodeProgram(this, o, null);

        } catch (SpokException ex) {
            LOGGER.error("unable to build root program from file.");
            LOGGER.debug(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.error("An error occured during reading file");
        } catch (JSONException ex) {
            LOGGER.error("An error occured during parsing the file");
            LOGGER.debug(ex.getMessage());
        }
        return null;
    }

    /**
     *
     * @param programJSON
     * @return the parent program of the program
     */
    private NodeProgram getProgramParent(JSONObject programJSON) {
        String packageName = programJSON.optString("package");
        if (packageName == null || packageName.isEmpty() || packageName.equalsIgnoreCase("root")) {
            LOGGER.warn("By default the program is stored as a child of root");
            return root;
        }
        String parentId;
        if (packageName.contains(".")) {
            parentId = packageName.substring(packageName.lastIndexOf(".") + 1);
        } else {
            parentId = packageName;
        }
        if (mapPrograms.containsKey(parentId)) {
            return mapPrograms.get(parentId);
        }
        LOGGER.error("the parent id has not been found: {}", parentId);
        return root;
    }

    /**
     *
     * @param programJSON the JSON description of the program
     * @return the node program corresponding to the JSON description passed as
     * argument
     */
    private NodeProgram putProgram(JSONObject programJSON) {
        NodeProgram p;

        NodeProgram parent = getProgramParent(programJSON);

        // initialize a program node from the JSON
        try {
            p = new NodeProgram(this, programJSON, parent);
        } catch (SpokException e) {
            LOGGER.error("Node error detected while loading a program: {}", e.getMessage());
            return null;
        }
        if (parent != null && !parent.addSubProgram(p.getId(), p)) {
            LOGGER.error("The program already has a subprogram of this id");
            return null;
        }
        mapPrograms.put(p.getId(), p);
        return p;
    }

    /**
     * Method to get the context
     *
     * @return the context proxy
     */
    public ContextProxySpec getContext() {
        return contextProxy;
    }

    /**
     * @return the property history manager
     */
    public PropertyHistoryManager getPropHistManager() {
        return propHistoryManager;
    }

    /**
     *
     */
    public class CoreEventListener implements CoreListener {

        /**
         *
         */
        private String objectId;
        /**
         *
         */
        private String varName;
        /**
         *
         */
        private String varValue;
        /**
         *
         */
        private final EUDEInterpreter eudeInt;

        /**
         *
         * @param objectId
         * @param varName
         * @param varValue
         * @param eudeInt
         */
        public CoreEventListener(String objectId, String varName,
                String varValue, EUDEInterpreter eudeInt) {
            this.objectId = objectId;
            this.varName = varName;
            this.varValue = varValue;
            this.eudeInt = eudeInt;
        }

        @Override
        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        @Override
        public void setEvent(String eventVarName) {
            this.varName = eventVarName;
        }

        @Override
        public void setValue(String eventVarValue) {
            this.varValue = eventVarValue;
        }

        @Override
        public String getObjectId() {
            return objectId;
        }

        @Override
        public String getEvent() {
            return varName;
        }

        @Override
        public String getValue() {
            return varValue;
        }

        @Override
        public void notifyEvent() {
            LOGGER.debug("Event notified");
            // transmit the core event to the concerned nodes
            synchronized (eudeInt) {
                ArrayList<NodeEvent> nodeEventList = mapCoreNodeEvent.get(this);

                mapCoreNodeEvent.remove(this);
                if (nodeEventList == null) {
                    LOGGER.warn("No CoreEvent found");
                    return;
                }
                for (NodeEvent n : nodeEventList) {
                    LOGGER.debug("Notifying node: {}", n);
                    n.coreEventFired();
                }
                contextProxy.deleteListener(this);
            }
        }

        @Override
        public void notifyEvent(CoreListener listener) {
            LOGGER.debug("The event is catch by the EUDE " + listener);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CoreEventListener)) {
                return false;
            }

            CoreEventListener c = (CoreEventListener) o;
            return (objectId.contentEquals(c.objectId) && varName.contentEquals(c.varName) && varValue.contentEquals(c.varValue));
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.objectId != null ? this.objectId.hashCode() : 0);
            hash = 17 * hash + (this.varName != null ? this.varName.hashCode() : 0);
            hash = 17 * hash + (this.varValue != null ? this.varValue.hashCode() : 0);
            return hash;
        }
    }

    @Override
    public void endEventFired(EndEvent e) {
        NodeProgram p = (NodeProgram) e.getSource();
        LOGGER.info("Program " + p.getProgramName() + " ended.");
    }

    @Override
    public void startEventFired(StartEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * Method to make some mocked tests
     *
     * @param pull
     * @param push
     */
    public void setTestMocks(DataBasePullService pull, DataBasePushService push, CHMIProxySpec chmiProxy, ContextProxySpec c) {
        this.contextHistory_pull = pull;
        this.contextHistory_push = push;
        this.chmiProxy = chmiProxy;
        this.contextProxy = c;
    }

    @Override
    public String toString() {
        return "[EUDE Mediator]";
    }

}
