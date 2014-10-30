/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class NodeEmpty extends Node {

    private NodeEmpty(Node p) {
        super(p);
    }

    /**
     *
     * @param p
     * @param o
     */
    public NodeEmpty(JSONObject o, Node p) {
        super(p, o);
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
        return "";
    }

    @Override
    protected Node copy(Node parent) {
        NodeEmpty n = new NodeEmpty(parent);
        return n;
    }

    @Override
    public void endEventFired(EndEvent e) {
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
        try {
            o.put("type", "empty");
        } catch (JSONException ex) {
            // will never happen
        }
        return o;

    }

    @Override
    public String getTypeSpec() {
        return "Empty";
    }
}
