/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.context.dependency.spec;

import java.util.ArrayList;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public interface Selector {
    public Boolean isEmptySelection();
    public JSONObject getJSONDescription();
    public Map<String, ArrayList<String>> getPlaceDeviceSelector();
}
