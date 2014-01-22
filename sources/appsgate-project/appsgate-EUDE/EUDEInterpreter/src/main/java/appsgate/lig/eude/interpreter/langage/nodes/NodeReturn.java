/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
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
     * the node to be evaluated for the return expression
     */
    private Node returnNode = null;
    /**
     * the value to return
     */
    private JSONObject returnValue = null;
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
    public NodeReturn(JSONObject obj, Node parent) throws SpokNodeException {
        super(parent);
        if (obj.has("NodeFunction")) {
            returnNode = new NodeFunction(obj.optJSONObject("NodeFunction"), this);
        }
        if (obj.has("returnValue")) {
            returnValue = obj.optJSONObject("returnValue");
        }
    }

    @Override
    protected void specificStop() throws SpokException {
    }

    @Override
    public String getExpertProgramScript() {
        String returnExp;
        if (this.returnNode != null) {
            returnExp = this.returnNode.getExpertProgramScript();
        } else {
            return returnValue.toString();
        }
        return "return " + returnExp + ";";
    }

    @Override
    protected Node copy(Node parent) {
        try {
            NodeReturn ret = new NodeReturn(parent);
            if (returnNode != null) {
                ret.returnNode = this.returnNode.copy(parent);
            }
            if (returnValue != null) {
                ret.returnValue = new JSONObject(returnValue.toString());
            }
            return ret;
        } catch (JSONException ex) {
            return null;
        }
    }

    @Override
    public JSONObject getResult() {
        return returnValue;
    }

    @Override
    public void endEventFired(EndEvent e) {
        Node source = (Node) e.getSource();
        try {
            returnValue = source.getResult();
        } catch (SpokException ex) {
            LOGGER.error("Exception raised during evaluation" + ex);
        }
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

        if (returnNode != null) {
            try {
                returnNode.addEndEventListener(this);
                returnNode.call();

            } catch (SpokException ex) {
                return ex.getJSONDescription();
            }
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
            if (returnNode != null) {
                o.put("functionNode", returnNode);
            }
            if (returnValue != null) {
                o.put("returnValue", returnValue);
            }

        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
    }

}
