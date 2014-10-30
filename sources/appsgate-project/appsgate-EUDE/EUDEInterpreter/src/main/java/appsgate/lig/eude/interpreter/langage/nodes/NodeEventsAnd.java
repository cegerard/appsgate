/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
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

    private HashMap<NodeEvent, NodeEvent> clockEventList;

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
        clockEventList = new HashMap<NodeEvent, NodeEvent>();
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
        LOGGER.trace("clock event {}", e);
        for (NodeEvent k : clockEventList.keySet()) {
            if (clockEventList.get(k).equals(e)) {
                LOGGER.trace("Removing key");
                nbEventEnded--;
                clockEventList.remove(k);
                return;
            }
        }
        LOGGER.warn("The event has not been catched : {}", e);
    }

    @Override
    void dealWithNormalEvent(NodeEvent e) throws SpokExecutionException {

        if (clockEventList.containsKey(e)) {
            if (clockEventList.get(e) != null) {
                clockEventList.get(e).stop();
            }
        } else {
            nbEventEnded++;
        }
        if (nbEventEnded >= listOfEvent.size()) {
            LOGGER.debug("All the events have been ended");
            fireEndEvent(new EndEvent(this));
            stop();
            return;
        }
        clockEventList.put(e, startClockEvent(duration));
    }

    @Override
    public String getTypeSpec() {
        return "Events AND";
    }
}
