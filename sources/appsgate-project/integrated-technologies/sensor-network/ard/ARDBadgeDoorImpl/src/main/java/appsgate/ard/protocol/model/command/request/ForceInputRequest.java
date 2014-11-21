package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class ForceInputRequest extends ARDRequest {

    public ForceInputRequest(Integer inputId,Boolean status,Boolean ejected) throws JSONException {
        super(0,"force_input");
        json.put("input_idx",inputId);
        json.put("status",status);

        if(ejected==null){
            json.put("ejected", JSONObject.NULL);
        }else {
            json.put("ejected", ejected);
        }

    }

}
