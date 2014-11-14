/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.StateDescription;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import appsgate.lig.eude.interpreter.spec.ProgramCommandNotification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeStateDevice extends NodeState {

    /**
     * Logger
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NodeStateDevice.class);

    /**
     * The object to be observed
     */
    private ICanBeEvaluated object;

    private StateDescription desc = null;

    /**
     * Private constructor to allow copy
     *
     * @param p the parent node
     */
    private NodeStateDevice(Node p) {
        super(p);
    }

    /**
     * Constructor
     *
     * @param o the json description
     * @param parent the parent node
     * @throws SpokNodeException
     */
    public NodeStateDevice(JSONObject o, Node parent) throws SpokNodeException {
        super(parent, o);
        if (getObjectNode() instanceof ICanBeEvaluated) {
            object = (ICanBeEvaluated) getObjectNode();
        } else {
            throw new SpokNodeException("NodeStateDevice", "object", null);
        }
    }

    @Override
    protected Node copy(Node parent) {
        NodeStateDevice o = new NodeStateDevice(parent);
        return commonCopy(o);
    }


    /**
     * Method that build the event list
     */
    @Override
    protected void buildEventsList() {
        EHMIProxySpec context;
        try {
            context = getMediator().getContext();
            desc = context.getEventsFromState(object.getResult().getValue(), getName());
        } catch (SpokExecutionException ex) {

        }

        if (desc == null) {
            LOGGER.error("State {} not found for {}", getName(), object.getResult().getValue());
            return;
        }
        // everything is OK
        setEvents(buildFromOntology(desc.getStartEvent()), buildFromOntology(desc.getEndEvent()));
    }

    /**
     *
     * @param o
     * @return
     * @throws SpokExecutionException
     */
    private INodeEvent buildFromOntology(JSONObject o) {
        Node events;
        if (o == null) {
            LOGGER.error("No event associated with this state");
            return null;
        }

        JSONObject target;
        target = object.getResult().getJSONDescription();

        try {
            events = Builder.buildFromJSON(o, this, target);
        } catch (SpokTypeException ex) {
            LOGGER.error("Unable to build events: {}", ex.getMessage());
            return null;
        }
        if (!(events instanceof INodeEvent)) {
            LOGGER.error("The events state of the grammar is not an event (INodeEvent)");
            return null;
        }
        return (INodeEvent) events;
    }


    @Override
    public NodeAction getSetter() throws SpokExecutionException, SpokNodeException {
        JSONObject action = desc.getSetter();
        if (action == null) {
            throw new SpokExecutionException("No setter has been found for this state: " + desc.getStateName());
        }
        try {
            action.put("target", getObjectNode().getJSONDescription());
        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return new NodeAction(action, this);
    }

    @Override
    protected Boolean isOfState() {
        if (desc == null){
            LOGGER.error("No state have been set");
            return false;
        }
        if (desc.getStateName() == null) {
            LOGGER.debug("No state name, so the result is false");
            return false;
        }
        String targetId = object.getResult().getValue();
        LOGGER.trace("Asking for {}, {}", object.getResult().getValue(), desc.getStateName());
        ProgramCommandNotification notif = getProgramLineNotification(null, targetId, "Reading from", ProgramCommandNotification.Type.READ);

        GenericCommand cmd = null;
        try {
            cmd = getMediator().executeCommand(targetId, desc.getStateName(), new JSONArray(), notif);
        } catch (SpokExecutionException ex) {
        }
        if (cmd == null) {
            LOGGER.error("Unable to retrieve the command, considering as null");
            return null;
        }
        cmd.run();
        Object aReturn = cmd.getReturn();
        if (aReturn == null) {
            LOGGER.error("There was no return value, returning null");
            return null;
        }
        LOGGER.debug("Is of state: [" + aReturn.toString() + "] compared to: [" + desc.getStateValue() + "]");
        return (desc.getStateValue().equalsIgnoreCase(aReturn.toString()));
    }


}
