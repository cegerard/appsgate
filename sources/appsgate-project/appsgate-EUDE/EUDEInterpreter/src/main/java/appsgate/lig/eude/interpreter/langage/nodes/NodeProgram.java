package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
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
public class NodeProgram extends Node {

    public Collection<NodeProgram> getSubPrograms() {
        if (subPrograms != null) {
            return subPrograms.values();
        }
        return new HashSet<NodeProgram>();
    }

    /**
     * Program running state static enumeration
     *
     * @author Cédric Gérard
     * @since September 13, 2013
     */
    public static enum RUNNING_STATE {

        DEPLOYED("DEPLOYED"), STARTED("STARTED"), FAILED("FAILED"),
        STOPPED("STOPPED"), PAUSED("PAUSED");

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
     * Use for simplify user interface reverse compute
     */
    private String userSource;

    /**
     * Sequence of rules to interpret
     */
    private Node body;

    private HashMap<String, NodeProgram> subPrograms;

    /**
     * The current running state of this program - DEPLOYED - STARTED - STOPPED
     * - PAUSED - FAILED
     */
    private RUNNING_STATE runningState = RUNNING_STATE.DEPLOYED;

    /**
     * Pointer to the interpreter, could be the interpreter for the simulator
     */
    private EUDEInterpreter mediator = null;

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
    }

    /**
     * Initialize the program from a JSON object
     *
     * @param mediator
     * @param programJSON Abstract tree of the program in JSON
     * @param p the node parent
     * @throws SpokNodeException
     */
    public NodeProgram(EUDEInterpreter mediator, JSONObject programJSON, Node p)
            throws SpokException {
        this(mediator, p);

        // initialize the program with the JSON
        id = getJSONString(programJSON, "id");
        if (programJSON.has("runningState")) {
            runningState = RUNNING_STATE.valueOf(getJSONString(programJSON, "runningState"));
        }

        update(programJSON);
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
     * @throws SpokNodeException
     */
    public final boolean update(JSONObject json) throws SpokException {

        //  this.programJSON = json;
        userSource = json.optString("userSource");

        name = getJSONString(json, "name");
        header = getJSONObject(json, "header");

        this.setSymbolTable(new SymbolTable(json.optJSONArray("definitions"), this));
        body = Builder.nodeOrNull(getJSONObject(json, "body"), this);

        return true;

    }

    public boolean pause() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Launch the interpretation of the rules
     *
     * @return integer
     */
    @Override
    public JSONObject call() {
        JSONObject ret = new JSONObject();
        if (runningState != RUNNING_STATE.PAUSED) {
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
        } else {
            // TODO restart from previous state
            // synchronized(pauseMutex) {
            // pauseMutex.notify();
            // return 1;
            // }
        }
        return null;
    }

    @Override
    public void stop() {
        if (runningState == RUNNING_STATE.STARTED && !isStopping()) {
            LOGGER.debug("Stoping program {}", this);
            setStopping(true);
            body.stop();
            body.removeEndEventListener(this);
            setRunningState(RUNNING_STATE.STOPPED);
            fireEndEvent(new EndEvent(this));
            setStopping(false);
        } else {
            LOGGER.warn("Trying to stop {}, while being at state {}", this, this.runningState);
        }
    }

    @Override
    protected void specificStop() {
    }

    /**
     * Set the current running state to deployed
     */
    public void setDeployed() {
        setRunningState(RUNNING_STATE.DEPLOYED);
    }

    @Override
    public void startEventFired(StartEvent e) {
        LOGGER.debug("The start event ({}) has been catched by {}", e.getSource(), this);
        setRunningState(RUNNING_STATE.STARTED);
    }

    @Override
    public void endEventFired(EndEvent e) {
        setRunningState(RUNNING_STATE.STOPPED);
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
     * @return the user input source
     */
    public String getUserSource() {
        if (userSource.isEmpty()) {
            return getExpertProgramScript();

        } else {
            return userSource;

        }
    }

    /**
     * @return the running state of the program
     */
    public RUNNING_STATE getRunningState() {
        return runningState;
    }

    /**
     * @return the JSON source of the program
     * @throws SpokNodeException if there is no source for the program
     */
    public JSONObject getJSONSource() throws SpokNodeException {
        return body.getJSONDescription();
    }

    /**
     *
     * @param runningState
     */
    private void setRunningState(RUNNING_STATE runningState) {
        this.runningState = runningState;
        getMediator().notifyChanges(new ProgramStateNotificationMsg(id, "runningState", this.runningState.toString()));
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

            o.put("userSource", getUserSource());
            o.put("body", body.getJSONDescription());

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
        return this.getHeader() + vars.getExpertProgramDecl() + "\n" + body.getExpertProgramScript();
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

        ret.userSource = getUserSource();
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
}
