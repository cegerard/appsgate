package appsgate.lig.eude.interpreter.impl;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class ClockProxy {

    /**
     * 
     */
    private final String id;

    public ClockProxy(JSONObject o) throws JSONException {
        this.id = o.getString("id");
    }

    public String getId() {
        return this.id;
    }

}
