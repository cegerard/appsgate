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
import java.util.logging.Level;
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

    private String returnExp;
    private Node returnNode = null;
    private JSONObject returnValue = null;
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
        returnExp = obj.optString("returnExp");
    }

    @Override
    protected void specificStop() throws SpokException {
    }

    @Override
    public String getExpertProgramScript() {
        return "return " + this.returnExp + ";";
    }

    @Override
    Node copy(Node parent) {
        NodeReturn ret = new NodeReturn(parent);
        ret.returnExp = this.returnExp;
        if (returnNode != null) {
            ret.returnNode = this.returnNode.copy(parent);
        }
        ret.returnValue = new JSONObject(returnValue);
        return ret;
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
            return ex.getJSON();
        }

        if (returnNode != null) {
            try {
                returnNode.addEndEventListener(this);
                returnNode.call();

            } catch (SpokException ex) {
                return ex.getJSON();
            }
        } else {
            addEndEventListener(this);
            fireEndEvent(new EndEvent(this));
        }
        return null;
    }

    @Override
    JSONObject getJSONDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
