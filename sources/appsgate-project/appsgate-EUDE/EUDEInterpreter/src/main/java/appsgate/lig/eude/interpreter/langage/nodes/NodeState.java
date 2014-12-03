/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
abstract public class NodeState extends Node implements ICanBeEvaluated {

    /**
     * Logger
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NodeState.class);

    /**
     *
     */
    private Node objectNode;

    /**
     * The state to look for
     */
    private String stateName;

    private Node eventStartNode;
    private INodeEvent eventStart = null;
    private Node eventEndNode;
    private INodeEvent eventEnd = null;

    private boolean isOnRules;

    /**
     * Private constructor to allow copy
     *
     * @param p the parent node
     */
    protected NodeState(Node p) {
        super(p);
    }

    /**
     * Constructor
     *
     * @param o the json description
     * @param parent the parent node
     * @throws SpokNodeException
     */
    public NodeState(Node parent, JSONObject o) throws SpokNodeException {
        super(parent, o);
        if (o.has("name")) {
            stateName = getJSONString(o, "name");
        } else {
            try {
                stateName = getJSONString(o.getJSONObject("param"), "value");
            } catch (JSONException ex) {
                LOGGER.error("Unable to build NodeState: {}", o.toString());
                throw new SpokNodeException("NodeState", "name", ex);

            }
        }
        try {
            objectNode = Builder.buildFromJSON(getJSONObject(o, "object"), parent);
        } catch (SpokTypeException ex) {
            LOGGER.error("Unable to build NodeState: {}", o.toString());
            throw new SpokNodeException("NodeState", "object", ex);
        }

    }

    /**
     * @return the object node
     */
    public Node getObjectNode() {
        return objectNode;
    }

    @Override
    protected void specificStop() {
        if (eventEndNode != null) {
            eventEndNode.removeEndEventListener(this);
            eventEndNode.stop();
        }
        if (eventStartNode != null) {
            eventStartNode.removeEndEventListener(this);
            eventStartNode.stop();
        }
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        buildEventsList();
        // We are in state
        Boolean state = isOfState();
        if (state == null) {
            LOGGER.error("Unable to compute a boolean value for {}", this);
            SpokExecutionException ex = new SpokExecutionException("Unable to compute a boolean value for this state");
            return ex.getJSONDescription();
        }
        isOnRules = state;
        if (isOnRules) {
            fireStartEvent(new StartEvent(this));
        }
        listenNextEvent();
        return null;
    }

    @Override
    public String getExpertProgramScript() {
        return objectNode.getExpertProgramScript() + ".isOfState(" + stateName + ")";
    }

    protected NodeState commonCopy(NodeState o) {
        o.objectNode = objectNode.copy(o);
        o.stateName = stateName;
        return o;
    }

    @Override
    public void endEventFired(EndEvent e) {
        INodeEvent n = (INodeEvent) e.getSource();
        if (n == eventStart) {
            LOGGER.trace("the start event of the state {} has been thrown", stateName);
            isOnRules = true;
            fireStartEvent(new StartEvent(this));
            listenNextEvent();

        } else if (n == eventEnd) {
            LOGGER.trace("the end event of the state {} has been thrown", stateName);
            isOnRules = false;
            fireEndEvent(new EndEvent(this));
        } else {
            LOGGER.error("An unexpected end event ({}) has been thrown for {}", e.getSource(), this);
            return;
        }
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
        try {
            o.put("type", "state");
            o.put("object", objectNode.getJSONDescription());
            o.put("name", stateName);
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }

        return o;
    }

    /**
     * Method that build the event list
     */
    abstract protected void buildEventsList();

    /**
     *
     * @return
     */
    public boolean isOnRules() {
        return isOnRules;
    }

    /**
     * do the job when the end state event has been raised
     */
    private void listenNextEvent() {
        if (isOnRules) {
            if (eventEndNode == null) {
                LOGGER.error("ListenNextEvent: No end event for {}", this);
                return;
            }
            eventEndNode.addEndEventListener(this);
            eventEndNode.call();
        } else {
            if (eventStartNode == null) {
                LOGGER.error("ListenNextEvent: No start event for {}", this);
                return;
            }
            eventStartNode.addEndEventListener(this);
            eventStartNode.call();
        }
    }

    @Override
    public String getTypeSpec() {
        return "State " + stateName;
    }

    /**
     * @return the method that set the state in the correct shape
     * @throws SpokExecutionException
     * @throws SpokNodeException
     */
    abstract public NodeAction getSetter() throws SpokExecutionException, SpokNodeException;

    /**
     * @return the id of the object whose state is observed
     */
    public String getObjectId() {
        return objectNode.getValue();
    }

    /**
     * @return the name of the state that is observed
     */
    public String getName() {
        return stateName;

    }

    /**
     * @return true if the state is ok
     */
    abstract protected Boolean isOfState();

    /**
     * @param start
     * @param end
     */
    protected void setEvents(INodeEvent start, INodeEvent end) {
        if (eventStart == null) {
            eventStart = start;
            eventStartNode = (Node) eventStart;
            eventStartNode.setIID(this.getIID() + ".start");
        }
        if (eventEnd == null) {
            eventEnd = end;
            eventEndNode = (Node) eventEnd;
            eventEndNode.setIID(this.getIID() + ".end");
        }
    }

    @Override
    public NodeValue getResult() {
        Boolean state = isOfState();
        if (state == null) {
            return null;
        }
        if (isOfState()) {
            return new NodeValue("boolean", "true", this);
        } else {
            return new NodeValue("boolean", "false", this);
        }
    }

    @Override
    public String getResultType() {
        return "boolean";
    }

    @Override
    protected void buildReferences(ReferenceTable table) {
        if (this.eventEndNode != null) {
            eventEndNode.buildReferences(table);
        }
        if (this.eventStartNode != null) {
            eventStartNode.buildReferences(table);
        }
        if (this.objectNode != null) {
            objectNode.buildReferences(table);
        }
    }

    /**
     * @return
     */
    public INodeEvent getStartEvent() {
        return eventStart;
    }

    /**
     * @return
     */
    public INodeEvent getEndEvent() {
        return eventEnd;
    }

}
