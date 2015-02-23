/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.references.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeSelectState extends Node implements INodeList, ICanBeEvaluated {

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSelectState.class.getName());

    private Node devicesToCheck;

    private String stateToCheck;

    private String stateValue;

    /**
     * the time of start given in seconds 0 means the time of the execution
     */
    private Long timeStart;

    /**
     * The duration of the select state in seconds 0, means till now must be > 0
     */
    private Long duration;

    private JSONArray devicesSelected;

    private JSONObject states;

    /**
     * private constructor to allow copy method
     *
     * @param p
     */
    private NodeSelectState(Node p) {
        super(p);
    }

    /**
     * Constructor
     *
     * @param o
     * @param parent
     * @throws SpokTypeException
     */
    public NodeSelectState(JSONObject o, Node parent) throws SpokException {
        super(parent, o);
        devicesToCheck = Builder.buildFromJSON(o.optJSONObject("devices"), this);
        if (!(devicesToCheck instanceof INodeList)) {
            LOGGER.error("The devices to check must be a list");
            throw new SpokNodeException(this, "SelectState.devices.listNeeded", null);
        }
        stateToCheck = getJSONString(o, "state");
        stateValue = getJSONString(o, "value");
        timeStart = o.optLong("start");
        if (timeStart == null) {
            timeStart = new Long(0);
        }
        duration = o.optLong("duration");
        if (duration == null) {
            duration = new Long(0);
        }
        if (duration < 0) {
            LOGGER.error("duration cannot be a negative value");
            throw new SpokNodeException(this, "SelectState.duration", null);
        }

    }

    @Override
    protected void specificStop() {
        devicesToCheck.stop();
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        devicesToCheck.addEndEventListener(this);
        return devicesToCheck.call();
    }

    @Override
    public String getExpertProgramScript() {
        return "_selectState()";
    }

    @Override
    protected Node copy(Node parent) {
        NodeSelectState s = new NodeSelectState(parent);
        s.devicesToCheck = devicesToCheck.copy(s);
        s.stateToCheck = stateToCheck;
        s.stateValue = stateValue;
        s.timeStart = timeStart;
        s.duration = duration;
        return s;
    }

    @Override
    public void endEventFired(EndEvent e) {
        LOGGER.trace("The node has been evaluated");
        try {
            List<NodeValue> result = ((INodeList) devicesToCheck).getElements();
            states = getEvents(result);
            LOGGER.debug("States: {}", states.toString());
            devicesSelected = checkStates(states, result);
            fireEndEvent(new EndEvent(this));
        } catch (SpokExecutionException ex) {
            LOGGER.error("Unable to compute what devices to check");
        }
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
        try {
            o.put("type", "selectState");
            o.put("devices", devicesToCheck.getJSONDescription());
            o.put("state", stateToCheck);
            o.put("value", stateValue);
            o.put("start", timeStart);
            o.put("duration", duration);
        } catch (JSONException ex) {
            // Exception never thrown
        }
        return o;
    }

    /**
     * Method to get the variables of a list, if the variable is not a list, it
     * returns null
     *
     * @return a list of Variable or null
     */
    @Override
    public List<NodeValue> getElements() {
        try {
            ArrayList<NodeValue> a = new ArrayList<NodeValue>();
            for (int i = 0; i < devicesSelected.length(); i++) {
                JSONObject o = devicesSelected.optJSONObject(i);
                if (o != null) {
                    a.add(new NodeValue(o, this));
                } else {
                    LOGGER.warn("An empty value has been returned for element {}", i);
                }
            }
            return a;

        } catch (SpokNodeException ex) {
            LOGGER.error("The variable was not well formed");
            return null;
        }
    }

    @Override
    public NodeValue getResult() {
        if (devicesSelected == null) {
            return null;
        }
        JSONObject o = new JSONObject();
        try {
            o.put("type", "list");
            o.put("value", devicesSelected);
        } catch (JSONException ex) {
        }
        try {
            return new NodeValue(o, this);
        } catch (SpokNodeException ex) {
            LOGGER.error("Unable to build the node value");
            return null;
        }
    }

    @Override
    public String getType() {
        return "list";
    }
    @Override
    public String getResultType() {
        return this.getType();
    }

    /**
     *
     * @param result
     * @param devices
     * @return
     */
    private JSONArray checkStates(JSONObject result, List<NodeValue> devices) {
        LOGGER.trace("check states");
        JSONArray ret = new JSONArray();
        for (NodeValue n : devices) {
            JSONArray deviceStates = result.optJSONArray(n.getValue());

            if (deviceStates == null) {
                LOGGER.warn("No states associated to device: {}", n.getValue());
                continue;
            }
            for (int i = 0; i < deviceStates.length(); i++) {
                JSONObject o;
                try {
                    o = deviceStates.getJSONObject(i);
                    if (o.getString("value").equalsIgnoreCase(this.stateValue)) {
                        ret.put(n.getJSONDescription());
                    }
                } catch (JSONException ex) {
                    LOGGER.warn("the result state were not well formed");
                }
            }
        }
        return ret;
    }

    /**
     *
     * @param result
     * @return
     * @throws SpokExecutionException
     */
    private JSONObject getEvents(List<NodeValue> result) throws SpokExecutionException {
        LOGGER.trace("get events");
        // convert the list of value to a set of ids
        Set<String> l = new HashSet<String>();
        for (NodeValue v : result) {
            if (v.getValueType() == NodeValue.TYPE.DEVICE) {
                l.add(v.getValue());
            }
        }
        Long begin = timeStart;
        if (timeStart <= 0) {
            begin = getMediator().getTime() + timeStart * 1000;
        }

        return getEvents(l, stateToCheck, begin, begin + duration * 1000);
    }

    @Override
    public String getTypeSpec() {
        return "SelectSate";
    }
    @Override
    protected void buildReferences(ReferenceTable table, HashMap<String,String> args) {
        // TODO implement reference for select state
    }

}
