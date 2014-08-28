package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class GetConfTTRequest extends ARDRequest {

    public GetConfTTRequest() throws JSONException {
        super(0, "get_conf_tt");
    }

}
