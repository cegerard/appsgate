package appsgate.lig.ehmi.trace;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class Traces {

    /**
     *
     */
    private final HashMap<String, Set<String>> entitiesByLocation;
    /**
     *
     */
    private final HashMap<String, Set<String>> entitiesByType;

    /**
     * Default constructor
     */
    public Traces() {
        this.entitiesByLocation = new HashMap<>();
        this.entitiesByType = new HashMap<>();
    }

    /**
     *
     * @param o a trace object
     */
    public void addEntity(JSONObject o) {
        addEntity(o.optString("id"), o.optJSONObject("location"), o.optString("type"));
    }

    /**
     *
     * @param id
     * @param location
     * @param type
     */
    public void addEntity(String id, JSONObject location, String type) {
        if (location != null) {
            String loc_id = location.optString("id");
            if (entitiesByLocation.containsKey(loc_id)) {
                entitiesByLocation.get(loc_id).add(id);
            } else {
                Set<String> s = new ConcurrentSkipListSet<>();
                s.add(id);
                entitiesByLocation.put(loc_id, s);
            }
        }
        if (type == null || type.isEmpty()) {
            // if not a device, then it is a program
            type = "programs";
        }
        if (entitiesByType.containsKey(type)) {
            entitiesByType.get(type).add(id);
        } else {
            Set<String> s = new ConcurrentSkipListSet<>();
            s.add(id);
            entitiesByType.put(type, s);
        }
    }

    /**
     *
     * @return the types of this traces
     */
    public Set<String> getTypes() {
        return entitiesByType.keySet();
    }

    /**
     *
     * @return the locations of this traces
     */
    public Set<String> getLocations() {
        return entitiesByLocation.keySet();
    }

    /**
     * @param location
     * @return the device contained by this location
     */
    public Set<String> getEntitiesByLocation(String location) {
        return entitiesByLocation.get(location);
    }

    /**
     * @param type
     * @return the list of devices of this tye
     */
    public Set<String> getEntitiesByType(String type) {
        return entitiesByType.get(type);
    }

    /**
     *
     * @return
     */
    public HashMap<String, GroupTuple> getGroupsByType() {
        HashMap<String, GroupTuple> groupFollower = new HashMap<>();
        for (String t : getTypes()) {
            int order = 2;
            if (t.equalsIgnoreCase("programs")) {
                order = 4;
            }
            groupFollower.put(t, new GroupTuple(order, new JSONArray(getEntitiesByType(t))));
        }
        return groupFollower;
    }

    /**
     * 
     * @return the list of ids contains by the traces
     */
    Set<String> getIds() {
        Set<String> ret = new ConcurrentSkipListSet<>();
        for (Set<String> s : entitiesByType.values()){
            ret.addAll(s);
        }
        return ret;
    }

}
