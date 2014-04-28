/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jr
 */
public class NodeValue extends Node implements INodeList, ICanBeEvaluated {

    // Logger
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NodeValue.class);

    public enum TYPE {

        DEVICE, LIST, PROGRAMCALL, VARIABLE, STRING, BOOLEAN, NUMBER;
    }

    /**
     * The type of the value
     * (device/list/programCall/variable/string/boolean/number)
     */
    private TYPE type;
    /**
     *
     */
    private String value;

    private JSONArray list;

    /**
     * private constructor to allow copy
     *
     * @param p
     */
    private NodeValue(Node p) {
        super(p);
    }

    /**
     *
     * @param o
     * @param parent
     * @throws SpokNodeException
     */
    public NodeValue(JSONObject o, Node parent) throws SpokNodeException {
        super(parent, o);
        type = TYPE.valueOf(getJSONString(o, "type").toUpperCase());
        switch (type) {
            case LIST:
                list = this.getJSONArray(o, "value");
                value = list.toString();
                break;
            default:
                value = this.getJSONString(o, "value");
                break;
        }
    }

    /**
     *
     * @param t
     * @param v
     * @param parent
     */
    public NodeValue(String t, String v, Node parent) {
        super(parent);
        this.type = TYPE.valueOf(t.toUpperCase());
        this.value = v;
    }

    @Override
    protected void specificStop() {
    }

    @Override
    public JSONObject call() {
        if (type == TYPE.VARIABLE) {
            return callVariable();
        }
        setStarted(true);
        fireEndEvent(new EndEvent(this));
        return null;
    }

    @Override
    public String getExpertProgramScript() {
        switch (type) {
            case STRING:
                return '"' + value + '"';
            case DEVICE:
                return "/" + value + "/";
            case PROGRAMCALL:
                return "|" + value + "|";
            case LIST:
                return listExpert();
            default:
                return value;
        }
    }

    @Override
    protected Node copy(Node parent) {
        NodeValue ret = new NodeValue(parent);
        ret.type = this.type;
        ret.value = this.value;
        try {
            if (list != null) {
                ret.list = new JSONArray(this.value);
            }
        } catch (JSONException ex) {
        }
        return ret;
    }

    @Override
    public void endEventFired(EndEvent e) {
        fireEndEvent(new EndEvent(this));
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
        try {
            o.put("type", getType());
            switch (type) {
                case LIST:
                    o.put("value", this.list);
                    break;
                default:
                    o.put("value", this.value);
                    break;
            }
        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
    }

    @Override
    public String getValue() {
        return value;
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
            if (list != null) {
                for (int i = 0; i < list.length(); i++) {
                    Node e = Builder.buildFromJSON(list.optJSONObject(i), this);
                    if (e instanceof NodeValue) {
                        a.add((NodeValue) e);
                    } else {
                        e.call();
                        if (e instanceof ICanBeEvaluated) {
                            a.add(((ICanBeEvaluated) e).getResult());
                        }
                    }
                }
            } else {
                a.add(this);
            }
            return a;

        } catch (SpokTypeException ex) {
            LOGGER.error("The value was not correct: {}", this.getExpertProgramScript());
            LOGGER.debug(this.getJSONDescription().toString());
            return null;
        } catch (SpokExecutionException ex) {
            LOGGER.error("Execution exception on reading value: {}", this.getExpertProgramScript());
            LOGGER.debug(this.getJSONDescription().toString());
            return null;
        }
    }

    /**
     * @return the value of the variable
     */
    public String getVariableValue() {
        if (type == TYPE.VARIABLE) {
            NodeVariableDefinition var = getVariableByName(value);
            return var.getValue();
        }
        return null;
    }

    @Override
    public String getType() {
        return type.toString().toLowerCase();
    }

    @Override
    public String getResultType() {
        return this.getType();
    }

    public TYPE getValueType() {
        return type;
    }

    @Override
    public NodeValue getResult() throws SpokExecutionException {
        if (type == TYPE.VARIABLE) {
            return getVariableByName(value).getResult();
        }
        return this;
    }

    /**
     *
     * @return
     */
    private JSONObject callVariable() {
        NodeVariableDefinition variableByName = this.getVariableByName(value);
        if (variableByName == null) {
            SpokExecutionException e = new SpokExecutionException("Variable " + value + "not found");
            return e.getJSONDescription();
        }
        variableByName.addEndEventListener(this);
        return variableByName.call();
    }

    /**
     *
     * @return the list string in the expert language
     */
    private String listExpert() {
        String[] array;
        List<NodeValue> elements = getElements();
        if (elements == null) {
            return "[]";
        }
        array = new String[elements.size()];
        int i = 0;
        for (NodeValue v : elements) {
            array[i++] = v.getExpertProgramScript();
        }

        return "[" + StringUtils.join(array, ",") + "]";

    }

    @Override
    public String toString() {
        return "[NodeValue type: " + getType() + ", value: " + getExpertProgramScript() + "]";
    }

}
