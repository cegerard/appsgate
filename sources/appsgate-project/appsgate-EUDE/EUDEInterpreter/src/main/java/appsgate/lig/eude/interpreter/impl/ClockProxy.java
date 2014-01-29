/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.eude.interpreter.impl;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
    public class ClockProxy {
        
        private String id;

        public ClockProxy(JSONObject o) throws JSONException {
            this.id = o.getString("id");
        }

        public String getId() {
            return this.id;
        }
        
    }
