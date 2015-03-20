package appsgate.lig.context.dependency.spec;

import appsgate.lig.context.dependency.graph.SelectReference;
import appsgate.lig.context.dependency.graph.DeviceReference;
import appsgate.lig.context.dependency.graph.ProgramReference;
import appsgate.lig.ehmi.spec.SpokObject;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class Dependencies implements SpokObject, Serializable {

    /**
     *
     */
    private final String id;

    //
    private final Set<String> actsOn;
    //
    private final Set<String> reads;
    //
    private final Set<String> isActed;
    //
    private final Set<String> isRead;

    /**
     *
     * @return the id of the dependency
     */
    public final String getId() {
        return id;
    }

    /**
     * @param id
     */
    public Dependencies(String id) {
        this.id = id;
        this.isRead = new HashSet<>();
        this.isActed = new HashSet<>();
        this.reads = new HashSet<>();
        this.actsOn = new HashSet<>();
    }

    /**
     *
     * @param key
     * @param jsonDep
     */
    public Dependencies(String key, JSONObject jsonDep) {
        this.id = key;
        this.isRead = getArrayFromJSON(jsonDep.optJSONArray("isRead"));
        this.isActed = getArrayFromJSON(jsonDep.optJSONArray("isActed"));
        this.reads = getArrayFromJSON(jsonDep.optJSONArray("reads"));
        this.actsOn = getArrayFromJSON(jsonDep.optJSONArray("actsOn"));
    }

    /**
     *
     * @param array
     * @return
     */
    private Set<String> getArrayFromJSON(JSONArray array) {
        Set<String> ret = new HashSet<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    ret.add(array.getString(i));
                } catch (JSONException ex) {
                }
            }
        }
        return ret;
    }

    /**
     * @param eid
     */
    public void addLinkEntityReaded(String eid) {
        this.reads.add(eid);
    }

    /**
     * @param eid
     */
    public void addLinkEntityChanged(String eid) {
        this.actsOn.add(eid);
    }

    /**
     * @param eid
     */
    public void addLinkEntityReading(String eid) {
        this.isRead.add(eid);
    }

    /**
     * @param eid
     */
    public void addLinkEntityChanging(String eid) {
        this.isActed.add(eid);
    }

    /**
     *
     * @return the list of id of the entities that are acted by the id
     */
    public Set<String> getActsOnEntities() {
        return actsOn;
    }

    /**
     *
     * @return the list of id of the entities that are read
     */
    public Set<String> getReadedEntities() {
        return reads;
    }

    /**
     *
     * @return the list of id of the entities that acts on this entity
     */
    public Set<String> getEntitiesActsOn() {
        return isActed;
    }

    /**
     *
     * @return the list of id that read this entity
     */
    public Set<String> getEntitiesRead() {
        return isRead;
    }

    /**
     *
     * @return
     */
    public Set<DeviceReference> getDevicesReferences() {
        return null;
    }

    /**
     *
     * @return
     */
    public Set<ProgramReference> getProgramsReferences() {
        return null;
    }

    /**
     *
     * @return
     */
    public Set<SelectReference> getSelectors() {
        return null;
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", getType());
            o.put("object", getId());
            o.put("actsOn", getActsOnEntities());
            o.put("reads", getReadedEntities());
            o.put("isRead", getEntitiesRead());
            o.put("isActed", getEntitiesActsOn());
        } catch (JSONException ex) {
        }
        return o;
    }

    @Override
    public String getType() {
        return "DependencyTree";
    }

    @Override
    public String getValue() {
        return "";
    }

}
