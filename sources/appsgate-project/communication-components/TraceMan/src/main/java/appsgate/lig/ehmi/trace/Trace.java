/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.ehmi.trace;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class
 * 
 * @author jr
 */
public class Trace {
    
    /**
     * Method to format a causality JSON object.
     *
     * @param type
     * @param description
     * @return the json object
     */
    public static JSONObject getJSONDecoration(String type, String cause, String source, String target, String description) {
        JSONObject causality = new JSONObject();
        try {
            causality.put("type", type);
            causality.put("causality", cause);
            causality.put("source", source);
            causality.put("target", target);
            causality.put("description", description);

        } catch (JSONException ex) {
            // Never happens
        }
        return causality;

    }
}
