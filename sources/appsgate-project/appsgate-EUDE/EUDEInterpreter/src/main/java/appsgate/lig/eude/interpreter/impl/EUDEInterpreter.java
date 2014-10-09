package appsgate.lig.eude.interpreter.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.EndEventListener;
import appsgate.lig.eude.interpreter.langage.components.GraphManager;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEventListener;
import appsgate.lig.eude.interpreter.langage.nodes.NodeEvent;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.eude.interpreter.spec.EUDE_InterpreterSpec;
import appsgate.lig.eude.interpreter.spec.ProgramCommandNotification;
import appsgate.lig.eude.interpreter.spec.ProgramNotification;
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
    private EHMIProxySpec ehmiProxy;

    /**
     * Reference the ApAM HistoryManager.
     */
    private PropertyHistoryManager propHistoryManager;

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
    private final List<CoreEventListener> mapCoreNodeEvent;

    /**
     * HashMap that contains all the existing programs under a JSON format
     */
    private final HashMap<String, NodeProgram> mapPrograms;

    /**
     * The root program of the interpreter
     */
    private final NodeProgram root;

    /**
     * The proxy to the clock
     */
    public ClockProxy clock;

    /**
     * Constructor. Initialize the list of programs and of events
     *
     */
    public EUDEInterpreter() {
        mapPrograms = new HashMap<String, NodeProgram>();
        mapCoreNodeEvent = new ArrayList<CoreEventListener>();
        root = initRootProgram();
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        LOGGER.debug("A new instance of Mediator is created");
//        restorePrograms();
        LOGGER.debug("The interpreter component is initialized");
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        LOGGER.debug("The router interpreter components has been stopped");
        boolean bdFound = restorePrograms();
        // delete the event listeners from the context
        if (ehmiProxy != null) {
            for (CoreEventListener listener : mapCoreNodeEvent) {
                ehmiProxy.deleteCoreListener(listener);
            }
        }
        //save program map state
        if (bdFound) {
            contextHistory_push.pushData_change(this.getClass().getSimpleName(), "interpreter", "start", "stop", getProgramsDesc());
        }
    }

    @Override
    public boolean addProgram(JSONObject programJSON) {
        if (!restorePrograms()) {
            return false;
        }
        NodeProgram p = putProgram(programJSON);
        if (p == null) {
            LOGGER.warn("Unable to add program.");
            return false;
        }

        //save program map state
        if (contextHistory_push.pushData_add(this.getClass().getSimpleName(), p.getId(), p.getProgramName(), getProgramsDesc())) {
            p.setDeployed();
            notifyAddProgram(p.getId(), p.getState().toString(), p.getProgramName(), p.getJSONDescription());
            return true;
        } else {
            mapPrograms.remove(p.getId());
            return false;
        }
    }

    @Override
    public boolean removeProgram(String programId) {
        if (!restorePrograms()) {
            return false;
        }
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
            notifyRemoveProgram(p.getId(), p.getProgramName());
            return true;
        }
        LOGGER.debug("Unable to warn save the state");

        return false;
    }

    @Override
    public boolean update(JSONObject jsonProgram) {
        if (!restorePrograms()) {
            return false;
        }
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

        if (p.isRunning()) {
            p.removeEndEventListener(this);
            p.stop();
        }

        if (p.update(jsonProgram)) {
            notifyUpdateProgram(p.getId(), p.getState().toString(), p.getProgramName(), p.getJSONDescription());
            //save program map state

            if (contextHistory_push.pushData_add(this.getClass().getSimpleName(), p.getId(), p.getProgramName(), getProgramsDesc())) {
                return true;
            }
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
        if (!restorePrograms()) {
            return false;
        }

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
        if (!restorePrograms()) {
            return false;
        }

        NodeProgram p = mapPrograms.get(programId);

        if (p != null) {
            p.stop();
            return true;
        }

        return false;
    }

    /**
     *
     * @param node a program node
     * @return the list of id that contains a program
     */
    public List<String> getListProgramIds(NodeProgram node) {
        if (!restorePrograms()) {
            return null;
        }
        if (node == null) {
            node = root;
        }

        List<String> list = new ArrayList<String>();
        list.add(node.getId());
        for (NodeProgram n : node.getSubPrograms()) {
            list.addAll(getListProgramIds(n));
        }
        return list;
    }

    @Override
    public HashMap<String, JSONObject> getListPrograms() {
        if (!restorePrograms()) {
            return null;
        }

        HashMap<String, JSONObject> mapProgramJSON = new HashMap<String, JSONObject>();
        for (NodeProgram p : mapPrograms.values()) {
            mapProgramJSON.put(p.getId(), p.getJSONDescription());
        }

        return mapProgramJSON;
    }

    @Override
    public boolean isProgramActive(String programId) {
        if (!restorePrograms()) {
            return false;
        }

        NodeProgram p = mapPrograms.get(programId);

        if (p != null) {
            return (p.isRunning());
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
        if (!restorePrograms()) {
            return null;
        }

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
    public GenericCommand executeCommand(String objectId, String methodName, JSONArray args, ProgramCommandNotification notif) {
        if (ehmiProxy == null) {
            LOGGER.warn("No EHMI Proxy bound");
            return null;
        }

        GenericCommand command = ehmiProxy.executeRemoteCommand(objectId, methodName, args);
        if (command == null) {
            LOGGER.error("Command not found {}, for {}", methodName, objectId);
        } else {
            notifyChanges(notif);
            command.run();
        }
        return command;

    }

    /**
     * @return the current time in milliseconds
     */
    public Long getTime() {
        LOGGER.trace("getTime called");
        if (ehmiProxy == null) {
            LOGGER.warn("No EHMI Proxy bound");
            return null;
        }
        Long time = ehmiProxy.getCurrentTimeInMillis();
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
        if (ehmiProxy == null) {
            LOGGER.warn("No EHMI Proxy bound");
            return;
        }

        CoreEventListener listener = new CoreEventListener(nodeEvent.getSourceId(), nodeEvent.getEventName(), nodeEvent.getEventValue());

        for (CoreEventListener l : mapCoreNodeEvent) {
            if (l.equals(listener)) {
                LOGGER.debug("Add node event to listener list.");
                l.addNodeEvent(nodeEvent);
                return;
            }

        }
        listener.addNodeEvent(nodeEvent);
        mapCoreNodeEvent.add(listener);
        if (!nodeEvent.isProgramEvent()) {
            ehmiProxy.addCoreListener(listener);
        }
        LOGGER.debug("Add node event listener list.{}", nodeEvent.getEventName());

    }

    /**
     * Remove a node to notify
     *
     * @param nodeEvent Node to remove
     */
    public synchronized void removeNodeListening(NodeEvent nodeEvent) {

        CoreEventListener listener;

        listener = new CoreEventListener(nodeEvent.getSourceId(), nodeEvent.getEventName(), nodeEvent.getEventValue());

        for (CoreEventListener l : mapCoreNodeEvent) {
            if (l.equals(listener)) {
                LOGGER.debug("Add node event to listener list.");
                l.removeNodeEvent(nodeEvent);
                return;
            }

        }
    }

    /**
     *
     * @param id
     * @param runningState
     * @param source
     */
    private void notifyUpdateProgram(String id, String runningState, String name, JSONObject source) {
        if (runningState.equalsIgnoreCase("INVALID")) {
            newProgramStatus(id, ReferenceTable.STATUS.INVALID);
        } else {
            newProgramStatus(id, ReferenceTable.STATUS.OK);
        }
        notifyChanges(new ProgramNotification("updateProgram", id, runningState, name, source, null));
    }

    /**
     *
     * @param id
     * @param runningState
     * @param source
     */
    private void notifyAddProgram(String id, String runningState, String name, JSONObject source) {
        notifyChanges(new ProgramNotification("newProgram", id, runningState, name, source, null));
    }

    /**
     *
     * @param id
     */
    private void notifyRemoveProgram(String id, String name) {

        newProgramStatus(id, ReferenceTable.STATUS.MISSING);
        notifyChanges(new ProgramNotification("removeProgram", id, "", name, null, null));
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
        if (clock == null && ehmiProxy != null) {
            JSONArray devices = ehmiProxy.getDevices();
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
        if (!restorePrograms()) {
            return null;
        }

        ArrayList<Entry<String, Object>> properties = new ArrayList<Entry<String, Object>>();
        for (NodeProgram subProgram : root.getSubPrograms()) {
            for (String key : getListProgramIds(subProgram)) {
                properties.add(new AbstractMap.SimpleEntry<String, Object>(key, mapPrograms.get(key).getJSONDescription().toString()));
            }
        }
        return properties;
    }

    boolean synchro = false;

    /**
     * Retrieve the programs from database and put them in the interpreter
     */
    private synchronized boolean restorePrograms() {
        //restore places from data base
        if (synchro) {
            return true;
        } else if (contextHistory_pull != null && contextHistory_pull.testDB()) {
            synchro = true;

            LOGGER.debug("Restore interpreter program list from database");
            JSONObject userbase = contextHistory_pull.pullLastObjectVersion(this.getClass().getSimpleName());
            if (userbase != null) {
                try {
                    JSONArray state = userbase.getJSONArray("state");
                    for (int i = 0; i < state.length(); i++) {
                        LOGGER.trace("Restoring : " + state.getJSONObject(i));
                        JSONObject obj = state.getJSONObject(i);
                        String key = (String) obj.keys().next();
                        NodeProgram np;
                        np = putProgram(new JSONObject(obj.getString(key)));
                        if (np == null) {
                            LOGGER.error("Unable to restore a program");
                            return false;
                        }
                        if (np.isRunning()) {
                            //TODO:Restore complete interpreter and programs state
                            //this.callProgram(np.getId());
                            np.setDeployed();
                        }
                    }
                    LOGGER.debug("program list successfully synchronized with database");
                    return true;

                } catch (JSONException e) {
                    LOGGER.warn("JSONException: {}", e.getMessage());
                }
            }

        }
        return synchro;

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
            LOGGER.debug("By default the program is stored as a child of root");
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
        p = new NodeProgram(this, programJSON, parent);
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
    public EHMIProxySpec getContext() {
        return ehmiProxy;
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
    @Override
    public void endEventFired(EndEvent e) {
        NodeProgram p = (NodeProgram) e.getSource();
        LOGGER.info("Program " + p.getProgramName() + " ended.");
        for (CoreEventListener l : mapCoreNodeEvent) {
            if (l.equals(e)) {
                l.notifyEvent();
                return;
            }

        }
        p.addStartEventListener(this);
        // Check if no end Event is listened
    }

    @Override
    public void startEventFired(StartEvent e) {
        for (CoreEventListener l : mapCoreNodeEvent) {
            if (l.equals(e)) {
                l.notifyEvent();
                return;
            }

        }
    }

    /**
     * Method to make some mocked tests it allows to init the proxy that are
     * normally automatically managed with APAM
     *
     * @param pull the pull service
     * @param push the push service
     * @param ehmiProxy the proxy to ehmi
     */
    public void setTestMocks(DataBasePullService pull, DataBasePushService push, EHMIProxySpec ehmiProxy) {
        this.contextHistory_pull = pull;
        this.contextHistory_push = push;
        this.ehmiProxy = ehmiProxy;
    }

    @Override
    public String toString() {
        return "[EUDE Mediator]";
    }

    @Override
    public void newDeviceStatus(String deviceId, Boolean statusOK) {
        ReferenceTable.STATUS s = ReferenceTable.STATUS.OK;
        if (!statusOK) {
            s = ReferenceTable.STATUS.MISSING;
        }
        for (NodeProgram p : mapPrograms.values()) {
            p.setDeviceStatus(deviceId, s);
        }
    }

    /**
     *
     * @param deviceId
     * @param status
     */
    public void newProgramStatus(String deviceId, ReferenceTable.STATUS status) {
        for (NodeProgram p : mapPrograms.values()) {
            p.setProgramStatus(deviceId, status);
        }
    }

    @Override
    public void checkReferences() {
        for (NodeProgram p : mapPrograms.values()) {
            p.getReferences().checkStatus();
        }
    }

    @Override
    public JSONObject getGraph() {
        GraphManager gm = new GraphManager(this);
        gm.buildGraph();
        return gm.getGraph();
    }
}
