package appsgate.ard.protocol.adaptor.constraint;

import appsgate.ard.protocol.model.Constraint;
import org.json.JSONException;
import org.json.JSONObject;

public class CardMessageConstraint implements Constraint {

    @Override
    public boolean evaluate(JSONObject jsonObject) throws JSONException {
        jsonObject.getJSONObject("event").getInt("card_idx");
        return true;
    }
}
