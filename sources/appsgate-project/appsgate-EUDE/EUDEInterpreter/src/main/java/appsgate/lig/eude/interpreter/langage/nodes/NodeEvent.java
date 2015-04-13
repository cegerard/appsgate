package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.dependency.graph.Reference.REFERENCE_TYPE;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.references.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.context.dependency.graph.ReferenceDescription;
import appsgate.lig.eude.interpreter.spec.ProgramEventNotification;
import org.json.JSONException;
import org.json.JSONObject;
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
public class NodeEvent extends Node implements INodeEvent {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeEvent.class);

    /**
     * Type of the source to listen. Can be "program" or "device"
     */
    private Node source;

    /**
     * Name of the event to listen
     */
    private String eventName;

    /**
     * Value of the event to wait
     */
    private String eventValue;

    /**
     *
     * @param parent
     */
    private NodeEvent(Node parent) {
        super(parent);
    }

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
        Node s = new NodeValue(s_type, s_id, this);
        source = s;
        eventName = name;
        eventValue = value;
    }

    /**
     *
     * @param eventJSON JSON representation of the event
     * @param parent
     * @throws SpokNodeException
     */
    public NodeEvent(JSONObject eventJSON, Node parent)
            throws SpokNodeException {
        super(parent, eventJSON);
        source = getDevice(eventJSON, "source");
        eventName = getJSONString(eventJSON, "eventName");
        if (eventJSON.has("eventValue") ) {
            eventValue = getJSONString(eventJSON, "eventValue");
        } else {
            JSONObject optJSONObject = getJSONObject(eventJSON, "param");
            eventValue = getJSONString(optJSONObject, "value");
        }

    }

    @Override
    public JSONObject call() {
        fireStartEvent(new StartEvent(this));
        setStarted(true);
        EUDEInterpreter mediator;
        try {
            mediator = getMediator();
        } catch (SpokExecutionException ex) {
            LOGGER.error("Unable to call this node: {}", this);
            return ex.getJSONDescription();
        }

        // if the source of the event is a program
        if (isProgramEvent()) {
            // get the node of the program
            NodeProgram p;
            p = mediator.getNodeProgram(source.getValue());
            // if it exists
            if (p != null) {
                // listen to its start event...
                if (eventName.equals("runningState")) {
                    LOGGER.trace("Node event added for {}", source.getValue());
                    mediator.addNodeListening(this);
                } else {
                    LOGGER.warn("Event ({}) not supported for programs.", eventName);
                }
            } else {
                // interpreter does not know the program, then the end event is automatically fired
                LOGGER.warn("Program {} not found", source.getValue());
                setStarted(false);
                fireEndEvent(new EndEvent(this));
            }
            // source.getType() is "device"
        } else {
            LOGGER.trace("Node event added for {}", source.getValue());
            mediator.addNodeListening(this);
        }
        return null;
    }

    @Override
    protected void specificStop() {
        EUDEInterpreter mediator;
        try {
            mediator = getMediator();
        } catch (SpokExecutionException ex) {
            LOGGER.error("Unable to stop this node cause the mediator has not been found");
            return;
        }
        if (isProgramEvent()) {
            NodeProgram p = mediator.getNodeProgram(source.getValue());
            if (eventName.equals("start")) {
                p.removeStartEventListener(this);
            } else if (eventName.equals("end")) {
                p.removeEndEventListener(this);
            }
        } else {
            mediator.removeNodeListening(this);
        }
    }

    /**
     * Once the event is fired, transmit the fact that the event has been fired
     */
    public void coreEventFired() {
        if (isStarted()) {
            try {
                getMediator().removeNodeListening(this);
            } catch (SpokExecutionException ex) {
                LOGGER.error("Unable to remove the node listening from {}", this);
            }
            setStarted(false);
            ProgramEventNotification n = new ProgramEventNotification(this.getProgramNode(), this.getIID(), this.getSourceId(), eventName, eventValue);
            this.notifyLine(n);
            fireEndEvent(new EndEvent(this));
        }
    }

    @Override
    public void endEventFired(EndEvent e) {
    }

    @Override
    public String getTypeSpec() {
        return "Event on " + source.getValue();
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
        try {
            o.put("type", "event");
            o.put("source", source.getJSONDescription());
            o.put("eventName", eventName);
            o.put("eventValue", eventValue);
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;

    }

    /**
     * @return the sourceID
     */
    public String getSourceId() {
        if (source instanceof NodeValue) {
            String val = ((NodeValue) source).getVariableValue();
            if (val != null) {
                return val;
            }
        }
        return source.getValue();
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
        ret = source.getExpertProgramScript() + ".";
        return ret + eventName + "(\"" + eventName + "\")";

    }

    @Override
    protected Node copy(Node parent) {
        NodeEvent ret = new NodeEvent(parent);
        ret.eventName = eventName;
        ret.eventValue = eventValue;
        ret.source = source.copy(parent);
        return ret;

    }

    @Override
    protected void buildReferences(ReferenceTable table, ReferenceDescription d) {
        if (source != null) {
            ReferenceDescription refData = new ReferenceDescription(REFERENCE_TYPE.READING, this.eventName);
            source.buildReferences(table, refData);
        }
    }

    public boolean isProgramEvent() {
        return source.getType().equalsIgnoreCase("programCall");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof NodeEvent)) {
            return false;
        }
        NodeEvent e = (NodeEvent) o;
        return this.eventName.equals(e.eventName) && this.eventValue.equals(e.eventValue) & this.source.equals(o);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
}
