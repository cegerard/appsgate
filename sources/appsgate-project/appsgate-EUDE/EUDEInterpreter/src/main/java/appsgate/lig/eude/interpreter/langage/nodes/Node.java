package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import java.util.concurrent.Callable;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.EndEventGenerator;
import appsgate.lig.eude.interpreter.langage.components.EndEventListener;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEventGenerator;
import appsgate.lig.eude.interpreter.langage.components.StartEventListener;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.components.SpokParser;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import appsgate.lig.eude.interpreter.spec.ProgramCommandNotification;
import appsgate.lig.eude.interpreter.spec.ProgramLineNotification;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for all the nodes of the interpreter
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 * @author JR Courtois
 *
 * @since May 22, 2013
 * @version 1.0.0
 *
 */
public abstract class Node implements Callable<JSONObject>, StartEventGenerator,
        StartEventListener, EndEventGenerator, EndEventListener, SpokObject {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);

    /**
     * List of the listeners that listen to the StartEvent of the node
     */
    private final ConcurrentLinkedQueue<StartEventListener> startEventListeners = new ConcurrentLinkedQueue<StartEventListener>();

    /**
     * List of the listeners that listen to the EndEvent of the node
     */
    private final ConcurrentLinkedQueue<EndEventListener> endEventListeners = new ConcurrentLinkedQueue<EndEventListener>();

    /**
     * Symbol table of the node containing the local symbols
     */
    private SymbolTable symbolTable;

    /**
     * Node parent in the abstract tree of a program
     */
    private Node parent;

    /**
     * Use to stop node but atomically
     */
    private boolean stopping = false;

    /**
     * use to know when a node node is execute
     */
    private boolean started = false;

    /**
     * the iid available for the editor
     */
    private String iid = null;
    /**
     * the phrase available for the editor
     */
    private String phrase = null;
    
    /**
     * 
     */
    protected Boolean stopped = false;

    /**
     * Default constructor
     *
     * @param p the parent node
     */
    public Node(Node p) {
        this.parent = p;
    }

    /**
     *
     * @param p the parent node
     * @param o the json object
     */
    public Node(Node p, JSONObject o) {
        this(p);
        this.iid = o.optString("iid");
        this.phrase = o.optString("phrase");
    }

    /**
     * @param n the parent node
     */
    protected void setParent(Node n) {
        this.parent = n;
    }

    /**
     * @return the parent node
     */
    public Node getParent() {
        return parent;
    }

    /**
     * @return the iid
     */
    public final String getIID() {
        return this.iid;
    }

    /**
     * @param id the iid to set;
     */
    public final void setIID(String id) {
        this.iid = id;
    }
    /**
     * Stop the interpretation of the node. Check if the node is not started
     */
    public void stop() {
        stopped = true;
        if (isStarted()) {
            setStopping(true);

            specificStop();
            setStarted(false);
            fireEndEvent(new EndEvent(this));
            setStopping(false);
        } else {
            specificStop();
        }
    }

    /**
     * This method is called by the stop method
     */
    abstract protected void specificStop();

    @Override
    abstract public JSONObject call();

    @Override
    public void startEventFired(StartEvent e) {
        LOGGER.trace("The start event has been fired: " + e.toString());
    }

    /**
     * Fire a start event to all the listeners
     *
     * @param e The start event to fire for all the listeners
     */
    protected void fireStartEvent(StartEvent e) {
        int nbListeners = startEventListeners.size();
        LOGGER.trace("fire startEvent {} for {} nodes", e.getSource(), nbListeners);
        for (int i = 0; i < nbListeners; i++) {
            StartEventListener l = startEventListeners.poll();
            l.startEventFired(e);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("iid", this.getIID());
            o.put("phrase", this.phrase);
        } catch (JSONException ex) {
        }
        return o;
    }

    /**
     * Fire an end event to all the listeners
     *
     * @param e The end event to fire for all the listeners
     */
    protected synchronized void fireEndEvent(EndEvent e) {

        //during the execution the list can be updated
        int nbListeners = endEventListeners.size();
        LOGGER.trace("fire endEvent {} for {} nodes", e.getSource(), nbListeners);
        for (int i = 0; i < nbListeners; i++) {
            EndEventListener l = endEventListeners.poll();
            l.endEventFired(e);
        }
    }

    /**
     * Add a new listener to the start event of the node
     *
     * @param listener Listener to addAnonymousVariable
     */
    @Override
    public void addStartEventListener(StartEventListener listener) {
        LOGGER.trace("ADD:  {} listen StartEvent FROM {}", listener, this);
        startEventListeners.add(listener);
    }

    /**
     * Remove a listener to the start event of the node
     *
     * @param listener Listener to remove
     */
    @Override
    public void removeStartEventListener(StartEventListener listener) {
        LOGGER.trace("REM: {} stop listening startEvent FROM {}", listener, this);
        boolean goon = true;
        while (goon) {
            goon = startEventListeners.remove(listener);
        }
    }

    /**
     * Add a new listener to the end event of the node
     *
     * @param listener Listener to addAnonymousVariable
     */
    @Override
    public void addEndEventListener(EndEventListener listener) {
        LOGGER.trace("ADD:  {} listen EndEvent FROM {}", listener, this);
        if(endEventListeners.contains(listener)) {
            LOGGER.warn("{} is already listening to {}", listener, this);
            return;
        }
        endEventListeners.add(listener);
    }

    /**
     * Remove a listener to the end event of the node
     *
     * @param listener Listener to remove
     */
    @Override
    public void removeEndEventListener(EndEventListener listener) {
        LOGGER.trace("REM: {} stop listening endEvent FROM {}", listener, this);
        boolean goon = true;
        while (goon) {
            goon = endEventListeners.remove(listener);
        }

    }

    /**
     *
     * @return
     */
    public abstract String getExpertProgramScript();

    /**
     *
     * @return mediator
     * @throws SpokExecutionException
     */
    public EUDEInterpreter getMediator() throws SpokExecutionException {
        if (this.parent != null) {
            return this.parent.getMediator();
        }
        LOGGER.error("No parent found for this node: {}", this);
        throw new SpokExecutionException("No parent found for the node");
    }

    /**
     * @return true if the node is started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * @return true if the node is stopping
     */
    public boolean isStopping() {
        return stopping;
    }

    /**
     * Set the started boolean to b
     *
     * @param b
     */
    public void setStarted(Boolean b) {
        started = b;
        try {
            NodeProgram pNode = this.getProgramNode();
            pNode.setActiveNode(iid, b);
            if (started) {
                pNode.incrementNodeCounter(iid);
            }
            getMediator().notifyChanges(new ProgramLineNotification(pNode.getId(), pNode.getActiveNodes(), pNode.getNodesCounter()));
        } catch (SpokExecutionException ex) {
        }

    }

    /**
     * Set the stopping boolean to b
     *
     * @param b
     */
    public void setStopping(Boolean b) {
        stopping = b;
    }

    /**
     * Method that retrieve a string from a Json object
     *
     * @param jsonObj
     * @param jsonParam
     * @return the string corresponding to the jsonParam
     * @throws SpokNodeException if there is no such parameter in the JSON
     * Object
     */
    protected String getJSONString(JSONObject jsonObj, String jsonParam) throws SpokNodeException {
        try {
            return jsonObj.getString(jsonParam);
        } catch (JSONException ex) {
            LOGGER.debug("Unable to get string: {}", jsonParam);
            throw new SpokNodeException(this.getClass().getName(), jsonParam, ex);
        }
    }

    /**
     * Method that retrieve an array from a Json object
     *
     * @param jsonObj
     * @param jsonParam
     * @return the array corresponding to the jsonParam
     * @throws SpokNodeException if there is no such parameter in the JSON
     * Object
     */
    protected JSONArray getJSONArray(JSONObject jsonObj, String jsonParam) throws SpokNodeException {
        try {
            return jsonObj.getJSONArray(jsonParam);
        } catch (JSONException ex) {
            LOGGER.debug("Unable to get array: {}", jsonParam);
            throw new SpokNodeException(this.getClass().getName(), jsonParam, ex);
        }

    }

    /**
     * Method that retrieve an object from a Json object
     *
     * @param jsonObj
     * @param jsonParam
     * @return the object corresponding to the jsonParam
     * @throws SpokNodeException if there is no such parameter in the JSON
     * Object
     */
    protected JSONObject getJSONObject(JSONObject jsonObj, String jsonParam) throws SpokNodeException {
        try {
            return jsonObj.getJSONObject(jsonParam);
        } catch (JSONException ex) {
            LOGGER.debug("Unable to get object: {}", jsonParam);
            throw new SpokNodeException(this.getClass().getName(), jsonParam, ex);
        }

    }

    /**
     * Method that retrieve the device even if this has been dynamically built
     * through State
     *
     * @param o the json description of the node to build
     * @param common the name of the source/target
     * @return the corresponding node
     * @throws SpokNodeException if the node cannot be built
     */
    final protected Node getDevice(JSONObject o, String common) throws SpokNodeException {
        Node s;
        try {
            if (o.has("stateTarget")) {
                s = Builder.buildFromJSON(o.optJSONObject("stateTarget"), this);
            } else {
                s = Builder.buildFromJSON(getJSONObject(o, common), this);
            }
        } catch (SpokTypeException ex) {
            LOGGER.error("No {} found", common);
            throw new SpokNodeException(this.getClass().getSimpleName(), common, ex);
        }
        return s;
    }

    /**
     * Method to set the symbol table
     *
     * @param s the symbol table to set
     */
    protected void setSymbolTable(SymbolTable s) {
        this.symbolTable = s;
    }

    /**
     * Getter for the local symbol table
     *
     * @return the symbol table of the node that contains the symbols defined by
     * the node
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Method that return the SymbolTable description in json format
     *
     * @return
     */
    public JSONArray getSymbolTableDescription() {
        if (symbolTable != null) {
            return symbolTable.getJSONDescription();
        }
        return new JSONArray();
    }

    /**
     * Method to find a variable by its name
     *
     * it recursively parse the tree to find the name
     *
     * @param varName the name of the variable
     * @return The NodeVariableDefinition
     */
    protected NodeVariableDefinition getVariableByName(String varName) {
        if (this.getSymbolTable() != null) {
            NodeVariableDefinition element;
            element = this.getSymbolTable().getVariableByKey(varName);
            if (element != null) {
                return element;
            }
        }
        if (parent != null) {
            LOGGER.trace("looking for variable in the parent node");
            return parent.getVariableByName(varName);
        }
        LOGGER.warn("variable not found");
        return null;
    }

    /**
     * Method to find a function by its name
     *
     * it recursively parse the tree to find the name
     *
     * @param funcName
     * @return the node of the function definition
     */
    protected NodeFunctionDefinition getFunctionByName(String funcName) {
        if (this.symbolTable != null) {
            NodeFunctionDefinition element;
            element = this.symbolTable.getFunctionByKey(funcName);
            if (element != null) {
                return element;
            }
        }
        if (parent != null) {
            return parent.getFunctionByName(funcName);
        }
        return null;
    }

    /**
     * Method to copy a node and the rules behind
     *
     * @param parent
     * @return the node copied
     */
    abstract protected Node copy(Node parent);

    /**
     * Method that find a node of a given class in a tree This method is
     * recursive
     *
     * @param aClass the class of the node to find
     * @param parent the parent node to explore
     * @return the node when it is found and null if it is not found
     */
    protected Node findNode(Class aClass, Node parent) {
        if (parent == null) {
            return null;
        }
        if (parent.getClass() == aClass) {
            return parent;
        }
        return findNode(aClass, parent.parent);
    }

    /**
     * Method that add a variable in the Symbol Table and creates it if it does
     * not exist
     *
     * @param name the name of the variable
     * @param v the variable to assign
     */
    protected void setVariable(String name, SpokObject v) {
        if (symbolTable == null) {
            symbolTable = new SymbolTable(this);
        }
        symbolTable.addVariable(name, v.getJSONDescription());
    }

    @Override
    public String getType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getValue() {
        return null;
    }

    /**
     *
     * @param ids
     * @param prop
     * @param time_start
     * @param time_end
     * @return
     * @throws SpokExecutionException
     */
    protected JSONObject getEvents(Set<String> ids, String prop, long time_start, long time_end) throws SpokExecutionException {
        return getMediator().getPropHistManager().getDevicesStatesHistoryAsJSON(ids, prop, time_start, time_end);

    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof SpokObject)) {
            return false;
        }
        return SpokParser.equals(this, (SpokObject) o);

    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    /**
     * @return the program node
     */
    protected NodeProgram getProgramNode() {
        return (NodeProgram) findNode(NodeProgram.class, this);

    }

    /**
     *
     * @return the programName
     */
    protected String getProgramName() {
        NodeProgram p = getProgramNode();
        if (p != null) {
            return p.getProgramName();
        }
        return null;
    }

    /**
     *
     * @param duration
     * @return @throws SpokExecutionException
     */
    protected NodeEvent startClockEvent(int duration) throws SpokExecutionException {
        if (duration > 0) {
            LOGGER.debug("Starting a clock event");
            String d = getTime(duration);
            NodeEvent ev = new NodeEvent("device", getMediator().getClock().getId(), "ClockAlarm", d, this);
            ev.addEndEventListener(this);
            ev.call();
            return ev;
        }
        return null;
    }

    /**
     * @return the time in string
     */
    private String getTime(Integer duration) throws SpokExecutionException {
        Long time = getMediator().getTime() + duration * 1000;
        return time.toString();
    }

    /**
     *
     * @param sourceId
     * @param targetId
     * @param description
     * @param type
     * @param context
     * @return
     */
    protected ProgramCommandNotification getProgramLineNotification(String sourceId, String targetId, String description, ProgramCommandNotification.Type type, JSONArray context) {
        NodeProgram p = this.getProgramNode();
        if (sourceId == null) {
            sourceId = p.getId();
        }
        if (targetId == null) {
            targetId = p.getId();
        }
        return new ProgramCommandNotification(p.getJSONDescription(), p.getId(), p.getProgramName(), p.getState().toString(), this.getIID(), sourceId, targetId, description, type, context);
    }

    /**
     *
     * @param table
     */
    protected void buildReferences(ReferenceTable table) {
        // Do nothing for leaf if no Device or no Program is referenced 
    }

    abstract public String getTypeSpec();
    
    @Override
    final public String toString() {
        return "[Node("+ this.iid+ ") " + getTypeSpec() + "]";
    }
}
