/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeReturn extends Node {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeReturn.class.getName());

    /**
     * the value to return
     */
    private Node returnValue = null;
    /**
     * the parent function of this return node
     */
    private NodeFunction functionParent = null;

    /**
     * Constructor to copy nodes
     *
     * @param p parent node
     */
    public NodeReturn(Node p) {
        super(p);
    }

    /**
     * Constructor
     *
     * @param obj the JSON object
     * @param parent the parent node
     * @throws SpokNodeException if something is not correctly written
     */
    public NodeReturn(JSONObject obj, Node parent) throws SpokException {
        super(parent);
        if (obj.has("returnValue")) {
            returnValue = Builder.BuildNodeFromJSON(obj.optJSONObject("returnValue"), this);
        }
    }

    @Override
    protected void specificStop() {
    }

    @Override
    public String getExpertProgramScript() {
        String returnExp = "";
        if (this.returnValue != null) {
            returnExp = this.returnValue.getExpertProgramScript();
        }
        return "return " + returnExp + ";";
    }

    @Override
    protected Node copy(Node parent) {
        NodeReturn ret = new NodeReturn(parent);
        if (returnValue != null) {
            ret.returnValue = returnValue.copy(ret);
        }
        return ret;
    }

    @Override
    public SpokObject getResult() {
        return returnValue;
    }

    @Override
    public void endEventFired(EndEvent e) {
        fireEndEvent(new EndEvent(this));
    }

    @Override
    public JSONObject call() {
        setStarted(true);

        functionParent = (NodeFunction) findNode(NodeFunction.class, this);
        if (functionParent == null) {
            SpokExecutionException ex = new SpokExecutionException("Unable to find a function node to this function.");
            return ex.getJSONDescription();
        }
        addEndEventListener(functionParent);

        if (returnValue != null) {
            returnValue.addEndEventListener(this);
            returnValue.call();

        } else {
            addEndEventListener(this);
            fireEndEvent(new EndEvent(this));
        }
        return null;
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", "return");
            if (returnValue != null) {
                o.put("returnValue", returnValue.getJSONDescription());
            }

        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
    }

}
