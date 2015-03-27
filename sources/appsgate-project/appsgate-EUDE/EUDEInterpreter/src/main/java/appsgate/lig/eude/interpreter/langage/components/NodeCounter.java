package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.ehmi.spec.SpokObject;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public final class NodeCounter implements SpokObject {

    private HashMap<String, ArrayList<Long>> map;

    /**
     * Constructor
     */
    public NodeCounter() {
        reinit();

    }

    /**
     * Method that reinit the map
     */
    public void reinit() {
        map = new HashMap<String, ArrayList<Long>>();
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject ret = new JSONObject();

        for (String k : map.keySet()) {
            try {
                ret.put(k, new JSONArray(map.get(k)));
            } catch (JSONException ex) {
            }
        }
        return ret;
    }

    @Override
    public String getType() {
        return "NodeCounter";
    }

    @Override
    public String getValue() {
        return "[NC: " + this.getCount().toString() + " ]";
    }

    /**
     *
     * @return
     */
    public Integer getCount() {
        Integer total = 0;
        for (ArrayList<Long> l : map.values()) {
            total += l.size();
        }
        return total;
    }

    /**
     *
     * @param nodeid
     * @return
     */
    public Integer getCount(String nodeid) {
        if (map.containsKey(nodeid)) {
            return map.get(nodeid).size();
        }
        return 0;
    }

    /**
     *
     * @param nodeId
     * @param timestamp
     */
    public void incrementNodeCounter(String nodeId, Long timestamp) {
        if (nodeId == null) {
            return;
        }
        ArrayList<Long> array;
        if (map.containsKey(nodeId)) {
            array = map.get(nodeId);
        } else {
            array = new ArrayList<Long>();
        }
        array.add(timestamp);
        map.put(nodeId, array);
    }

}
