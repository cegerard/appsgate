package appsgate.ard.protocol.adaptor.constraint;

import appsgate.ard.protocol.model.Constraint;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adele on 11/02/15.
 */
public class AlarmConstraint implements Constraint {
    @Override
    public boolean evaluate(JSONObject jsonObject) throws JSONException {
        try {
            Boolean alarm=jsonObject.getJSONObject("event").getBoolean("alarm");
            Boolean active=jsonObject.getJSONObject("event").getBoolean("active");
            return alarm && active;
        } catch (JSONException e){
            return false;
        }
    }
}
