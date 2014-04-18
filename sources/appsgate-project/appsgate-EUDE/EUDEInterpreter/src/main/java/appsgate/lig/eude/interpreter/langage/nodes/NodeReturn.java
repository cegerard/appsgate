/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
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
public class NodeReturn extends Node implements INodeFunction {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeReturn.class);

    /**
     * the value to return
     */
    private Node returnValueNode = null;
    /**
     * the value to return
     */
    private INodeFunction returnValue = null;
    /**
     * the parent function of this return node
     */
    private NodeFunction functionParent = null;

    /**
     * Constructor to copy nodes
     *
     * @param p parent node
     * @param id the id of the node
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
        super(parent, obj);
        try {
            returnValueNode = Builder.buildFromJSON(obj.optJSONObject("returnValue"), this);
            if (returnValueNode instanceof INodeFunction) {
                returnValue = (INodeFunction) returnValueNode;
            } else {
                LOGGER.error("The return value is not a function and has no result");
                throw new SpokNodeException("NodeReturn", "returnValue", null);
            }
        } catch (SpokTypeException ex) {
            throw new SpokNodeException("NodeReturn", "returnValue", ex);
        }
    }

    @Override
    protected void specificStop() {
    }

    @Override
    public String getExpertProgramScript() {
        String returnExp = "";
        if (this.returnValue != null) {
            returnExp = this.returnValueNode.getExpertProgramScript();
        }
        return "return " + returnExp + ";";
    }

    @Override
    protected Node copy(Node parent) {
        NodeReturn ret = new NodeReturn(parent);
        if (returnValue != null) {
            ret.returnValueNode = returnValueNode.copy(ret);
            ret.returnValue = (INodeFunction) ret.returnValueNode;
        }
        return ret;
    }

    @Override
    public NodeValue getResult() throws SpokExecutionException {
        if (returnValue == null) {
            return null;
        }
        return returnValue.getResult();
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
            returnValueNode.addEndEventListener(this);
            returnValueNode.call();

        } else {
            addEndEventListener(this);
            fireEndEvent(new EndEvent(this));
        }
        return null;
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
        try {
            o.put("type", "return");
            if (returnValue != null) {
                o.put("returnValue", returnValueNode.getJSONDescription());
            }

        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
    }

}
