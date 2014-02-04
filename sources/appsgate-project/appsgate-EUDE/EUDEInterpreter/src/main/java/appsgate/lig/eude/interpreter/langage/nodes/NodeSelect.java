package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.agregator.spec.ContextAgregatorSpec;
import appsgate.lig.eude.interpreter.impl.EUDEMediator;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokVariable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.main.spec.AppsGateSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class NodeSelect extends Node {

    private JSONArray what;
    private JSONArray where;
    private JSONArray state;
    private JSONArray specificDevices;

    /**
     * private constructor to allow copy
     *
     * @param p
     */
    private NodeSelect(Node p) {
        super(p);
    }

    /**
     * Constructor
     * 
     * @param o
     * @param parent 
     */
    public NodeSelect(JSONObject o, Node parent) {
        super(parent);
        what = o.optJSONArray("what");
        if (what == null) {
            what = new JSONArray();
        }
        where = o.optJSONArray("where");
        if (where == null) {
            where = new JSONArray();
        }
        state = o.optJSONArray("state");
        if (state == null) {
            state = new JSONArray();
        }
    }

    @Override
    protected void specificStop() {
    }

    @Override
    public String getExpertProgramScript() {
        return "SELECT LANGUAGE NOT IMPLEMENTED YET";
    }

    @Override
    public void endEventFired(EndEvent e) {
    }

    @Override
    protected Node copy(Node parent) {
        NodeSelect ret = new NodeSelect(parent);
        try {
            ret.what = new JSONArray(what.toString());
            ret.where = new JSONArray(where.toString());
            ret.state = new JSONArray(state.toString());
        } catch (JSONException ex) {
        }
        return ret;
    }

    @Override
    public SpokVariable getResult() throws SpokException {
        if (specificDevices == null) {
            return null;
        }
        JSONObject o = new JSONObject();
        try {
            o.put("type", "list");
            o.put("value", specificDevices);
        } catch (JSONException ex) {
        }
        return new SpokVariable(o);
    }

    @Override
    public JSONObject call() {
        EUDEMediator mediator;
        try {
            mediator = getMediator();
        } catch (SpokExecutionException ex) {
            return ex.getJSONDescription();
        }
        // TODO: fix this
        ContextAgregatorSpec ctxt = mediator.getContext();
        specificDevices = ctxt.getDevicesInSpaces(what, where);
        fireEndEvent(new EndEvent(this));
        return null;
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", "select");
            o.put("what", what);
            o.put("where", where);
            o.put("state", state);
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
    }

}
