package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.dependency.graph.ProgramGraph;
import appsgate.lig.context.dependency.graph.Reference.STATUS;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ErrorMessagesFactory;
import appsgate.lig.eude.interpreter.langage.components.NodeCounter;
import appsgate.lig.eude.interpreter.references.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import appsgate.lig.eude.interpreter.spec.ProgramDesc;
import appsgate.lig.eude.interpreter.spec.ProgramStateNotification;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import org.json.JSONArray;
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
final public class NodeProgram extends Node implements ProgramDesc, ProgramGraph {

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
    private JSONObject bodyJSON = null;

    /**
     * Sequence of rules to interpret
     */
    private Node body;
    /**
     * The json program
     */
    private JSONObject rulesJSON = null;
    /**
     * The json program
     */
    private JSONObject actionsJSON = null;

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
    private NodeCounter nodesCounter;

    /**
     * The current running state of this program - DEPLOYED - INVALID -
     * INCOMPLETE LIMPING - PROCESSING
     */
    private PROGRAM_STATE state = PROGRAM_STATE.DEPLOYED;

    /**
     * Pointer to the interpreter, could be the interpreter for the simulator
     */
    private EUDEInterpreter mediator = null;

    /**
     * If the program is invalid or incomplete, it contains a message saying why
     */
    private JSONObject errorMessage = null;

    /**
     * the table containing all the references
     */
    private ReferenceTable references = null;

    /**
     * Boolean to know if the program is syntaxically correct or not, used to
     * avoid the update of the program state
     */
    private boolean isSyntaxicallyCorrect = true;

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
        activeNodes = new JSONObject();
        nodesCounter = new NodeCounter();
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
        LOGGER.trace("NodeProgram(EUDEInterpreter mediator, JSONObject o, Node p), JSON object : " + o.toString());

        if (!o.has("body")) {
            LOGGER.error("this program has no body");
            errorMessage = ErrorMessagesFactory.getEmptyProgramMessage();
            setInvalid();
            return;
        }
        this.bodyJSON = o.optJSONObject("body");
        this.rulesJSON = o.optJSONObject("rules");
        // initialize the program with the JSON
        try {

            id = getJSONString(o, "id");
            update(o);
        } catch (SpokNodeException ex) {
            LOGGER.warn("Program node triggered an exception during constructor : " + ex.getMessage());
            errorMessage = ErrorMessagesFactory.getMessageFromSpokNodeException(ex);
            setInvalid();
        }
    }

    @Override
    public EUDEInterpreter getMediator() throws SpokExecutionException {
        if (this.mediator == null) {
            throw new SpokExecutionException("Mediator not found");
        }
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
            this.bodyJSON = json.optJSONObject("body");
            this.rulesJSON = json.optJSONObject("rules");
            this.actionsJSON = json.optJSONObject("actions");
            if (json.has("runningState")) {
                this.state = PROGRAM_STATE.valueOf(json.optString("runningState"));
            }

            if (this.bodyJSON != null) {
                body = Builder.nodeOrNull(getJSONObject(json, "body"), this);
            } else {
                JSONArray rules = new JSONArray();
                rules.put(this.rulesJSON);
                rules.put(this.actionsJSON);
                JSONObject o = new JSONObject();
                try {
                    o.put("type", "SetOfRules");
                    o.put("rules", rules);
                } catch (JSONException ex) {
                }
                body = Builder.nodeOrNull(o, this);
            }
            this.isSyntaxicallyCorrect = true;
            return this.buildReferences();
        } catch (SpokNodeException ex) {
            LOGGER.error("Unable to parse a specific node: {}", ex.getMessage());
            this.isSyntaxicallyCorrect = false;
            errorMessage = ErrorMessagesFactory.getMessageFromSpokNodeException(ex);

        } catch (SpokTypeException ex) {
            LOGGER.error("Problem of type: {}", ex.getMessage());
            this.isSyntaxicallyCorrect = false;
            errorMessage = ErrorMessagesFactory.getMessageFromInvalidType(ex);
        } catch (SpokException ex) {
            LOGGER.error("Problem: {}", ex.getMessage());
            this.isSyntaxicallyCorrect = false;
            errorMessage = ErrorMessagesFactory.getMessageFromSpokException(ex);
        }
        setInvalid();
        return false;

    }

    /**
     *
     */
    private Boolean buildReferences() {
        if (this.body == null) {
            LOGGER.trace("buildReferences(), body is null, program should not be valid");
            return false;
        }
        this.references = new ReferenceTable(mediator, this.id);
        if (this.body != null) {
            this.body.buildReferences(this.references, null);
        }
        STATUS newStatus = this.references.checkReferences();
        return applyStatus(newStatus);
    }

    @Override
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
        nodesCounter.reinit();
        if (canRun()) {
            setProcessing("0");
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
            LOGGER.warn("Trying to start {} while: {}", this, state.toString());
            ret.put("status", false);
        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return ret;
    }

    @Override
    public void stop() {
    	LOGGER.trace("stop()");
        if (!isValid()) {
            LOGGER.warn("Trying to stop {}, but this program is invalid", this);
            return;
        }

        if (!isRunning()) {
            LOGGER.warn("Trying to stop {}, but this program is not currently running.", this);
            return;
        }
        if (!isStopping()) {
            LOGGER.debug("Stopping program {}", this);
            setStopping(true);
            body.stop();
            body.removeEndEventListener(this);
            setStopped();
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
    final public void setStopped() {
    	LOGGER.trace("setStopped(), current state : "+state);

        switch (state) {
            case INVALID:
                LOGGER.warn("Trying to stop {}, while being invalid", this);
                break;
            case DEPLOYED:
                LOGGER.warn("Trying to stop {}, while being already stopped", this);
                break;
            case INCOMPLETE:
                LOGGER.warn("Trying to stop {}, while being already stopped (incomplete)", this);
                break;
            case PROCESSING:
                LOGGER.debug("Stopping {}", this);
                setState(PROGRAM_STATE.DEPLOYED, null);
                break;
            case LIMPING:
                LOGGER.debug("Stopping {} (incomplete)", this);
                setState(PROGRAM_STATE.INCOMPLETE, null);
                break;
            default:
                throw new AssertionError(state.name());

        }
    }

    /**
     * set the state of the program to invalid
     */
    private void setInvalid() {
        if (isRunning()) {
            this.stop();
        }
        setState(PROGRAM_STATE.INVALID, null);
    }

    /**
     * set the state to a valid state
     */
    private void setValid() {
        if (state == PROGRAM_STATE.INVALID || state == PROGRAM_STATE.INCOMPLETE) {
            setState(PROGRAM_STATE.DEPLOYED, id);
        }
        if (state == PROGRAM_STATE.LIMPING) {
            setState(PROGRAM_STATE.PROCESSING, id);
        }
    }

    private void setIncomplete() {
        if (state == PROGRAM_STATE.INVALID || state == PROGRAM_STATE.DEPLOYED) {
            setState(PROGRAM_STATE.INCOMPLETE, id);
        }
        if (state == PROGRAM_STATE.PROCESSING) {
            setState(PROGRAM_STATE.LIMPING, id);
        }
    }

    /**
     * @return true if the program can be run, false otherwise
     */
    final public boolean canRun() {
        return this.state == PROGRAM_STATE.DEPLOYED || this.state == PROGRAM_STATE.INCOMPLETE;
    }

    /**
     * @return true if the program can be stopped, false otherwise
     */
    final public boolean isRunning() {
        return (this.state == PROGRAM_STATE.PROCESSING || this.state == PROGRAM_STATE.LIMPING);
    }

    /**
     * @return the current state of the program
     */
    @Override
    final public PROGRAM_STATE getState() {
        return state;
    }

    /**
     * @return true if the program is valid
     */
    final public boolean isValid() {
        return this.state != PROGRAM_STATE.INVALID;
    }

    /**
     * set the state to processing if the program is valid
     *
     * @param iid
     */
    public void setProcessing(String iid) {
        switch (state) {
            case INVALID:
                LOGGER.warn("Unable to start {}, cause program is invalid", this);
                break;
            case DEPLOYED:
                LOGGER.trace("Program {} started", this);
                setState(PROGRAM_STATE.PROCESSING, iid);
                break;
            case PROCESSING:
                LOGGER.warn("Unable to start {}, cause program is already running", this);
                break;
            case INCOMPLETE:
                LOGGER.trace("Program {} started (partially)", this);
                setState(PROGRAM_STATE.LIMPING, iid);
                break;
            case LIMPING:
                LOGGER.warn("Unable to start {}, cause program is already running (partially)", this);
                break;
            default:
                throw new AssertionError(state.name());
        }
    }

    @Override
    public void startEventFired(StartEvent e) {
        LOGGER.debug("The start event ({}) has been catched by {}", e.getSource(), this);
    }

    @Override
    public void endEventFired(EndEvent e) {
        setStopped();
        fireEndEvent(new EndEvent(this));
    }

    /**
     *
     * @return the id
     */
    @Override
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
    public JSONObject getJSONBody() {
        return bodyJSON;
    }

    /**
     * @return the JSON source of the rules
     */
    public JSONObject getJSONRules() {
        return rulesJSON;
    }

    /**
     * @return the JSON source of the actions
     */
    public JSONObject getJSONActions() {
        return actionsJSON;
    }

    /**
     * @param runningState
     */
    private void setState(PROGRAM_STATE runningState, String iid) {

        if (runningState != this.state) {
            try {
                this.state = runningState;
                getMediator().newProgramStatus(this.getId(), ReferenceTable.getProgramStatus(runningState));
                getMediator().notifyChanges(new ProgramStateNotification(id, this.state.toString(), name, iid));
            } catch (SpokExecutionException ex) {
                LOGGER.warn("Problem while updating state of the node: {}", ex.getMessage());
            }
        }
    }

    @Override
    public String getTypeSpec() {
        return "Program : " + name;
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("type", "program");
            o.put("runningState", state.toString());
            o.put("name", name);
            o.put("header", header);
            o.put("package", getPath());

            o.put("body", getJSONBody());
            o.put("rules", getJSONRules());
            o.put("actions", getJSONActions());

            o.put("activeNodes", activeNodes);
            o.put("nodesCounter", nodesCounter.getJSONDescription());

            o.put("definitions", getSymbolTableDescription());

            o.put("errorMessage", this.errorMessage);

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
        NodeProgram ret;
        try {
            ret = new NodeProgram(getMediator(), parent);
        } catch (SpokExecutionException ex) {
            LOGGER.error("Unable to make a copy: {}", ex.getMessage());
            return null;
        }
        ret.id = id;
        ret.state = state;
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
    public void setDeviceStatus(String id, STATUS s) {
        references.setDeviceStatus(id, s);
        changeStatus();
    }

    /**
     * Method to get a program change and propagate it to the reference table
     *
     * @param id the id of the program
     * @param s the new status
     */
    public void setProgramStatus(String id, STATUS s) {
    	LOGGER.trace("setProgramStatus(String id : {}, STATUS s : {})", id, s);

        // No need to update the status and the reference table if this is the same program
        if (!id.equalsIgnoreCase(this.id)) {
            if (references.setProgramStatus(id, s)) {
                changeStatus();
            }
        }
    }

    /**
     * method to handle a change in the reference table status
     */
    private void changeStatus() {
        applyStatus(references.computeStatus());
    }

    /**
     *
     * @param s
     * @return
     */
    private Boolean applyStatus(STATUS s) {
        errorMessage = references.getErrorMessage();
        // Don't change the status if the program is not syntaxically correct
        if (this.isSyntaxicallyCorrect) {
            switch (s) {
                case INVALID:
                    setInvalid();
                    break;
                case MISSING:
                case UNSTABLE:
                case UNKNOWN:
                    setIncomplete();
                    break;
                case OK:
                    setValid();
                    break;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates a node status in the active nodes set
     *
     * @param nodeId
     * @param status
     * @throws SpokExecutionException
     */
    public void setActiveNode(String nodeId, boolean status) throws SpokExecutionException {
        if (nodeId == null) {
            return;
        }
        try {
            this.activeNodes.put(nodeId, status);
        } catch (JSONException e) {
            throw new SpokExecutionException("Unable to update the active nodes set");
        }

    }

    /**
     * Increments a given node counter
     *
     * @param nodeId
     * @throws SpokExecutionException
     */
    public void incrementNodeCounter(String nodeId) throws SpokExecutionException {
        if (nodeId == null) {
            return;
        }
        nodesCounter.incrementNodeCounter(nodeId, this.getMediator().getTime());
    }

    /**
     * Returns the object representing the active nodes of the program
     *
     * @return
     */
    public JSONObject getActiveNodes() {
        return this.activeNodes;
    }

    /**
     * Returns the object representing the node counters of the program
     *
     * @return
     */
    public JSONObject getNodesCounter() {
        return this.nodesCounter.getJSONDescription();
    }

    @Override
    public String getStateName() {
        return getState().name();
    }
}
