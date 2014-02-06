/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.agregator.spec.ContextAgregatorSpec;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
class NodeState extends Node {

    private Node object;
    private String stateName;
    private Node stateValue;

    private ArrayList<NodeEvent> events;

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
     */
    public NodeState(JSONObject o, Node parent) throws SpokException {
        super(parent);
        object = Builder.buildFromJSON(getJSONObject(o, "object"), parent);
        stateName = getJSONString(o, "stateName");
        stateValue = Builder.buildFromJSON(getJSONObject(o, "stateValue"), parent);
        events = new ArrayList<NodeEvent>();
        buildEventsList();
    }

    @Override
    protected void specificStop() {
        for (NodeEvent e : events) {
            e.stop();
        }
    }

    @Override
    public JSONObject call() {
        for (NodeEvent e : events) {
            e.call();
        }
        return null;
    }

    @Override
    public String getExpertProgramScript() {
        return object.getExpertProgramScript() + "." + stateName + "(" + stateValue.getExpertProgramScript() + ")";
    }

    @Override
    protected Node copy(Node parent) {
        NodeState o = new NodeState(parent);
        o.events = new ArrayList<NodeEvent>();
        for (NodeEvent e : events) {
            o.events.add((NodeEvent) e.copy(parent));
        }
        o.object = object.copy(parent);
        o.stateName = stateName;
        o.stateValue = stateValue.copy(parent);
        return o;
    }

    @Override
    public void endEventFired(EndEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", "state");
            o.put("targetId", object.getJSONDescription());
            o.put("stateName", stateName);
            o.put("stateValue", stateValue.getJSONDescription());
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
        ContextAgregatorSpec context = getMediator().getContext();
        String brickType = context.getBrickType(object.getValue());
        if (brickType == null || brickType.isEmpty()) {
            throw new SpokExecutionException("There is no type found for the device " + object.getValue());
        }
        JSONArray eventsFromState = context.getEventsFromState(brickType, stateName, stateValue.getValue());
        if (eventsFromState == null) {
            throw new SpokExecutionException("No events are defined for the given state");
        }
        // everything is OK
        for (int i = 0; i < eventsFromState.length(); i++) {
            events.add(new NodeEvent(brickType, object.getValue(), stateName, stateValue.getValue(), this));
        }
    }

}
