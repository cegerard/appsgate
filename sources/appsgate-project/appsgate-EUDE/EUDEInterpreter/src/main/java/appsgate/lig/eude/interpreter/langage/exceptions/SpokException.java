package appsgate.lig.eude.interpreter.langage.exceptions;

import appsgate.lig.ehmi.spec.SpokObject;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class SpokException extends Exception implements SpokObject {

    /**
     * Constructor
     *
     * @param reason
     * @param ex
     */
    public SpokException(String reason, Exception ex) {
        super(reason, ex);
    }

    /**
     * @return
     */
    @Override
    public JSONObject getJSONDescription() {
        JSONObject ret = new JSONObject();
        try {
            ret.put("exceptionType", this.getClass().getSimpleName().toString());
            ret.put("message", this.getMessage());
        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return ret;

    }

    @Override
    public String getType() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    public String getValue() {
        return null;
    }
}
