package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import java.util.logging.Level;
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
     * Attribute use to detect error like infinite loop and kill them
     */
    private int state = 0;

    /**
     * Default constructor
     *
     * @constructor
     * @param interpreter the interpreter that execute this program
     */
    public NodeProgram(EUDEInterpreterImpl interpreter) {
        super(interpreter);
    }

    /**
     * Initialize the program from a JSON object
     *
     * @param interpreter
     * @param programJSON Abstract tree of the program in JSON
     * @throws appsgate.lig.eude.interpreter.langage.nodes.NodeException
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
     * @throws appsgate.lig.eude.interpreter.langage.nodes.NodeException
     */
    public final boolean update(JSONObject jsonProgram) throws NodeException {

        this.programJSON = jsonProgram;
        userInputSource = getJSONString(programJSON, "userInputSource");

        JSONObject source = getJSONObject(jsonProgram, "source");
        name = getJSONString(source, "programName");
        author = getJSONString(source, "author");
        target = getJSONString(source, "target");
        if (source.has("daemon")) {
            try {
                daemon = source.getBoolean("daemon");
            } catch (JSONException ex) {
                throw new NodeException("NodeProgram", "daemon", ex);
            }
        } else {
            daemon = false;
        }
        seqRules = new NodeSeqRules(interpreter, getJSONArray(source, "seqRules"));

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

//            if (seqRulesThread != null) {
//                return 1;
//            } else {
//                setRunningState(RUNNING_STATE.FAILED);
//            }
        } else {
            // TODO restart from previous state
            // synchronized(pauseMutex) {
            // pauseMutex.notify();
            // return 1;
            // }
        }
        return -1;
    }

    /**
     * Restart daemon program after their previous termination
     */
    private int daemonCall() {
        state--;
        seqRules.addStartEventListener(this);
        seqRules.call();
        return 1;
//        seqRulesThread = pool.submit(seqRules);

//        if (seqRulesThread != null) {
//            return 1;
//        } else {
//            setRunningState(RUNNING_STATE.FAILED);
//            return -1;
//        }
    }

    @Override
    public void stop() {
        if (runningState == RUNNING_STATE.STARTED) {
            seqRules.stop();
            seqRules.removeEndEventListener(this);
            setRunningState(RUNNING_STATE.STOPPED);
            fireEndEvent(new EndEvent(this));
        } else {
            LOGGER.warn("Trying to stop program {}, while being at state {}", this.name, this.runningState);
        }
    }

    /**
     * Set the current running state to deployed
     */
    public void setDeployed() {
        setRunningState(RUNNING_STATE.DEPLOYED);
    }

    @Override
    public void startEventFired(StartEvent e) {
        setRunningState(RUNNING_STATE.STARTED);
        seqRules.removeStartEventListener(this);
    }

    @Override
    public void endEventFired(EndEvent e) {
        if (isDaemon()) {
            if (daemonCall() == -1) {
                seqRules.removeEndEventListener(this);
                fireEndEvent(new EndEvent(this));
            }
        } else {
            setRunningState(RUNNING_STATE.STOPPED);
            seqRules.removeEndEventListener(this);
            fireEndEvent(new EndEvent(this));
        }
    }

    /**
     * Accessors
     */
    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setDaemon(Boolean d) {
        this.daemon = d;
    }

    public boolean isDaemon() {
        return this.daemon;
    }

    public String getUserInputSource() {
        return userInputSource;
    }

    public JSONObject getProgramJSON() {
        return programJSON;
    }

    public RUNNING_STATE getRunningState() {
        return runningState;
    }

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
    public void setRunningState(RUNNING_STATE runningState) {
        try {
            programJSON.put("runningState", runningState.toString());
            this.runningState = runningState;
            interpreter.notifyChanges(new ProgramStateNotificationMsg(id, "runningState", this.runningState.toString()));

            if (runningState.equals(RUNNING_STATE.STARTED)) {
                state++;
            } else if (runningState.equals(RUNNING_STATE.STOPPED)) {
                state--;
            }

            //This program seem to be in an infinite loop
            if (state > 1) {
                //stop it
                stop();
                //notify client that a program has been killed for security
                interpreter.notifyChanges(new ProgramStateNotificationMsg(id, "ERROR", "INFINITE_LOOP_KILLED"));
                //change the state of the program to failed start
                setRunningState(RUNNING_STATE.FAILED);
            }

        } catch (JSONException e) {
            LOGGER.warn("JSON Exception : {}", e.getMessage());
        }
    }

}
