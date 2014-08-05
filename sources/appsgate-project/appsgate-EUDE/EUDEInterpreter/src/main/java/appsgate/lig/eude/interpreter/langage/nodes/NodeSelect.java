package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeSelect extends Node implements INodeList, ICanBeEvaluated {

    /**
     * Static class member uses to log what happened in each instances
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSelect.class);

    private JSONArray what;
    private JSONArray where;
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
        super(parent, o);
        what = o.optJSONArray("what");
        if (what == null) {
            what = new JSONArray();
        }
        where = o.optJSONArray("where");
        if (where == null) {
            where = new JSONArray();
        }
    }

    @Override
    protected void specificStop() {
        // No sub nodes to stop
    }

    @Override
    public String getExpertProgramScript() {
        return "SELECT LANGUAGE NOT IMPLEMENTED YET";
    }

    @Override
    public void endEventFired(EndEvent e) {
        // This node does not wait for any other node
    }

    @Override
    protected Node copy(Node parent) {
        NodeSelect ret = new NodeSelect(parent);
        try {
            ret.what = new JSONArray(what.toString());
            ret.where = new JSONArray(where.toString());
        } catch (JSONException ex) {
        }
        return ret;
    }

    @Override
    public NodeValue getResult() throws SpokExecutionException {
        if (specificDevices == null) {
            return null;
        }
        JSONObject o = new JSONObject();
        try {
            o.put("type", "list");
            o.put("value", specificDevices);
        } catch (JSONException ex) {
        }
        try {
            return new NodeValue(o, this);
        } catch (SpokNodeException ex) {
            throw new SpokExecutionException("Unable to build the node value");
        }
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        try {
            specificDevices = getDevicesInSpaces(what, where);
        } catch (SpokExecutionException ex) {
            return ex.getJSONDescription();
        }
        fireEndEvent(new EndEvent(this));
        return null;
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
        try {
            o.put("type", "select");
            o.put("what", what);
            o.put("where", where);
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
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
            for (int i = 0; i < specificDevices.length(); i++) {
                JSONObject o = specificDevices.optJSONObject(i);
                if (o != null) {
                    a.add(new NodeValue(o, this));
                } else {
                    LOGGER.warn("An empty value has been returned for element {}", i);
                }
            }
            return a;

        } catch (SpokNodeException ex) {
            LOGGER.error("The variable was not well formed");
            return null;
        }
    }

    @Override
    public String getType() {
        return "list";
    }
    @Override
    public String getResultType() {
        return this.getType();
    }
    @Override
    protected void buildReferences(ReferenceTable table) {
        // For now, do nothing
        // TODO: implement reference for select
    }

}
