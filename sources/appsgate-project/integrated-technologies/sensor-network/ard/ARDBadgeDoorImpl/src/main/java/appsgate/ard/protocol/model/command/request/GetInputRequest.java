package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class GetInputRequest extends ARDRequest {

    public GetInputRequest(Integer inputIndex) throws JSONException {
        super(0,"get_input");
        json.put("input_idx",inputIndex);
    }

}
