package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.impl.EUDEMediator;
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
     * Daemon attribute
     */
    private Boolean daemon;

    /**
     * Use for simplify user interface reverse compute
     */
    private String userSource;

    /**
     * Sequence of rules to interpret
     */
    private Node body;

    /**
     * JSON representation of the program
     */
    private JSONObject programJSON;

    /**
     * The current running state of this program - DEPLOYED - STARTED - STOPPED
     * - PAUSED - FAILED
     */
    private RUNNING_STATE runningState = RUNNING_STATE.DEPLOYED;

    private EUDEMediator mediator = null;

    /**
     * Default constructor
     *
     * @param i
     * @constructor
     */
    public NodeProgram(EUDEMediator i) {
        super(null);
        this.mediator = i;
    }

    /**
     * Initialize the program from a JSON object
     *
     * @param mediator
     * @param programJSON Abstract tree of the program in JSON
     * @throws SpokNodeException
     */
    public NodeProgram(EUDEMediator mediator, JSONObject programJSON)
            throws SpokException {
        this(mediator);

        // initialize the program with the JSON
        id = programJSON.optString("id");
        if (programJSON.has("runningState")) {
            runningState = RUNNING_STATE.valueOf(getJSONString(programJSON, "runningState"));
        }

        update(programJSON);
    }

    @Override
    public EUDEMediator getMediator() {
        return this.mediator;
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
        userSource = programJSON.optString("userSource");

        name = getJSONString(jsonProgram, "name");
        author = jsonProgram.optString("author");

        this.setSymbolTable(new SymbolTable(jsonProgram.optJSONArray("definitions")));
        if (jsonProgram.has("daemon")) {
            daemon = jsonProgram.optBoolean("daemon");
        } else {
            daemon = false;
        }
        body = Builder.nodeOrNull(getJSONObject(jsonProgram, "body"), this);

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
        if (isDaemon()) {
            LOGGER.debug("The end event ({}) has been fired on a daemon, program is still running", e.getSource());
            body.addEndEventListener(this);
            body.call();
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
     * @return true if the Program is a daemon
     */
    public boolean isDaemon() {
        return this.daemon;
    }

    /**
     * @return the user input source
     */
    public String getUserSource() {
        return userSource;
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
            getMediator().notifyChanges(new ProgramStateNotificationMsg(id, "runningState", this.runningState.toString()));

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
            o.put("type", "program");
            o.put("runningState", runningState.name);
            o.put("name", name);
            o.put("daemon", daemon);
            o.put("author", author);
            o.put("package", getPath());

            o.put("userSource", userSource);
            o.put("body", body.getJSONDescription());
            o.put("definitions", getSymbolTable().getJSONDescription());

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
        this.setSymbolTable(vars);
        return this.getHeader() + vars.getExpertProgramDecl() + "\n" + body.getExpertProgramScript();
    }

    /**
     * @return the header of a program
     */
    private String getHeader() {
        String ret = "";
        ret += "Author: " + this.author + "\n";
        return ret;
    }

    @Override
    protected Node copy(Node parent) {
        NodeProgram ret = new NodeProgram(getMediator());
        ret.id = id;
        ret.runningState = runningState;
        ret.name = name;
        ret.daemon = daemon;
        ret.author = author;

        ret.userSource = userSource;

        ret.programJSON = new JSONObject(programJSON);
        try {
            ret.update(programJSON);
        } catch (SpokException ex) {
            LOGGER.error("Unable to copy the program", ex);
            return null;
        }
        return ret;
    }

    private String getPath() {
        NodeProgram p = (NodeProgram) this.getParent();
        if (p == null) {
            return name;
        } else {
            return p.getPath() + "." + name;
        }
    }

}
