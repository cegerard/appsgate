package appsgate.lig.ehmi.tracker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class ProgramEntry extends TrackerEntry {
    
    private final String name;
    private final String state;
    /**
     *
     * @param t
     * @param id
     * @param name
     * @param status
     */
    public ProgramEntry(long t, String id, String name, String status) {
        super(t, id);
        this.name = name;
        this.state = status;
    }

    /**
     *
     * @return
     */
    @Override
    public JSONObject getJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", this.getObjectId());
            o.put("name", this.name);
            o.put("state", this.state);
            o.put("event", getEvent(
            ));

        } catch (JSONException ex) {
        }
        return o;
    }

}
