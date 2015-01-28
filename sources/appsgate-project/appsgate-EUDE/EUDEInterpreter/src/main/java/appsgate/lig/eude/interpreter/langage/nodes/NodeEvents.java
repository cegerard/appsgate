/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.ClockProxy;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public abstract class NodeEvents extends Node implements INodeEvent{

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeEvents.class);

    /**
     * the sequence of event
     */
    protected ArrayList<Node> listOfEvent;

    /**
     * The time to wait to invalidate this sequence of event
     */
    protected Integer duration = 0;

    /**
     * Default constructor for copy
     *
     * @param p parent node
     */
    protected NodeEvents(Node p) {
        super(p);
    }

    /**
     * Constructor with json description
     *
     * @param o the json description
     * @param parent the parent node
     * @throws SpokNodeException
     */
    public NodeEvents(JSONObject o, Node parent) throws SpokNodeException {
        super(parent, o);
        JSONArray seqEventJSON = getJSONArray(o, "events");
        JSONObject stateTarget = null;
        listOfEvent = new ArrayList<Node>();
        duration = o.optInt("duration", 0);
        // Transmit the target, if the event is based on a state node.
        if (o.has("stateTarget")) {
            stateTarget = o.optJSONObject("stateTarget");
        }
        for (int i = 0; i < seqEventJSON.length(); i++) {
            try {
                listOfEvent.add(Builder.buildFromJSON(seqEventJSON.getJSONObject(i), this, stateTarget));
            } catch (JSONException ex) {
                throw new SpokNodeException(this, "NodeEvents", "item " + i, ex);
            } catch (SpokTypeException ex) {
                throw new SpokNodeException(this, "NodeEvents", "event", ex);
            }
        }
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        if (listOfEvent.isEmpty()) {
            LOGGER.warn("No events to track, consider the events as raised");
            fireEndEvent(new EndEvent(this));
            return null;
        }
        specificCall();
        return null;
    }

    /**
     *
     */
    protected void specificCall() {
        for (Node e : listOfEvent) {
            e.addEndEventListener(this);
            e.call();
        }
    }

    @Override
    protected void specificStop() {
        for (Node e : listOfEvent) {
            e.removeEndEventListener(this);
            e.stop();
        }
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject ret = super.getJSONDescription();
        try {
            ret.put("events", getJSONArray());
            ret.put("duration", duration);
            ret = specificDesc(ret);
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return ret;
    }

    abstract JSONObject specificDesc(JSONObject ret) throws JSONException;

    /**
     * This method copy all of the data that are common to all the events node
     *
     * @param ret
     * @return
     */
    protected NodeEvents commonCopy(NodeEvents ret) {
        ret.listOfEvent = new ArrayList<Node>();
        for (Node n : listOfEvent) {
            ret.listOfEvent.add((NodeEvent) n.copy(ret));
        }
        ret.duration = duration;
        return ret;
    }

    /**
     * @return the array of events
     */
    private JSONArray getJSONArray() {
        JSONArray a = new JSONArray();
        int i = 0;
        for (Node n : this.listOfEvent) {
            try {
                a.put(i, n.getJSONDescription());
                i++;
            } catch (JSONException ex) {
                // Do nothing since 'JSONObject.put(key,val)' would raise an exception
                // only if the key is null, which will never be the case
            }
        }
        return a;
    }

    /**
     *
     * @param nodeEnded
     * @return
     * @throws SpokExecutionException
     */
    protected Boolean isClockEvent(NodeEvent nodeEnded) throws SpokExecutionException {
        EUDEInterpreter mediator = getMediator();
        ClockProxy p = mediator.getClock();
        if (p == null) {
            throw new SpokExecutionException("Unable to find clock");
        }
        return nodeEnded.getSourceId().equals(p.getId());
    }



    @Override
    public void endEventFired(EndEvent e) {
        NodeEvent nodeEnded = (NodeEvent) e.getSource();
        if (!isStopping()) {
            try {
                if (isClockEvent(nodeEnded)) {
                    LOGGER.debug("ClockEvent detected");
                    dealWithClockEvent(nodeEnded);
                } else {
                    LOGGER.debug("NEvents end event: {}", nodeEnded);
                    dealWithNormalEvent(nodeEnded);
                }
            } catch (SpokExecutionException ex) {
                LOGGER.error(ex.getMessage());
            }
        } else {
            LOGGER.warn("endEvent {} has been fired while the node was stopping", nodeEnded);
        }
    }

    /**
     * 
     * @return the duration
     */
    protected int getDuration() {
            return duration;
        
    }
    
    abstract void dealWithClockEvent(NodeEvent e) throws SpokExecutionException;

    abstract void dealWithNormalEvent(NodeEvent e) throws SpokExecutionException;
        
    @Override
    protected void buildReferences(ReferenceTable table, HashMap<String,String> args) {
        for (Node n: this.listOfEvent) {
            n.buildReferences(table, null);
        }
    }

}
