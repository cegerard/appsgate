/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeEventsAnd extends NodeEvents {

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventsAnd.class);

    /**
     * 
     */
    private int nbEventEnded = 0;

    private NodeEvent clockEvent = null;
    
    /**
     * private constructor to allow copy
     *
     * @param parent
     */
    private NodeEventsAnd(Node parent) {
        super(parent);
    }

    /**
     * Constructor
     *
     * @param o, the json description of the node
     * @param parent the parent node
     * @throws SpokNodeException if something is wrong in the description
     */
    public NodeEventsAnd(JSONObject o, Node parent) throws SpokNodeException {
        super(o, parent);
    }

    @Override
    protected void specificCall() {
        nbEventEnded = 0;
        clockEvent = null;
        super.specificCall();
    }

    @Override
    JSONObject specificDesc(JSONObject ret) throws JSONException {
        ret.put("type", "eventsAnd");
        return ret;
    }

    @Override
    public String getExpertProgramScript() {
        return "[" + StringUtils.join(listOfEvent, " AND ") + "]";
    }

    @Override
    protected Node copy(Node parent) {
        NodeEventsAnd ret = new NodeEventsAnd(parent);
        return commonCopy(ret);
    }

    @Override
    void dealWithClockEvent(NodeEvent e) throws SpokExecutionException {
        stop();
        call();
    }

    @Override
    void dealWithNormalEvent(NodeEvent e) throws SpokExecutionException {
        nbEventEnded++;
        if (nbEventEnded >= listOfEvent.size()) {
            LOGGER.debug("All the events have been ended");
            stop();
            fireEndEvent(new EndEvent(this));
            return;
        }
        if (clockEvent == null) {
            clockEvent = startClockEvent();
        }
    }

}
