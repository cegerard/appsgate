package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node program for the interpreter. Contains the metadata of the program, the
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
     * Program's name given by the user
     */
    private String name;

    /**
     * User's name who wrote the program
     */
    private String author;

    /**
     * Target user
     */
    private String target;

    /**
     * Daemon attribute
     */
    private Boolean daemon;

    /**
     * Use for simplify user interface reverse compute
     */
    private String userInputSource;

    /**
     * Sequence of rules to interpret
     */
    private Node seqRules;

    /**
     * JSON representation of the program
     */
    private JSONObject programJSON;

    /**
     * The current running state of this program - DEPLOYED - STARTED - STOPPED
     * - PAUSED - FAILED
     */
    private RUNNING_STATE runningState = RUNNING_STATE.DEPLOYED;

    private EUDEInterpreterImpl interpreter = null;

    /**
     * Default constructor
     *
     * @param i
     * @constructor
     */
    public NodeProgram(EUDEInterpreterImpl i) {
        super(null);
        this.interpreter = i;
    }

    /**
     * Initialize the program from a JSON object
     *
     * @param interpreter
     * @param programJSON Abstract tree of the program in JSON
     * @throws SpokNodeException
     */
    public NodeProgram(EUDEInterpreterImpl interpreter, JSONObject programJSON)
            throws SpokException {
        this(interpreter);

        // initialize the program with the JSON
        id = getJSONString(programJSON, "id");
        runningState = RUNNING_STATE.valueOf(getJSONString(programJSON, "runningState"));
        update(programJSON);
    }

    @Override
    public EUDEInterpreterImpl getInterpreter() {
        return this.interpreter;
    }

    /**
     * Update the current program source code Program need to be stopped.
     *
     * @param jsonProgram the new source code
     *
     * @return true if the source code has been updated, false otherwise
     * @throws SpokNodeException
     */
    public final boolean update(JSONObject jsonProgram) throws SpokException {

        this.programJSON = jsonProgram;
        userInputSource = getJSONString(programJSON, "userInputSource");

        JSONObject source = getJSONObject(jsonProgram, "source");
        name = getJSONString(source, "programName");
//        author = getJSONString(source, "author");
//        target = getJSONString(source, "target");

        this.setSymbolTable(new SymbolTable(source.optJSONArray("seqDefinitions")));
        if (source.has("daemon")) {
            daemon = source.optBoolean("daemon");
        } else {
            daemon = false;
        }
        seqRules = NodeBuilder.BuildNodeFromJSON(getJSONObject(source, "seqRules"), this);

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
            seqRules.addStartEventListener(this);
            seqRules.addEndEventListener(this);
            // seqRulesThread = pool.submit(seqRules);
            seqRules.call();
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
    public void stop() throws SpokException {
        if (runningState == RUNNING_STATE.STARTED && !isStopping()) {
            LOGGER.debug("Stoping program {}", this);
            setStopping(true);
            seqRules.stop();
            seqRules.removeEndEventListener(this);
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
        if (isDaemon()) {
            LOGGER.debug("The end event ({}) has been fired on a daemon, program is still running", e.getSource());
            seqRules.addEndEventListener(this);
            seqRules.call();
            LOGGER.debug("Call rearmed");
        } else {
            setRunningState(RUNNING_STATE.STOPPED);
            fireEndEvent(new EndEvent(this));
        }
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     *
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return true if the Program is a daemon
     */
    public boolean isDaemon() {
        return this.daemon;
    }

    /**
     * @return the user input source
     */
    public String getUserInputSource() {
        return userInputSource;
    }

    /**
     * @return the JSONObjecto containing the program
     */
    public JSONObject getProgramJSON() {
        return programJSON;
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
        try {
            return programJSON.getJSONObject("source");
        } catch (JSONException ex) {
            throw new SpokNodeException("NodeProgram", "source", ex);
        }
    }

    /**
     *
     * @param runningState
     */
    private void setRunningState(RUNNING_STATE runningState) {
        try {
            programJSON.put("runningState", runningState.toString());
            this.runningState = runningState;
            getInterpreter().notifyChanges(new ProgramStateNotificationMsg(id, "runningState", this.runningState.toString()));

        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
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
            o.put("runningState", runningState.name);

            JSONObject source = new JSONObject();
            source.put("seqRules", seqRules.getJSONDescription());
            source.put("daemon", daemon);
            source.put("programName", name);
            source.put("userInputSource", userInputSource);
            source.put("seqDefinitions", getSymbolTable().getJSONDescription());

            o.put("source", source);
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
        SymbolTable vars = new SymbolTable();
        seqRules.collectVariables(vars);
        this.setSymbolTable(vars);
        return this.getHeader() + vars.getExpertProgramDecl() + "\n" + seqRules.getExpertProgramScript();
    }

    /**
     * @return the header of a program
     */
    private String getHeader() {
        String ret = "";
        ret += "Author: " + this.author + "\n";
        ret += "Target:" + this.target + "\n";
        return ret;
    }

    @Override
    protected void collectVariables(SymbolTable s) {
        seqRules.collectVariables(s);
    }

    @Override
    protected Node copy(Node parent) {
        NodeProgram ret = new NodeProgram(getInterpreter());
        ret.author = author;
        ret.daemon = daemon;
        ret.id = id;
        ret.name = name;
        ret.runningState = runningState;
        ret.target = target;
        ret.userInputSource = userInputSource;
        ret.programJSON = new JSONObject(programJSON);
        try {
            boolean update = ret.update(programJSON);
        } catch (SpokException ex) {
            LOGGER.error("Unable to copy the program", ex);
            return null;
        }
        return ret;
    }

}
