/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class NodeValue extends Node {

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

    /**
     *
     */
    private JSONArray list = null;

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
        super(parent);
        type = TYPE.valueOf(getJSONString(o, "type").toUpperCase());
        switch (type) {
            case DEVICE:
            case LIST:
            case PROGRAMCALL:
            case VARIABLE:
                value = this.getJSONString(o, "id");
                break;
            default:
                value = this.getJSONString(o, "value");
                break;
        }
        if (o.has("list")) {
            list = getJSONArray(o, "list");
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
            default:
                return value;
        }
    }

    @Override
    protected Node copy(Node parent) {
        NodeValue ret = new NodeValue(parent);
        ret.type = this.type;
        ret.value = this.value;
        ret.list = this.list;
        return ret;
    }

    @Override
    public void endEventFired(EndEvent e) {
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", getType());
            switch (type) {
                case DEVICE:
                case LIST:
                case PROGRAMCALL:
                case VARIABLE:
                    o.put("id", this.value);
                    break;
                default:
                    o.put("value", this.value);
                    break;
            }
            if (list != null) {
                o.put("list", list);
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

    @Override
    public String getType() {
        return type.toString().toLowerCase();
    }
    
    public TYPE getValueType() {
        return type;
    }

    @Override
    public SpokObject getResult() {
        return this;
    }

    @Override
    public String toString() {
        return "[NodeValue type: " + getType() + ", value: " + getValue() + "]";
    }

}
