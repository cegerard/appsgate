package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import java.util.concurrent.Callable;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.EndEventGenerator;
import appsgate.lig.eude.interpreter.langage.components.EndEventListener;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEventGenerator;
import appsgate.lig.eude.interpreter.langage.components.StartEventListener;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.components.SpokParser;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import java.util.ArrayList;
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
     * Default constructor
     *
     * @param p
     */
    public Node(Node p) {
        this.parent = p;
    }

    /**
     * @param n the parent node
     */
    protected void setParent(Node n) {
        this.parent = n;
    }

    public Node getParent() {
        return parent;
    }

    /**
     * Stop the interpretation of the node. Check if the node is not started
     */
    public void stop() {
        if (isStarted()) {
            setStopping(true);

            specificStop();
            setStarted(false);
            setStopping(false);
        } else {
            LOGGER.warn("Trying to stop a not started node {}", this);
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
        //during the execution the list can be updated
        int nbListeners = startEventListeners.size();
        for (int i = 0; i < nbListeners; i++) {
            StartEventListener l = startEventListeners.poll();
            l.startEventFired(e);
        }
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
        startEventListeners.remove(listener);
    }

    /**
     * Add a new listener to the end event of the node
     *
     * @param listener Listener to addAnonymousVariable
     */
    @Override
    public void addEndEventListener(EndEventListener listener) {
        LOGGER.trace("ADD:  {} listen EndEvent FROM {}", listener, this);
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
        endEventListeners.remove(listener);
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
        throw new SpokExecutionException("No mediator found");
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
            throw new SpokNodeException(this.getClass().getName(), jsonParam, ex);
        }

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
     * Method that return the value associated to a node Must be overridden
     *
     * @return null by default
     * @throws SpokException
     */
    public SpokObject getResult() throws SpokException {
        return null;
    }

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
     * @param what
     * @param where
     * @return
     * @throws SpokExecutionException
     */
    protected JSONArray getDevicesInSpaces(JSONArray what, JSONArray where)
            throws SpokExecutionException {
        ArrayList<String> WHAT = new ArrayList<String>();
        for (int i = 0; i < what.length(); i++) {
            JSONObject o = what.optJSONObject(i);
            if (o != null) {
                WHAT.add(o.optString("value"));
            }
        }
        ArrayList<String> WHERE = new ArrayList<String>();
        for (int i = 0; i < where.length(); i++) {
            JSONObject o = where.optJSONObject(i);
            if (o != null) {
                WHERE.add(o.optString("value"));
            }
        }
        ArrayList<String> devicesInSpaces = getMediator().getContext().getDevicesInSpaces(WHAT, WHERE);
        JSONArray retArray = new JSONArray();
        for (String name : devicesInSpaces) {
            NodeValue n = new NodeValue("device", name, this);
            retArray.put(n.getJSONDescription());
        }
        return retArray;
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
     *
     * @return the programName
     */
    protected String getProgramName() {
        NodeProgram p = (NodeProgram) findNode(NodeProgram.class, this);
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
     * @return
     */
    private String getTime(Integer duration) throws SpokExecutionException {
        Long time = getMediator().getTime() + duration * 1000;
        return time.toString();
    }

}
