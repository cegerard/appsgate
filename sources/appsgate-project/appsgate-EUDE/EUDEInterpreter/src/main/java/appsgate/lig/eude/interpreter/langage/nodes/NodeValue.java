/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class NodeValue extends Node {

    private String type;
    private String value;

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
     */
    public NodeValue(JSONObject o, Node parent) {
        super(parent);
        type = o.optString("type");
        value = o.optString("value");
    }

    /**
     *
     * @param t
     * @param v
     * @param parent
     */
    public NodeValue(String t, String v, Node parent) {
        super(parent);
        this.type = t;
        this.value = v;
    }

    @Override
    protected void specificStop() {
    }

    @Override
    public JSONObject call() {
        fireEndEvent(new EndEvent(this));
        return null;
    }

    @Override
    public String getExpertProgramScript() {
        return getJSONDescription().toString();
    }

    @Override
    protected Node copy(Node parent) {
        NodeValue ret = new NodeValue(parent);
        ret.type = this.type;
        ret.value = this.value;
        return ret;
    }

    @Override
    public void endEventFired(EndEvent e) {
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", this.type);
            o.put("value", this.value);
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
        return type;
    }

    @Override
    public SpokObject getResult() {
        return this;
    }
    
    @Override
    public String toString() {
        return "[NodeValue type: " + type + ", value: " + value + "]";
    }

}
