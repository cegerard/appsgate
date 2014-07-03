package appsgate.ard.protocol.model;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * This interface define a constraint that evaluates if the json represents an object that we intent to use
 */
public interface Constraint {

    public boolean evaluate(JSONObject jsonObject) throws JSONException;

}
