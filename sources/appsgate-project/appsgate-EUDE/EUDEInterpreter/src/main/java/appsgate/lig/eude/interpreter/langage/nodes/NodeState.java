/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.StateDescription;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeState extends Node implements ICanBeEvaluated {

    /**
     * Logger
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NodeState.class);

    /**
     * The object to be observed
     */
    private Node objectNode;
    private ICanBeEvaluated object;
    /**
     * The state to look for
     */
    private String stateName;

    private Node eventStartNode;
    private INodeEvent eventStart = null;
    private Node eventEndNode;
    private INodeEvent eventEnd = null;

    private boolean isOnRules;

    private StateDescription desc = null;

    /**
     * Private constructor to allow copy
     *
     * @param p the parent node
     */
    private NodeState(Node p) {
        super(p);
    }

    /**
     * Constructor
     *
     * @param o the json description
     * @param parent the parent node
     * @throws SpokNodeException
     */
    public NodeState(JSONObject o, Node parent) throws SpokNodeException {
        super(parent, o);
        try {
            objectNode = Builder.buildFromJSON(getJSONObject(o, "object"), parent);
            if (objectNode instanceof ICanBeEvaluated) {
                object = (ICanBeEvaluated) objectNode;
            } else {
                throw new SpokNodeException("NodeState", "object", null);
            }
        } catch (SpokTypeException ex) {
            throw new SpokNodeException("NodeState", "object", ex);
        }
        stateName = getJSONString(o, "name");

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
        try {
            buildEventsList();
        } catch (SpokExecutionException ex) {
            LOGGER.error("Unable to build Events list");
            return ex.getJSONDescription();
        }
        try {
            // We are in state
            if (isOfState()) {
                isOnRules = true;
                fireStartEvent(new StartEvent(this));
                listenEndStateEvent();
            } else {
                isOnRules = false;
                eventStartNode.addEndEventListener(this);
                eventStartNode.call();
            }
        } catch (SpokExecutionException ex) {
            LOGGER.error("Unable to execute the State node, due to: " + ex);
            return ex.getJSONDescription();
        }
        return null;
    }

    @Override
    public String getExpertProgramScript() {
        return objectNode.getExpertProgramScript() + ".isOfState(" + stateName + ")";
    }

    @Override
    protected Node copy(Node parent) {
        NodeState o = new NodeState(parent);
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
            listenEndStateEvent();
        } else {
            LOGGER.trace("the end event of the state {} has been thrown", stateName);
            isOnRules = false;
            fireEndEvent(new EndEvent(this));
            setStarted(false);
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
     *
     * @throws SpokExecutionException
     */
    private void buildEventsList() throws SpokExecutionException {
        EHMIProxySpec context;
        context = getMediator().getContext();
        desc = context.getEventsFromState(object.getResult().getValue(), stateName);

        if (desc == null) {
            LOGGER.error("State {} not found for {}", stateName, object.getResult().getValue());
            throw new SpokExecutionException("There is no type found for the device " + objectNode.getValue());
        }
        // everything is OK
        if (eventStart == null) {
            eventStart = buildFromOntology(desc.getStartEvent());
            eventStartNode = (Node) eventStart;
        }
        if (eventEnd == null) {
            eventEnd = buildFromOntology(desc.getEndEvent());
            eventEndNode = (Node) eventEnd;
        }
    }

    /**
     *
     * @param o
     * @return
     * @throws SpokExecutionException
     */
    private INodeEvent buildFromOntology(JSONObject o) throws SpokExecutionException {
        Node events;
        if (o == null) {
            throw new SpokExecutionException("No event associated with this state");
        }

        JSONObject target;
        target = object.getResult().getJSONDescription();

        try {
            events = Builder.buildFromJSON(o, this, target);
        } catch (SpokTypeException ex) {
            LOGGER.error("Unable to build events: {}", ex.getMessage());
            throw new SpokExecutionException("grammar is not correctly formed");
        }
        if (!(events instanceof INodeEvent)) {
            throw new SpokExecutionException("The events state of the grammar is not an event (INodeEvent)");
        }
        return (INodeEvent) events;
    }

    /**
     *
     * @return
     */
    boolean isOnRules() {
        return isOnRules;
    }

    /**
     * do the job when the end state event has been raised
     */
    private void listenEndStateEvent() {
        eventEndNode.addEndEventListener(this);
        eventEndNode.call();
    }

    @Override
    public String toString() {
        return "[State " + stateName + "]";
    }

    /**
     * @return the method that set the state in the correct shape
     */
    NodeAction getSetter() throws SpokExecutionException, SpokNodeException {
        JSONObject action = desc.getSetter();
        if (action == null) {
            throw new SpokExecutionException("No setter has been found for this state: " + desc.getStateName());
        }
        try {
            action.put("target", objectNode.getJSONDescription());
        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return new NodeAction(action, this);
    }

    /**
     * @return the id of the object whose state is observed
     */
    String getObjectId() {
        return objectNode.getValue();
    }

    /**
     * @return the name of the state that is observed
     */
    String getName() {
        return stateName;

    }

    /**
     * @return true if the state is ok
     * @throws SpokExecutionException
     */
    private boolean isOfState() throws SpokExecutionException {
        if (desc.getStateName() == null) {
            LOGGER.debug("No state name, so the result is false");
            return false;
        }
        LOGGER.trace("Asking for {}, {}", object.getResult().getValue(), desc.getStateName());
        GenericCommand cmd = getMediator().executeCommand(object.getResult().getValue(), desc.getStateName(), new JSONArray());
        if (cmd == null) {
            throw new SpokExecutionException("The command has not been created");
        }
        cmd.run();
        Object aReturn = cmd.getReturn();
        if (aReturn == null) {
            throw new SpokExecutionException("The command has no return");
        }
        LOGGER.debug("Is of state: [" + aReturn.toString() + "] compared to: [" + desc.getStateValue() + "]");
        return (desc.getStateValue().equalsIgnoreCase(aReturn.toString()));
    }

    @Override
    public NodeValue getResult() throws SpokExecutionException {
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

}
