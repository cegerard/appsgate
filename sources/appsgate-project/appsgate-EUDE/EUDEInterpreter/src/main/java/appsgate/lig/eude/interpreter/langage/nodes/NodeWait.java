/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.components.SpokParser;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
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

    private NodeEvent event;

    /**
     * private constructor to allow copy
     *
     * @param p
     */
    private NodeWait(Node p) {
        super(p);
    }

    public NodeWait(JSONObject o, Node parent) throws SpokTypeException {
        super(parent, o);
        if (o.has("waitFor")) {
            waitFor = Builder.buildFromJSON(o.optJSONObject("waitFor"), this);
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
        setProgramWaiting();
        setStarted(true);
        fireStartEvent(new StartEvent(this));
        if (waitFor == null) {
            stopWaiting();
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
        if (event != null && event.equals(e.getSource())) {
            event = null;
            stopWaiting();
            return;
        }
        if (waitFor.getType().equalsIgnoreCase("number")) {
            try {
                Integer duration;
                duration = SpokParser.getNumericResult(waitFor).intValue();
                event = startClockEvent(duration);
                if (event == null) {
                    stopWaiting();
                }
            } catch (SpokTypeException ex) {
                LOGGER.error("Unable to parse numeric result: {}", ex.getMessage());
                stopWaiting();
            } catch (SpokExecutionException ex) {
                LOGGER.error("Unable to start clock event: {}", ex.getMessage());
                stopWaiting();
            }
        } else {
            event = null;
            stopWaiting();
        }
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
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

    /**
     * Method to process the end of waiting
     */
    private void stopWaiting() {
        setStarted(false);
        setProgramProcessing();
        fireEndEvent(new EndEvent(this));
    }

    @Override
    protected void buildReferences(ReferenceTable table) {
        if (this.event != null) {
            event.buildReferences(table);
        }
        if (this.waitFor != null) {
            waitFor.buildReferences(table);
        }
    }
}
