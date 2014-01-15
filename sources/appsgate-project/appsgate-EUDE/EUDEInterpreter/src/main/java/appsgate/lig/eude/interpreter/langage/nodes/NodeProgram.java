package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node program for the interpreter. Contains the metadatas of the program, the
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
    private NodeSeqRules seqRules;

    /**
     * JSON representation of the program
     */
    private JSONObject programJSON;

    /**
     * The current running state of this program - DEPLOYED - STARTED - STOPPED
     * - PAUSED - FAILED
     */
    private RUNNING_STATE runningState = RUNNING_STATE.DEPLOYED;

    /**
     * Default constructor
     *
     * @constructor
     * @param interpreter the interpreter that execute this program
     */
    public NodeProgram(EUDEInterpreterImpl interpreter) {
        super(interpreter, null);
    }

    /**
     * Initialize the program from a JSON object
     *
     * @param interpreter
     * @param programJSON Abstract tree of the program in JSON
     * @throws NodeException
     */
    public NodeProgram(EUDEInterpreterImpl interpreter, JSONObject programJSON)
            throws NodeException {
        this(interpreter);

        // initialize the program with the JSON
        id = getJSONString(programJSON, "id");
        runningState = RUNNING_STATE.valueOf(getJSONString(programJSON, "runningState"));
        update(programJSON);
    }

    /**
     * Update the current program source code Program need to be stopped.
     *
     * @param jsonProgram the new source code
     *
     * @return true if the source code has been updated, false otherwise
     * @throws NodeException
     */
    public final boolean update(JSONObject jsonProgram) throws NodeException {

        this.programJSON = jsonProgram;
        userInputSource = getJSONString(programJSON, "userInputSource");

        JSONObject source = getJSONObject(jsonProgram, "source");
        name = getJSONString(source, "programName");
//        author = getJSONString(source, "author");
//        target = getJSONString(source, "target");

        this.setSymbolTable(new SymbolTable(getJSONArray(source, "seqDefinitions")));
        if (source.has("daemon")) {
            try {
                daemon = source.getBoolean("daemon");
            } catch (JSONException ex) {
                throw new NodeException("NodeProgram", "daemon", ex);
            }
        } else {
            daemon = false;
        }
        seqRules = new NodeSeqRules(getInterpreter(), getJSONArray(source, "seqRules"), this);

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
    public Integer call() {
        if (runningState != RUNNING_STATE.PAUSED) {
            fireStartEvent(new StartEvent(this));
            seqRules.addStartEventListener(this);
            seqRules.addEndEventListener(this);
            // seqRulesThread = pool.submit(seqRules);
            seqRules.call();
            return 1;
        } else {
            // TODO restart from previous state
            // synchronized(pauseMutex) {
            // pauseMutex.notify();
            // return 1;
            // }
        }
        return -1;
    }

    @Override
    public void stop() {
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
     * @throws NodeException if there is no source for the program
     */
    public JSONObject getJSONSource() throws NodeException {
        try {
            return programJSON.getJSONObject("source");
        } catch (JSONException ex) {
            throw new NodeException("NodeProgram", "source", ex);
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
            LOGGER.warn("JSON Exception : {}, unable to set the running state inside the JSON program", e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "[Node Program : " + name + "]";
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
    Node copy(Node parent) {
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
        } catch (NodeException ex) {
            LOGGER.error("Unable to copy the program", ex);
        }
        return ret;
    }

}
