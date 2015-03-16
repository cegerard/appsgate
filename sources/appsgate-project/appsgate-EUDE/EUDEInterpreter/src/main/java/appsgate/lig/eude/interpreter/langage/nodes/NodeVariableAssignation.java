/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.references.ReferenceTable;
import appsgate.lig.ehmi.spec.SpokObject;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.context.dependency.graph.ReferenceDescription;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeVariableAssignation extends Node implements ICanBeEvaluated {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeVariableAssignation.class);

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
     * @throws SpokNodeException
     */
    public NodeVariableAssignation(JSONObject obj, Node p) throws SpokNodeException {
        super(p, obj);
        if (obj.has("value")) {
            try {
                value = Builder.buildFromJSON(obj.optJSONObject("value"), this);
            } catch (SpokNodeException ex) {
                LOGGER.error("Unable to build the value of the variable assignation");
                throw new SpokNodeException(this, "NodeVariableAssignation.value", ex);
            }
        }
        name = getJSONString(obj, "name");
    }

    @Override
    protected void specificStop() {
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
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
        ICanBeEvaluated source = (ICanBeEvaluated) e.getSource();
        SpokObject v = source.getResult();
        if (v != null) {
            setVariable(v);
        } else {
            setVariable(null);
        }
        fireEndEvent(new EndEvent(this));
    }

    @Override
    public String getTypeSpec() {
        return "Var " + this.name + "=" + this.value;

    }

    @Override
    public NodeValue getResult() {
        if (value == null) {
            LOGGER.error("A variable assignation should not be null");
            return null;
        }
        return ((ICanBeEvaluated) value).getResult();
    }

    @Override
    public String getResultType() {
        if (value == null) {
            LOGGER.debug("this variable is null so the type is null");
            return null;
        }
        return ((ICanBeEvaluated) value).getResultType();
    }

    /**
     * Method that set the variable to its value
     *
     * @param v the variable to set
     */
    private void setVariable(SpokObject v) {
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

    @Override
    protected void buildReferences(ReferenceTable table, ReferenceDescription d) {
        if (value != null) {
            value.buildReferences(table, null);
        }
    }
}
