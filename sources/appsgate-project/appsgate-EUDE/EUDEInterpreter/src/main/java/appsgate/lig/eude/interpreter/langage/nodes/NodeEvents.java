/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
class NodeEvents extends Node {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeEvents.class.getName());

    /**
     * the sequence of event
     */
    private ArrayList<NodeEvent> seqEvent;
    /**
     * The number of events that have fired EndEvent
     */
    private int nbEventEnded = 0;

    /**
     *
     */
    private Integer nbEventToOccur = null;

    private Integer duration = 0;

    /**
     * Default constructor for copy
     *
     * @param p parent node
     */
    private NodeEvents(Node p) {
        super(p);
    }

    /**
     * Constructor with json description
     *
     * @param o the json description
     * @param parent the parent node
     */
    public NodeEvents(JSONObject o, Node parent) throws SpokNodeException {
        super(parent);
        JSONArray seqEventJSON = getJSONArray(o, "events");
        seqEvent = new ArrayList<NodeEvent>();
        for (int i = 0; i < seqEventJSON.length(); i++) {
            try {
                seqEvent.add(new NodeEvent(seqEventJSON.getJSONObject(i), this));
            } catch (JSONException ex) {
                throw new SpokNodeException("NodeSeqEvent", "item " + i, ex);
            }
        }
        nbEventToOccur = o.optInt("nbEventToOccur");
        if (nbEventToOccur == null) {
            nbEventToOccur = seqEvent.size();
        }
        // If we just want one event among the events, no need to wait for a duration
        if (nbEventToOccur > 1) {
            duration = o.optInt("duration");
        }

    }

    @Override
    protected void specificStop() {
        for (Node e : seqEvent) {
            e.removeEndEventListener(this);
            e.stop();
        }
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        nbEventEnded = 0;
        for (Node e : seqEvent) {
            e.addEndEventListener(this);
            e.call();
        }
        return null;
    }

    @Override
    public String getExpertProgramScript() {
        String ev = "[";
        for (Node e : seqEvent) {
            ev += e.getExpertProgramScript() + ",";
        }
        ev = ev.substring(0, ev.length() - 1) + "]";
        return ev;
    }

    @Override
    protected Node copy(Node parent) {
        NodeEvents ret = new NodeEvents(parent);
        ret.seqEvent = new ArrayList<NodeEvent>();
        for (Node n : seqEvent) {
            ret.seqEvent.add((NodeEvent) n.copy(ret));
        }
        ret.duration = duration;
        ret.nbEventToOccur = nbEventToOccur;
        return ret;

    }

    @Override
    public void endEventFired(EndEvent e) {
        NodeEvent nodeEnded = (NodeEvent) e.getSource();
        LOGGER.debug("NEvents end event: {}", nodeEnded);
        if (!isStopping()) {
            if (nodeEnded.getSourceId().equals(getClockId())) {
                nbEventEnded--;
            } else {
                nbEventEnded++;
                if (nbEventEnded >= nbEventToOccur) {
                    LOGGER.debug("All the events have been ended");
                    stop();
                    fireEndEvent(new EndEvent(this));
                } else {
                    nodeEnded.addEndEventListener(this);
                    nodeEnded.call();
                    startClockEvent();
                }
            }
        } else {
            LOGGER.warn("endEvent has been fired while the node was stopping");
        }
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject ret = new JSONObject();
        try {
            ret.put("type", "events");
            ret.put("events", getJSONArray());
            ret.put("nbEventToOccur", nbEventToOccur);
            ret.put("duration", duration);
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return ret;
    }

    @Override
    public String toString() {
        return "[Node Events: " + seqEvent.size() + " events]";
    }

    /**
     * @return the array of events
     */
    private JSONArray getJSONArray() {
        JSONArray a = new JSONArray();
        int i = 0;
        for (Node n : this.seqEvent) {
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
     * TODO: implement this correctly
     *
     * @return
     */
    public String getClockId() {
        return "clock";
    }

    /**
     *
     */
    private void startClockEvent() {
        if (duration > 0) {
            LOGGER.debug("Starting a clock event");
            String d = getTime(duration);
            NodeEvent ev = new NodeEvent("clock", getClockId(), "clockAlarm", d, this);
            seqEvent.add(ev);
            ev.addEndEventListener(this);
            ev.call();

        }
    }

    /**
     * TODO: implement this correctly
     *
     * @return
     */
    private String getTime(Integer duration) {
        return duration.toString();
    }

}
