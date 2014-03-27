/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokParser;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeWait extends Node {

    // LOGGER
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeWait.class);

    /**
     *
     */
    private Node waitFor = null;

    /**
     * private constructor to allow copy
     *
     * @param p
     */
    private NodeWait(Node p) {
        super(p);
    }

    public NodeWait(JSONObject o, Node parent) throws SpokTypeException {
        super(parent);
        if (o.has("waitFor")) {
            waitFor = Builder.buildFromJSON(o.optJSONObject("waitFor"), parent);
        }
    }

    @Override
    protected void specificStop() {
        if (waitFor != null) {
            waitFor.stop();
        }
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        if (waitFor == null) {
            fireEndEvent(new EndEvent(this));
            return null;
        }
        waitFor.addEndEventListener(this);
        return waitFor.call();
    }

    @Override
    public String getExpertProgramScript() {
        if (waitFor == null) {
            return "no_op;";
        }
        return "wait (" + waitFor.getExpertProgramScript() + ")";
    }

    @Override
    protected Node copy(Node parent) {
        NodeWait ret = new NodeWait(parent);
        if (waitFor != null) {
            ret.waitFor = waitFor.copy(ret);
        }
        return ret;
    }

    @Override
    public void endEventFired(EndEvent e) {
        if (waitFor.getType().equalsIgnoreCase("number")) {
            try {
                Integer duration;
                duration = SpokParser.getNumericResult(waitFor).intValue();
                waitFor = startClockEvent(duration);
                if (waitFor == null) {
                    fireEndEvent(new EndEvent(this));
                }
            } catch (SpokTypeException ex) {
                LOGGER.error("Unable to parse numeric result: {}", ex.getMessage());
                fireEndEvent(new EndEvent(this));
            } catch (SpokExecutionException ex) {
                LOGGER.error("Unable to start clock event: {}", ex.getMessage());
                fireEndEvent(new EndEvent(this));
            }
        } else {
            fireEndEvent(new EndEvent(this));
        }
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", "wait");
            if (waitFor != null) {
                o.put("waitFor", waitFor.getJSONDescription());
            }
        } catch (JSONException e) {
            // Never happens
        }
        return o;
    }

}
