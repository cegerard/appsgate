package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import java.util.concurrent.Callable;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.EndEventGenerator;
import appsgate.lig.eude.interpreter.langage.components.EndEventListener;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEventGenerator;
import appsgate.lig.eude.interpreter.langage.components.StartEventListener;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.components.Variable;
import appsgate.lig.router.spec.GenericCommand;
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
public abstract class Node implements Callable<Integer>, StartEventGenerator, StartEventListener, EndEventGenerator, EndEventListener {

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
     * The interpreter
     */
    private final EUDEInterpreterImpl interpreter; // :TODO: make this static 

    /**
     * Symbol table of the node containing the local symbols
     */
    private SymbolTable symbolTable;// :TODO: remove this unused element

    /**
     * Node parent in the abstract tree of a program
     */
    private final Node parent;

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
     * @param interpreter interpreter pointer for the nodes
     * @param p
     */
    public Node(EUDEInterpreterImpl interpreter, Node p) {
        this.interpreter = interpreter;
        this.parent = p;
    }

    /**
     * Stop the interpretation of the node. 
     * Check if the node is not started
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
     * This method is called by the
     */
    abstract protected void specificStop();

    @Override
    public void startEventFired(StartEvent e) {
        LOGGER.trace("The start event has been fired: " + e.toString());
    }

    @Override
    public void endEventFired(EndEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @param listener Listener to add
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
     * @param listener Listener to add
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
     * Getter for the local symbol table
     *
     * @return the symbol table of the node that contains the symbols defined by
     * the node
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Getter for the symbol table of the parent node
     *
     * @return the symbol table of the parent node if the node has a parent,
     * null otherwise
     */
    public SymbolTable getParentSymbolTable() {
        if (parent != null) {
            return parent.getSymbolTable();
        } else {
            return null;
        }
    }

    /**
     *
     * @return
     */
    public abstract String getExpertProgramScript();

    @Override
    public Integer call() {
        return null;
    }

    /**
     * get the command from the interpreter
     *
     * @param targetId
     * @param methodName
     * @param args
     * @return
     */
    protected GenericCommand executeCommand(String targetId, String methodName, JSONArray args) {
        return interpreter.executeCommand(targetId, methodName, args);
    }

    /**
     * get the node program from interpreter
     *
     * @param targetId
     * @return
     */
    protected NodeProgram getNodeProgram(String targetId) {
        return interpreter.getNodeProgram(targetId);
    }

    /**
     * call the program from interpreter
     *
     * @param targetId
     */
    protected void callProgram(String targetId) {
        interpreter.callProgram(targetId);
    }

    /**
     * Stop program from the interpreter
     *
     * @param targetId
     */
    protected void stopProgram(String targetId) {
        interpreter.stopProgram(targetId);
    }

    /**
     * add node listening to interpreter
     *
     * @param aThis
     */
    protected void addNodeListening(NodeEvent aThis) {
        interpreter.addNodeListening(aThis);
    }

    /**
     * remove the node listening from interpreter
     *
     * @param aThis
     */
    protected void removeNodeListening(NodeEvent aThis) {
        interpreter.removeNodeListening(aThis);
    }

    /**
     * transmit the state notification message to interpreter
     *
     * @param programStateNotificationMsg
     */
    protected void notifyChanges(ProgramStateNotificationMsg programStateNotificationMsg) {
        interpreter.notifyChanges(programStateNotificationMsg);
    }

    /**
     *
     * @return interpreter
     */
    public EUDEInterpreterImpl getInterpreter() {
        return interpreter;
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
     * @throws NodeException if there is no such parameter in the JSON Object
     */
    protected String getJSONString(JSONObject jsonObj, String jsonParam) throws NodeException {
        try {
            return jsonObj.getString(jsonParam);
        } catch (JSONException ex) {
            throw new NodeException(this.getClass().getName(), jsonParam, ex);
        }
    }

    /**
     * Method that retrieve an array from a Json object
     *
     * @param jsonObj
     * @param jsonParam
     * @return the array corresponding to the jsonParam
     * @throws NodeException if there is no such parameter in the JSON Object
     */
    protected JSONArray getJSONArray(JSONObject jsonObj, String jsonParam) throws NodeException {
        try {
            return jsonObj.getJSONArray(jsonParam);
        } catch (JSONException ex) {
            throw new NodeException(this.getClass().getName(), jsonParam, ex);
        }

    }

    /**
     * Method that retrieve an object from a Json object
     *
     * @param jsonObj
     * @param jsonParam
     * @return the object corresponding to the jsonParam
     * @throws NodeException if there is no such parameter in the JSON Object
     */
    protected JSONObject getJSONObject(JSONObject jsonObj, String jsonParam) throws NodeException {
        try {
            return jsonObj.getJSONObject(jsonParam);
        } catch (JSONException ex) {
            throw new NodeException(this.getClass().getName(), jsonParam, ex);
        }

    }

    protected void setSymbolTable(SymbolTable s) {
        this.symbolTable = s;
    }

    /**
     *
     * @param varName
     * @return
     */
    protected Variable getElementFromName(String varName) {
        if (this.symbolTable != null) {
            Variable element;
            element = this.symbolTable.getVariableByKey(varName);
            if (element != null) {
                return element;
            }
        }
        if (parent != null) {
            return parent.getElementFromName(varName);
        }
        return null;
    }

    /**
     * Method that returns the Variable name of a given id and type
     * 
     * @param id
     * @param type
     * @return
     */
    protected String getElementKey(String id, String type) {
        if (this.symbolTable != null) {
            String key;
            key = this.symbolTable.getVariableKey(id, type);
            if (key != null) {
                return key;
            }
        }
        if (parent != null) {
            return parent.getElementKey(id, type);
        }
        return null;
    }

    abstract protected void collectVariables(SymbolTable s);
}
