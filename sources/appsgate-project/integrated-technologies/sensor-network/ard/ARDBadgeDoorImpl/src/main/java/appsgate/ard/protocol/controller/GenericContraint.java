package appsgate.ard.protocol.controller;

import appsgate.ard.protocol.model.Constraint;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This constraint is created for every Door to restrict every message only to the door that they correspond to
 */
public class GenericContraint implements Constraint {

    private Integer doorIdx;

    public GenericContraint(Integer doorIdx){
        this.doorIdx=doorIdx;
    }

    @Override
    public boolean evaluate(JSONObject jsonObject) throws JSONException {
        return jsonObject.getJSONObject("event").getString("class").equals("card") && jsonObject.getJSONObject("event").getInt("door_idx") == doorIdx;
    }

}
