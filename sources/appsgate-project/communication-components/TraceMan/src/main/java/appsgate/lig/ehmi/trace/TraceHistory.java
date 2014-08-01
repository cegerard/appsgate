/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.ehmi.trace;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public interface TraceHistory {
    Boolean init();
    void close();
    void trace(JSONObject o);
    JSONArray get(Long timestamp, Integer count);
}
