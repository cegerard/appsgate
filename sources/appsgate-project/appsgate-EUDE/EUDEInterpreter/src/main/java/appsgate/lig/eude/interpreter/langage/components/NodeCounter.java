package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.ehmi.spec.SpokObject;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public final class NodeCounter implements SpokObject {

    private HashMap<String, Pair> map;

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
        map = new HashMap<>();

    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject ret = new JSONObject();

        for (String k : map.keySet()) {
            try {
                ret.put(k, map.get(k).getJSONDescription());
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
        return map.size();
    }

    /**
     *
     * @param nodeid
     * @return
     */
    public Integer getCount(String nodeid) {
        if (map.containsKey(nodeid)) {
            return map.get(nodeid).getCount();
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
        Pair array;
        if (map.containsKey(nodeId)) {
            array = map.get(nodeId);
            array.increment(timestamp);
        } else {
            array = new Pair(timestamp);
        }
        map.put(nodeId, array);
    }

    private class Pair {

        private int count = 0;
        private Long timestamp;

        /**
         * Constructor
         * @param t 
         */
        private Pair(Long t) {
            count = 1;
            timestamp = t;
        }
        
        /**
         * increment the pair with a new timestamp
         * @param t 
         */
        private void increment(Long t) {
            timestamp = t;
            count++;
        }
        
        /**
         *
         * @return the count of this node
         */
        private Integer getCount() {
            return count;
        }

        /**
         *
         * @return
         */
        private JSONObject getJSONDescription() {
            JSONObject o = new JSONObject();
            try {
                o.put("c", count);
                o.put("t", timestamp);
            } catch (JSONException ex) {
                // never thrown
            }
            return o;
        }

    }
}
