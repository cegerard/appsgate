/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.components.SpokVariable;
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
public class NodeVariableAssignation extends Node {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeVariableAssignation.class.getName());

    /**
     * The name of the variable
     */
    private String name;
    /**
     * The value of the variable
     */
    private Node value = null;

    /**
     * Default Constructor
     *
     * @param p
     */
    private NodeVariableAssignation(Node p) {
        super(p);
    }

    /**
     * Main constructor
     *
     * @param obj the json description
     * @param p the parent
     * @throws SpokException
     */
    public NodeVariableAssignation(JSONObject obj, Node p) throws SpokException {
        super(p);
        if (obj.has("value")) {
            value = NodeBuilder.BuildNodeFromJSON(obj.optJSONObject("value"), this);
        }
        name = getJSONString(obj, "name");
    }
    
    @Override
    protected void specificStop() throws SpokException {
    }
    
    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", "assignation");
            if (value != null) {
                o.put("value", value.getJSONDescription());
            }
            o.put("name", name);
            
        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
        
    }
    
    @Override
    public String getExpertProgramScript() {
        if (this.value != null) {
            return this.name + " = " + this.value.getExpertProgramScript() + ";";
        } else {
            return this.name + " = UNDEFINED ;";
        }
    }
    
    @Override
    protected Node copy(Node parent) {
        NodeVariableAssignation ret = new NodeVariableAssignation(parent);
        ret.name = name;
        if (value != null) {
            ret.value = value.copy(ret);
        }
        return ret;
    }
    
    @Override
    public JSONObject call() {
        setStarted(true);
        if (value != null) {
            value.addEndEventListener(this);
            value.call();
        }
        return null;
    }
    
    @Override
    public void endEventFired(EndEvent e) {
        Node source = (Node) e.getSource();
        try {
            SpokObject v = source.getResult();
            if (v != null) {
                setVariable(new SpokVariable(v.getJSONDescription()));
            } else {
                setVariable(null);
            }
        } catch (SpokException ex) {
            LOGGER.error("Exception raised during evaluation" + ex);
        }
        fireEndEvent(new EndEvent(this));
    }
    
    @Override
    public String toString() {
        return "[Var " + this.name + "=" + this.value + "]";
        
    }
    
    @Override
    public SpokObject getResult() throws SpokException {
        if (value == null) {
            throw new SpokExecutionException("A variable assignation should not be null");
        }
        return value.getResult();
    }

    /**
     * Method that set the variable to its value
     *
     * @param v the variable to set
     */
    private void setVariable(SpokVariable v) {
        Node findNode = findNode(NodeFunction.class, this);
        if (findNode == null) {
            findNode = findNode(NodeProgram.class, this);
        }
        if (findNode == null) {
            LOGGER.warn("Unable to find a bloc to assign this variable ({})", this.name);
        } else {
            LOGGER.trace("variable assigned: " + this.name + " to " + v.getValue());
            findNode.setVariable(this.name, v);
        }
    }
    
}
