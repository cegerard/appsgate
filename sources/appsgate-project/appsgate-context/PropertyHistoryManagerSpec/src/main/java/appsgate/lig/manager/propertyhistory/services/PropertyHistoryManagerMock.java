/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.manager.propertyhistory.services;

import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class PropertyHistoryManagerMock implements PropertyHistoryManager {

    @Override
    public String getDevicesStatesHistoryAsString(Set<String> devicesID, String propertyName, long time_start, long time_end) {
        return getDevicesStatesHistoryAsJSON(devicesID, propertyName, time_start, time_end).toString();
    }

    @Override
    public JSONObject getDevicesStatesHistoryAsJSON(Set<String> devicesID, String propertyName, long time_start, long time_end) {
        JSONObject ret = new JSONObject();
        JSONArray a = new JSONArray();
        JSONObject measure = new JSONObject();
        try {
            measure.put("time", time_start - 1);
            measure.put("value", true);
        } catch (JSONException ex) {
        }
        a.put(measure);
        for (String s : devicesID) {
            try {
                ret.put(s, a);
            } catch (JSONException ex) {
            }
        }
        return ret;
    }

}
