/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeStateProgram extends NodeState {

    /**
     * Logger
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NodeStateProgram.class);

    private Boolean stateOn;

    public NodeStateProgram(JSONObject o, Node parent) throws SpokNodeException {
        super(parent, o);
        stateOn = getName().equalsIgnoreCase("isStarted");
    }

    /**
     * Private constructor to allow copy
     *
     * @param p the parent node
     */
    private NodeStateProgram(Node p) {
        super(p);
    }

    @Override
    protected Node copy(Node parent) {
        NodeStateProgram o = new NodeStateProgram(parent);
        return commonCopy(o);
    }

    @Override
    protected void buildEventsList() {
        JSONObject beginEvent;
        JSONObject endEvent;
        try {
            beginEvent = new JSONObject("{'type':'nodeEvent', 'eventName':'programCall', 'eventValue':'start'}");
            beginEvent.put("source", getObjectNode().getJSONDescription());
            endEvent = new JSONObject("{'type':'nodeEvent', 'eventName':'programCall', 'eventValue':'stop'}");
            endEvent.put("source", getObjectNode().getJSONDescription());
        } catch (JSONException ex) {
            LOGGER.error("unable to build a JSON object");
            return;
        }
        try {
            // everything is OK
            setEvents(new NodeEvent(beginEvent, this), new NodeEvent(endEvent, this));
        } catch (SpokNodeException ex) {
            LOGGER.error("Unable to create nodes ({}, {})", beginEvent.toString(), endEvent.toString());
        }

    }

    @Override
    public NodeAction getSetter() throws SpokExecutionException, SpokNodeException {
        JSONObject o = null;
        try {
            o = new JSONObject("{'type':'nodeAction', 'args':[], 'returnType':''}");
            o.put("target", getObjectNode().getJSONDescription());
        } catch (JSONException ex) {
            LOGGER.error("unable to build a JSON object");
            throw new SpokExecutionException("Unable to build JSON object");
        }

        if (stateOn == true) {
            try {
                o.put("action", "start");
            } catch (JSONException ex) {
            }
            return new NodeAction(o, this);
        } else {
            try {
                o.put("action", "stop");
            } catch (JSONException ex) {
            }
            return new NodeAction(o, this);
        }

    }

    @Override
    protected Boolean isOfState() {
        try {
            
            NodeProgram p = getMediator().getNodeProgram(getObjectId());
            if (p != null) {
                return p.isRunning() == stateOn;
            }
            else {
                LOGGER.error("The program ({}) does not exist");
                return null;
            }
        } catch (SpokExecutionException ex) {
            LOGGER.error("An exception occur");
            return false;
        }
    }

}
