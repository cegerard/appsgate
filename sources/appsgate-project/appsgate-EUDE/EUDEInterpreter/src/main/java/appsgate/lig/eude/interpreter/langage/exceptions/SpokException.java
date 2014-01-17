/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.exceptions;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class SpokException extends Exception {

    public SpokException(String reason, Exception ex) {
        super(reason, ex);
    }
    
    /**
     *
     * @return
     */
    public JSONObject getJSON() {
        try {
            JSONObject ret = new JSONObject();
            ret.put("exceptionType", this.getClass().getSimpleName().toString());
            ret.put("message", this.getMessage());
            return ret;
        } catch (JSONException ex) {
            return new JSONObject();
        }
        
    }
}
