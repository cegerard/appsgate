package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class ForceInputRequest extends ARDRequest {

    public ForceInputRequest(Integer inputId,Boolean status) throws JSONException {
        super(0,"force_input");
        json.put("input_idx",inputId);
        json.put("status",status);
    }

}
