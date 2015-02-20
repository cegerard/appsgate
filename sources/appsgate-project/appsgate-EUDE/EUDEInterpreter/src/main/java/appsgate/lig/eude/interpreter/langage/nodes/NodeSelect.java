package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public NodeValue getResult() {
        if (specificDevices == null) {
            specificDevices = getDevicesInSpaces(what, where);
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
            LOGGER.error("Unable to build the node value");
            return null;
        }
    }

    /**
     *
     * @param what
     * @param where
     * @return
     */
    private JSONArray getDevicesInSpaces(JSONArray what, JSONArray where) {
        ArrayList<String> WHAT = getStringList(what);

        ArrayList<String> WHERE = getStringList(where);
        JSONArray retArray = new JSONArray();
        ArrayList<String> devicesInSpaces;
        try {
            devicesInSpaces = getMediator().getContext().getDevicesInSpaces(WHAT, WHERE);
            for (String name : devicesInSpaces) {
                NodeValue n = new NodeValue("device", name, this);
                retArray.put(n.getJSONDescription());
            }
        } catch (SpokExecutionException ex) {
            LOGGER.warn("Unable to get devices in space");
        }
        return retArray;
    }

    /**
     * return the list of string value corresponding to a list of JSON
     * description of nodes.
     *
     * @param what the JSONArray containing
     * @return an array list of string
     */
    private ArrayList<String> getStringList(JSONArray what) {
        ArrayList<String> WHAT = new ArrayList<String>();

        for (int i = 0; i < what.length(); i++) {
            JSONObject o = what.optJSONObject(i);
            if (o != null) {
                try {
                    Node n = Builder.buildFromJSON(o, this);
                    if (n instanceof ICanBeEvaluated) {
                        String s = ((ICanBeEvaluated) n).getResult().getValue();
                        if (!s.isEmpty()) {
                            WHAT.add(s);
                        }
                    } else {
                        LOGGER.warn("Found an unexpected node: " + n);
                    }
                } catch (SpokNodeException ex) {
                    LOGGER.error("Unable to parse the what branch of selector");
                    LOGGER.debug("Unable to parse: " + o.toString());
                }
            }
        }
        return WHAT;
    }

    public Map<String, ArrayList<String>> getPlaceDeviceSelector() {
        ArrayList<String> WHAT = getStringList(what);
        ArrayList<String> WHERE = getStringList(where);

        HashMap<String, ArrayList<String>> elements = new HashMap();

        ArrayList<String> devicesInSpaces = new ArrayList<String>();
        try {
            devicesInSpaces = getMediator().getContext().getDevicesInSpaces(WHAT, WHERE);
        } catch (SpokExecutionException ex) {
            LOGGER.warn("Unable to get devices in space");
        }
        elements.put("placeSelector", WHERE);
        elements.put("deviceSelector", devicesInSpaces);

        return elements;
    }

    /**
     * @return true if the selection is an empty one
     */
    public Boolean isEmptySelection() {
        return getDevicesInSpaces(what, where).length() == 0;
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        specificDevices = getDevicesInSpaces(what, where);
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
    protected void buildReferences(ReferenceTable table, HashMap<String,String> args) {
        table.addNodeSelect(this, args);
    }

    @Override
    public String getTypeSpec() {
        return "Select: " + what.toString() + ", from: " + where.toString();
    }

//    public List<JSONObject> getElementSelector(){
//        ArrayList<JSONObject> arrayElem = new ArrayList<JSONObject>();
//        ArrayList<JSONObject> arrayPlace= new ArrayList<JSONObject>();
//        for (int i = 0; i < where.length(); i++) {
//            
//            try {
//                JSONObject o = new JSONObject();
//                o.putOpt("place", where.optJSONObject(i));
//            } catch (JSONException ex) {
//                java.util.logging.Logger.getLogger(NodeSelect.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        
//         for (int i = 0; i < what.length(); i++) {
//            JSONObject o = new JSONObject();
//            try {
//                o.putOpt("device", what.optJSONObject(i));
//            } catch (JSONException ex) {
//                java.util.logging.Logger.getLogger(NodeSelect.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
}
