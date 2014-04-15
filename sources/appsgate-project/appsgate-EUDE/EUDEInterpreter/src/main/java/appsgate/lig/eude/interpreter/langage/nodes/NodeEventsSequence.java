/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeEventsSequence extends NodeEvents {

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventsSequence.class);
    
    /**
     * 
     */
    private NodeEvent clockEvent = null;
    
    /**
     * 
     */
    private int nextEvent = 0;

    /**
     * Private constructor to allow copy
     *
     * @param parent
     */
    private NodeEventsSequence(Node parent, String iid) {
        super(parent, iid);
    }

    /**
     * Constructor
     *
     * @param o
     * @param parent
     * @throws SpokNodeException
     */
    public NodeEventsSequence(JSONObject o, Node parent) throws SpokNodeException {
        super(o, parent);
    }

    @Override
    JSONObject specificDesc(JSONObject ret) throws JSONException {
        ret.put("type", "eventsSequence");
        return ret;
    }

    @Override
    protected void specificCall() {
        clockEvent = null;
        nextEvent = 0;
        callEvent();
    }

    @Override
    public String getExpertProgramScript() {
        return "[" + StringUtils.join(listOfEvent, " THEN ") + "]";

    }

    @Override
    protected Node copy(Node parent) {
        NodeEventsSequence ret = new NodeEventsSequence(parent, getIID());
        return commonCopy(ret);
    }

    @Override
    void dealWithClockEvent(NodeEvent e) throws SpokExecutionException {
        stop();
        call();
    }

    @Override
    void dealWithNormalEvent(NodeEvent e) throws SpokExecutionException {
        if (nextEvent >= listOfEvent.size()) {
            stop();
            fireEndEvent(new EndEvent(this));
            return;
        } else {
            callEvent();
        }
        if(clockEvent == null) {
            clockEvent = startClockEvent(getDuration());
        }
    }

    private void callEvent() {
        Node e = listOfEvent.get(nextEvent);
        e.addEndEventListener(this);
        e.call();
        nextEvent++;
    }

}
