package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.spec.ProgramStateNotification;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node program for the mediator. Contains the metadata of the program, the
 * parameters, the variables and the rules
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since May 22, 2013
 * @version 1.0.0
 *
 */
final public class NodeProgram extends Node {


    /**
     * Program running state static enumeration
     *
     * @author Cédric Gérard
     * @since September 13, 2013
     */
    public static enum RUNNING_STATE {

        INVALID("INVALID"), DEPLOYED("DEPLOYED"), PROCESSING("PROCESSING"), WAITING("WAITING"), KEEPING("KEEPING");

        private String name = "";

        RUNNING_STATE(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeProgram.class);

    /**
     * Program's id set by the EUDE editor
     */
    private String id;

    /**
     * @param i the id to set
     */
    protected void setId(String i) {
        id = i;
    }

    /**
     * Program's name given by the user
     */
    private String name;

    /**
     * User's name who wrote the program
     */
    private JSONObject header;

    /**
     * The json program
     */
    private JSONObject programJSON = null;

    /**
     * Sequence of rules to interpret
     */
    private Node body;

    /**
     * Sub programs
     */
    private HashMap<String, NodeProgram> subPrograms;

    /**
     * Object representing active nodes of the program
     */
    private JSONObject activeNodes;

    /**
     * Object counting the number of times node were executed
     */
    private JSONObject nodesCounter;

    /**
     * The current running state of this program - DEPLOYED - INVALID -
     * PROCESSING - WAITING
     */
    private RUNNING_STATE runningState = RUNNING_STATE.DEPLOYED;

    /**
     * Pointer to the interpreter, could be the interpreter for the simulator
     */
    private EUDEInterpreter mediator = null;

    private ReferenceTable references = null;

    /**
     * Default constructor
     *
     * @param i
     * @param p
     * @constructor
     */
    public NodeProgram(EUDEInterpreter i, Node p) {
        super(p);
        this.mediator = i;
        subPrograms = new HashMap<String, NodeProgram>();
        references = new ReferenceTable(i, this.id);
    }

    /**
     * Initialize the program from a JSON object
     *
     * @param mediator
     * @param o Abstract tree of the program in JSON
     * @param p the node parent
     */
    public NodeProgram(EUDEInterpreter mediator, JSONObject o, Node p) {
        this(mediator, p);
        if (!o.has("body")) {
            LOGGER.error("this program has no body");
            setInvalid();
            return;
        }
        this.programJSON = o.optJSONObject("body");
        // initialize the program with the JSON
        try {

            id = getJSONString(o, "id");
            if (o.has("runningState")) {
                LOGGER.trace("Running state: {}", o.optString("runningState"));
                runningState = RUNNING_STATE.valueOf(getJSONString(o, "runningState"));
            }
            if (isValid()) {
                update(o);
            }

        } catch (SpokNodeException ex) {
            setInvalid();
        }
    }

    @Override
    public EUDEInterpreter getMediator() {
        return this.mediator;
    }

    /**
     * Update the current program source code Program need to be stopped.
     *
     * @param json the new source code
     *
     * @return true if the source code has been updated, false otherwise
     */
    public final boolean update(JSONObject json) {

        try {
            name = getJSONString(json, "name");
            header = getJSONObject(json, "header");

            this.setSymbolTable(new SymbolTable(json.optJSONArray("definitions"), this));
            body = Builder.nodeOrNull(getJSONObject(json, "body"), this);
            this.programJSON = getJSONObject(json, "body");

            return this.buildReferences();
        } catch (SpokException ex) {
            LOGGER.error("Unable to parse a specific node: {}", ex.getMessage());
        }
        setInvalid();
        return false;

    }

    /**
     *
     */
    private Boolean buildReferences() {
        if (this.body == null) {
            return false;
        }
        this.references = new ReferenceTable(mediator, this.id);
        this.body.buildReferences(this.references);
        ReferenceTable.STATUS newStatus = this.references.checkReferences();
        if (newStatus != ReferenceTable.STATUS.OK) {
            setInvalid();
            return false;
        }
        setValid();
        return true;
    }

    public ReferenceTable getReferences() {
        return this.references;
    }

    /**
     * Launch the interpretation of the rules
     *
     * @return integer
     */
    @Override
    public JSONObject call() {
        JSONObject ret = new JSONObject();
        activeNodes = new JSONObject();
        nodesCounter = new JSONObject();
        if (runningState == RUNNING_STATE.DEPLOYED) {
            //setProcessing(this.body.getIID());
            fireStartEvent(new StartEvent(this));
            body.addStartEventListener(this);
            body.addEndEventListener(this);
            body.call();
            try {
                ret.put("status", true);
            } catch (JSONException ex) {
                // Do nothing since 'JSONObject.put(key,val)' would raise an exception
                // only if the key is null, which will never be the case
            }
            return ret;
        }
        try {
            LOGGER.warn("Trying to start {} while: {}", this, runningState.toString());
            ret.put("status", false);
        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return ret;
    }

    @Override
    public void stop() {
        if (runningState == RUNNING_STATE.INVALID) {
            LOGGER.warn("Trying to stop {}, but this program is invalid", this);
            return;
        }

        if (runningState == RUNNING_STATE.DEPLOYED) {
            LOGGER.warn("Trying to stop {}, but this program is not currently running.", this);
            return;
        }
        if (!isStopping()) {
            LOGGER.debug("Stoping program {}", this);
            setStopping(true);
            body.stop();
            body.removeEndEventListener(this);
            //setDeployed();
            fireEndEvent(new EndEvent(this));
            setStopping(false);
        } else {
            LOGGER.warn("Trying to stop {}, while already stopping", this);
        }
    }

    @Override
    protected void specificStop() {
    }

    /**
     * Set the current running state to deployed
     */
    final public void setDeployed() {
        setRunningState(RUNNING_STATE.DEPLOYED, null);
    }

    /**
     * set the state of the program to invalid
     */
    private void setInvalid() {
        setRunningState(RUNNING_STATE.INVALID, null);
    }

    /**
     * set the state to a valid state
     */
    private void setValid() {
        if (runningState == RUNNING_STATE.INVALID) {
            setRunningState(RUNNING_STATE.DEPLOYED, id);
        }
    }

    /**
     * @return true if the program can be run, false otherwise
     */
    final public boolean canRun() {
        return this.runningState == RUNNING_STATE.DEPLOYED;
    }

    /**
     * @return true if the program can be stopped, false otherwise
     */
    final public boolean isRunning() {
        return (this.runningState == RUNNING_STATE.PROCESSING || this.runningState == RUNNING_STATE.WAITING || this.runningState == RUNNING_STATE.KEEPING);
    }

    /**
     * @return the current state of the program
     */
    final public RUNNING_STATE getState() {
        return runningState;
    }

    /**
     * @return true if the program is valid
     */
    final public boolean isValid() {
        return this.runningState != RUNNING_STATE.INVALID;
    }

    /**
     * set the state of this program to waiting, if this program is already
     * running
     *
     * @param iid
     */
    final public void setWaiting(String iid) {
        if (isValid()) {
            setRunningState(RUNNING_STATE.WAITING, iid);
        } else {
            LOGGER.warn("Trying to set {} waiting, while being {}", this, this.runningState);
        }
    }

    /**
     * set the state to processing if the program is valid
     *
     * @param iid
     */
    public void setProcessing(String iid) {
        if (isValid()) {
            setRunningState(RUNNING_STATE.PROCESSING, iid);
        } else {
            LOGGER.warn("Trying to set {} processing, while being {}", this, this.runningState);
        }
    }
    void setKeeping(String iid) {
        if (isValid()){
            setRunningState(RUNNING_STATE.KEEPING, iid);
        } else {
            LOGGER.warn("Trying to set {} processing, while being {}", this, this.runningState);
        }
    }

    @Override
    public void startEventFired(StartEvent e) {
        LOGGER.debug("The start event ({}) has been catched by {}", e.getSource(), this);
    }

    @Override
    public void endEventFired(EndEvent e) {
        setDeployed();
        fireEndEvent(new EndEvent(this));
    }

    /**
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return the program name
     */
    @Override
    public String getProgramName() {
        return name;
    }

    /**
     *
     * @return the author
     */
    public String getAuthor() {
        return header.optString("author");
    }

    /**
     * @return the JSON source of the program
     */
    public JSONObject getJSONSource() {
        return programJSON;
    }

    /**
     * @param runningState
     */
    private void setRunningState(RUNNING_STATE runningState, String iid) {
        if (runningState != this.runningState) {
            this.runningState = runningState;
            getMediator().notifyChanges(new ProgramStateNotification(id, "runningState", this.runningState.toString(), name, iid));
        }
    }

    @Override
    public String toString() {
        return "[Node Program : " + name + "]";
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("type", "program");
            o.put("runningState", runningState.name);
            o.put("name", name);
            o.put("header", header);
            o.put("package", getPath());

            o.put("body", getJSONSource());

            o.put("activeNodes", activeNodes);
            o.put("nodesCounter", nodesCounter);

            o.put("definitions", getSymbolTableDescription());

        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;

    }

    /**
     * @return the script of a program, more readable than the json structure
     */
    @Override

    public String getExpertProgramScript() {
        SymbolTable vars = new SymbolTable(this);
        this.setSymbolTable(vars);
        return this.getHeader() + vars.getExpertProgramDecl() + "\n" + this.getBodyScript();
    }

    private String getBodyScript() {
        if (body != null) {
            return body.getExpertProgramScript();
        }
        return "";
    }

    /**
     * @return the header of a program
     */
    private String getHeader() {
        String ret = "";
        if (!getAuthor().isEmpty()) {
            ret += "Author: " + getAuthor() + "\n";
        }
        return ret;
    }

    @Override
    protected Node copy(Node parent) {
        NodeProgram ret = new NodeProgram(getMediator(), parent);
        ret.id = id;
        ret.runningState = runningState;
        ret.name = name;
        ret.header = new JSONObject(header);

        if (body != null) {
            ret.body = body.copy(ret);
        }
        return ret;
    }

    /**
     *
     * @return
     */
    public String getPath() {
        NodeProgram p = (NodeProgram) this.getParent();
        if (p != null) {
            return p.recursivePath();
        }
        return "";
    }

    private String recursivePath() {
        NodeProgram p = (NodeProgram) this.getParent();
        if (p == null) {
            return id;
        }
        return p.recursivePath() + "." + id;
    }

    /**
     * Add a sub program if no program already exists
     *
     * @param id
     * @param subProgram
     * @return
     */
    public boolean addSubProgram(String id, NodeProgram subProgram) {
        if (id == null || id.isEmpty()) {
            LOGGER.warn("Unable to add a sub program without an id");
            return false;
        }
        if (subProgram == this || this.id.equalsIgnoreCase(id)) {
            LOGGER.warn("trying to add the program as its own child");
            return false;
        }
        if (subPrograms.containsKey(id)) {
            LOGGER.warn("A sub program with this name [{}] already exists", id);
            return false;
        }
        subPrograms.put(id, subProgram);
        return true;
    }

    /**
     * Retrieve a program with a given id
     *
     * @param id the name of the program
     * @return the program corresponding to the name if it exists, null
     * otherwise
     */
    public NodeProgram getSubProgram(String id) {
        if (id == null || id.isEmpty()) {
            LOGGER.warn("No id has been passed");
            return null;
        }
        return subPrograms.get(id);
    }

    /**
     * Remove a sub program from the list of sub programs
     *
     * @param id the id of the sub program
     * @return true if the program has been removed, false if the program did
     * not exist
     */
    public boolean removeSubProgram(String id) {
        if (subPrograms.containsKey(id)) {
            LOGGER.trace("A sub program [{}] has been removed.", id);
            subPrograms.remove(id);
            return true;
        }
        LOGGER.debug("The sub program [{}] has not been found", id);
        return false;
    }

    /**
     *
     * @return the sub programs
     */
    public Collection<NodeProgram> getSubPrograms() {
        if (subPrograms != null) {
            return subPrograms.values();
        }
        return new HashSet<NodeProgram>();
    }

    /**
     * Method to get a device change and propagate it to the reference table
     *
     * @param id the id of the device
     * @param s the status
     */
    public void setDeviceStatus(String id, ReferenceTable.STATUS s) {
        if (references.setDeviceStatus(id, s)) {
            changeStatus();
        }
    }

    /**
     * Method to get a program change and propagate it to the reference table
     *
     * @param id the id of the program
     * @param s the new status
     */
    public void setProgramStatus(String id, ReferenceTable.STATUS s) {
        if (references.setProgramStatus(id, s)) {
            changeStatus();
        }
    }

    /**
     * method to handle a change in the reference table status
     */
    private void changeStatus() {
        switch (references.getStatus()) {
            case INVALID:
            case MISSING:
            case UNSTABLE:
            case UNKNOWN:
                setInvalid();
                break;
            case OK:
                setValid();
                break;
        }

    }

    /**
     * Updates a node status in the active nodes set
     * @param nodeId
     * @param status
     * @throws JSONException
     */
    public void setActiveNode(String nodeId, boolean status) throws SpokExecutionException {
        try {
            this.activeNodes.put(nodeId, status);
        } catch (Exception e) {
            throw new SpokExecutionException("Unable to update the active nodes set");
        }

    }

    /**
     * Increments a given node counter
     * @param nodeId
     * @throws JSONException
     */
    public void incrementNodeCounter(String nodeId) throws SpokExecutionException {
        try {
            int counter = this.nodesCounter.has(nodeId)?Integer.parseInt((String) this.nodesCounter.get(nodeId))+1:1;
            this.nodesCounter.put(nodeId, String.valueOf(counter));
        } catch (Exception e) {
            throw new SpokExecutionException("Unable to increment the node counter for: " + nodeId);
        }
    }

    /**
     * Returns the object representing the active nodes of the program
     * @return
     */
    public JSONObject getActiveNodes() {
        return this.activeNodes;
    }

    /**
     * Returns the object representing the node counters of the program
     * @return
     */
    public JSONObject getNodesCounter() {
        return this.nodesCounter;
    }
}
