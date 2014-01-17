package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node representing the events
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since June 25, 2013
 * @version 1.0.0
 */
public class NodeEvent extends Node {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeEvent.class);

    /**
     * Type of the source to listen. Can be "program" or "device"
     */
    private final String sourceType;
    /**
     * ID of the source to listen
     */
    private final String sourceId;

    /**
     * Name of the event to listen
     */
    private final String eventName;

    /**
     * Value of the event to wait
     */
    private final String eventValue;

    /**
     *
     * @param s_type
     * @param s_id
     * @param name
     * @param value
     * @param parent
     */
    public NodeEvent(String s_type, String s_id, String name, String value, Node parent) {
        super(parent);
        sourceType = s_type;
        sourceId = s_id;
        eventName = name;
        eventValue = value;
    }

    /**
     *
     * @param eventJSON JSON representation of the event
     * @param parent
     * @throws SpokNodeException
     */
    public NodeEvent(JSONObject eventJSON, Node parent) throws SpokNodeException {
        super(parent);
        sourceType = getJSONString(eventJSON, "sourceType");
        sourceId = getJSONString(eventJSON, "sourceId");
        eventName = getJSONString(eventJSON, "eventName");
        eventValue = getJSONString(eventJSON, "eventValue");

    }

    @Override
    public JSONObject call() {
        fireStartEvent(new StartEvent(this));
        setStarted(true);
        EUDEInterpreterImpl interpreter;
        try {
            interpreter = getInterpreter();
        } catch (SpokExecutionException ex) {
            LOGGER.error("Unable to call this node: {}", this);
            return null;
        }

        // if the source of the event is a program
        if (sourceType.equals("program")) {
            // get the node of the program
            NodeProgram p;
            p = interpreter.getNodeProgram(sourceId);
            // if it exists
            if (p != null) {
                // listen to its start event...
                if (eventName.equals("runningState")) {
                    LOGGER.trace("Node event added for {}", sourceId);
                    interpreter.addNodeListening(this);
                } else {
                    LOGGER.warn("Event ({}) not supported for programs.", eventName);
                }
            } else {
                // interpreter does not know the program, then the end event is automatically fired
                LOGGER.warn("Program {} not found", sourceId);
                setStarted(false);
                fireEndEvent(new EndEvent(this));
            }
            // sourceType is "device"
        } else {
            LOGGER.trace("Node event added for {}", sourceId);
            interpreter.addNodeListening(this);
        }

        return null;
    }

    @Override
    public void specificStop() throws SpokException{
        if (sourceType.equals("program")) {
            NodeProgram p = getInterpreter().getNodeProgram(sourceId);
            if (eventName.equals("start")) {
                p.removeStartEventListener(this);
            } else if (eventName.equals("end")) {
                p.removeEndEventListener(this);
            }
        } else {
            getInterpreter().removeNodeListening(this);
        }
    }

    /**
     * Once the event is fired, transmit the fact that the event has been fired
     */
    public void coreEventFired() {
        setStarted(false);
        fireEndEvent(new EndEvent(this));
    }

    @Override
    public void endEventFired(EndEvent e) {
        LOGGER.debug("EndEvent fired: {}", e.toString());
    }

    @Override
    public String toString() {
        return "[Node Event on " + sourceId + "]";
    }
    
    @Override
    JSONObject getJSONDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the sourceID
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * @return the eventName
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * @return the eventValue
     */
    public String getEventValue() {
        return eventValue;
    }

    @Override
    public String getExpertProgramScript() {
        String ret;
        ret = this.getElementKey(sourceId, sourceType) + ".";
        return ret + eventName + "(\"" + eventName + "\")";

    }

    @Override
    protected void collectVariables(SymbolTable s) {
        s.addAnonymousVariable(sourceId, sourceType);
    }

    @Override
    Node copy(Node parent) {
        NodeEvent ret = new NodeEvent(this.sourceType, this.sourceId, this.eventName, this.eventValue, parent);
        ret.setSymbolTable(this.getSymbolTable());
        return ret;

    }

}
