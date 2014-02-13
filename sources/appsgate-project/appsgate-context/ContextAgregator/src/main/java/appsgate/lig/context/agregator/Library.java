/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.context.agregator;

import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public final class Library {

    /**
     * Static class member uses to log what happened in each instances
     */
    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Library.class);

    /**
     *
     */
    private HashMap<String, JSONObject> root;

    /**
     * Constructor
     */
    public Library() {
        root = new HashMap<String, JSONObject>();
    }

    /**
     *
     * @param o
     */
    public void addDesc(JSONObject o) {
        if (!o.has("typename")) {
            LOGGER.error("The description has no type name");
            return;
        }
        addDescForType(o.optString("typename"), o);
    }

    /**
     *
     * @param type
     * @param o
     */
    public void addDescForType(String type, JSONObject o) {
        root.put(type, o);
    }

    /**
     *
     * @param type
     * @param stateName
     * @return
     * @throws JSONException
     */
    public JSONObject getStateForType(String type, String stateName) throws JSONException {
        JSONObject desc = getDescriptionFromType(type);
        if (desc == null) {
            LOGGER.error("No description found for this type");
            return null;
        }
        JSONArray array;
        try {
            array = desc.getJSONArray("states");
        } catch (JSONException ex) {
            LOGGER.error("unable to find the states definition.");
            return null;
        }

        for (int i = 0; i < array.length(); i++) {
            if (array.getJSONObject(i).getString("name").equalsIgnoreCase(stateName)) {
                return array.getJSONObject(i);
            }
        }
        LOGGER.error("State not found: {}", stateName);
        return null;
    }

    /**
     * 
     * @param type
     * @return 
     */
    public JSONObject getDescriptionFromType(String type) {

        if (root == null) {
            LOGGER.error("The library is not inited");
            return null;
        }
        if (root.containsKey(type)) {
            return root.get(type);
        } 
        LOGGER.error("type [{}] not found in library", type);
        return null;

    }

}
