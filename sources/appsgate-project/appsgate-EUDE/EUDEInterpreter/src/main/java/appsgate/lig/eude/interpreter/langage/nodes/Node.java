package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
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
import appsgate.lig.eude.interpreter.langage.components.SpokVariable;
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
     * @param interpreter interpreter pointer for the nodes
     * @param p
     */
    public Node(EUDEInterpreterImpl interpreter, Node p) {
        this.interpreter = interpreter;
        this.parent = p;
    }

    /**
     * @param n the parent node
     */
    protected void setParent(Node n) {
        this.parent = n;
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

    @Override
    public Integer call() {
        return null;
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
     * Method to find a variable by its name
     *
     * it recursively parse the tree to find the name
     *
     * @param varName the name of the variable
     * @return The SpokVariable
     */
    protected SpokVariable getVariableByName(String varName) {
        if (this.getSymbolTable() != null) {
            SpokVariable element;
            element = this.getSymbolTable().getVariableByKey(varName);
            if (element != null) {
                return element;
            }
        }
        if (parent != null) {
            return parent.getVariableByName(varName);
        }
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
     * Method that returns the SpokVariable name of a given id and type
     *
     * @param id
     * @param type
     * @return
     */
    protected String getElementKey(String id, String type) {
        if (this.symbolTable != null) {
            String key;
            key = this.symbolTable.getAnonymousVariableKey(id, type);
            if (key != null) {
                return key;
            }
        }
        if (parent != null) {
            return parent.getElementKey(id, type);
        }
        return null;
    }

    /**
     * Helper method to build an expert Program from anonymous variables
     *
     * @param s the symbol table to populate
     */
    abstract protected void collectVariables(SymbolTable s);

    /**
     * Method to copy a node and the rules behind
     *
     * @param parent
     */
    abstract Node copy(Node parent);

}
