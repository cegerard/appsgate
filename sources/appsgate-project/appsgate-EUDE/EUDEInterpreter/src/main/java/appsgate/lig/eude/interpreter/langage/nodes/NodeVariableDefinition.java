package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokSymbolTableException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeVariableDefinition extends Node implements INodeList {

    // Logger
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NodeVariableDefinition.class);

    private String id = null;
    private SpokObject value = null;

    /**
     *
     * @param parent
     */
    private NodeVariableDefinition(Node parent) {
        super(parent);
    }

    /**
     * Constructor
     *
     * @param i the id
     * @param parent
     */
    public NodeVariableDefinition(String i, Node parent) {
        super(parent);
        this.id = i;
        this.value = null;
    }

    /**
     *
     * @param obj
     * @param parent
     * @throws SpokNodeException
     */
    public NodeVariableDefinition(JSONObject obj, Node parent) throws SpokException {
        super(parent);
        this.id = obj.optString("var_name");
        checkVariable(obj.optJSONObject("value"));
    }

    /**
     *
     * @param v_name
     * @param jsonVariable
     * @param parent
     * @throws SpokException
     */
    public NodeVariableDefinition(String v_name, JSONObject jsonVariable, Node parent) throws SpokException {
        super(parent);
        this.id = v_name;
        checkVariable(jsonVariable);
    }

    /**
     * Return the text which is used to build a text program from a program tree
     *
     * @return
     */
    public String getExpertProgramDecl() {
        if (value == null) {
            return "{ type: " + this.getType() + ", id: " + this.id + "}";
        } else {
            return value.getJSONDescription().toString();
        }
    }

    /**
     * two variables are equals if they have the same id and type
     *
     * @param other
     * @return true if both variables are the same
     */
    public boolean equals(NodeVariableDefinition other) {
        return other.id.equals(this.id) && other.getParent() == getParent();
    }

    /**
     * @return the type of this variable
     */
    @Override
    public String getType() {
        if (value == null) {
            return "undefined";
        }
        return value.getType();
    }

    /**
     * @return
     */
    public boolean isVar() {
        return getType().equalsIgnoreCase("variable");
    }

    @Override
    public String getValue() {
        if (isUndefined()) {
            LOGGER.error("trying to access a non defined variable");
            return null;
        }
        if (!isVar()) {
            return value.getValue();
        }
        LOGGER.trace("We found a reference");
        String varName = value.getValue();
        NodeVariableDefinition element = getVariableByName(varName);
        if (element == null) {
            LOGGER.error("Var not found");
            return null;
        }
        if (element.value.getValue().equals(varName)) {
            LOGGER.error("Found a circular reference");
        }
        return element.getValue();
    }

    /**
     * @return the name of the variable
     */
    public String getName() {
        return this.id;
    }

    /**
     * Method that check the correctness of the variable
     *
     * @throws SpokSymbolTableException
     */
    private void checkVariable(JSONObject obj) throws SpokException {
        if (this.id == null || this.id.isEmpty()) {
            throw new SpokNodeException("Variable", "id/name", null);
        }
        if (obj == null) {
            LOGGER.debug("Declaring a non inited variable");
            return;
        }
        if (obj.optString("type").equalsIgnoreCase("variabledefinition") || obj.optString("type").equalsIgnoreCase("variable")) {
            throw new SpokSymbolTableException("Trying to define a variable inside a variable", null);
        }
        this.value = Builder.buildFromJSON(obj, this);
    }

    /**
     * @return the json description of the variable
     */
    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", "variableDefinition");
            o.put("var_name", id);
            if (!isUndefined()) {
                o.put("value", value.getJSONDescription());
            }

        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
    }

    @Override
    public String toString() {
        if (isUndefined()) {
            return "[var -> " + this.id + " (UNDEF)]";

        } else {
            return "[var " + this.id + "[" + getType() + "]: " + value.toString() + "]";
        }
    }

    @Override
    protected void specificStop() {
        Node n = (Node) getNodeValue();
        if (n != null) {
            n.stop();
        }
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        Node n = (Node) getNodeValue();
        if (n != null) {
            n.addEndEventListener(this);
            return n.call();
        }
        // This is not a node value
        if (value != null) {
            LOGGER.debug("call a variable on a non node value");
            fireEndEvent(new EndEvent(this));
            return null;
        }
        LOGGER.error("Trying to call a non affected variable");
        SpokExecutionException e = new SpokExecutionException("Trying to call a non affected variable: " + getName());
        return e.getJSONDescription();
    }

    @Override
    public String getExpertProgramScript() {
        return "";
    }

    @Override
    protected Node copy(Node parent) {
        NodeVariableDefinition node = new NodeVariableDefinition(parent);
        node.id = this.id;
        Node n = (Node) getNodeValue();
        if (n != null) {
            node.value = n.copy(parent);
        }
        node.value = this.value;
        return node;
    }

    @Override
    public void endEventFired(EndEvent e) {
        fireEndEvent(new EndEvent(this));
    }

    /**
     *
     * @return a pointer to the Node value if this is a Node object, null
     * otherwise
     */
    public INodeList getNodeValue() {
        if (value != null && value instanceof INodeList) {
            return (INodeList) value;
        }
        return null;
    }

    @Override
    public List<NodeValue> getElements() {
        INodeList n = getNodeValue();
        if (n != null) {
            return n.getElements();
        }
        return new ArrayList<NodeValue>();
    }

    /**
     *
     * @param o the value to set
     */
    public void setValue(SpokObject o) {
        this.value = o;
    }

    private boolean isUndefined() {
        return value == null;
    }

}
