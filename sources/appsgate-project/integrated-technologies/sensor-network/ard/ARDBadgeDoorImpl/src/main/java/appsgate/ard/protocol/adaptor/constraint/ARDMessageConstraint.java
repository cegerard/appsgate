package appsgate.ard.protocol.adaptor.constraint;

import appsgate.ard.protocol.model.Constraint;
import appsgate.lig.ard.badge.door.messages.ARDBadgeDoorContactNotificationMsg;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adele on 11/02/15.
 */
public class ARDMessageConstraint implements Constraint {
    @Override
    public boolean evaluate(JSONObject jsonObject) throws JSONException {
        jsonObject.getJSONObject("event").getString("cause");
        return true;
    }
}
