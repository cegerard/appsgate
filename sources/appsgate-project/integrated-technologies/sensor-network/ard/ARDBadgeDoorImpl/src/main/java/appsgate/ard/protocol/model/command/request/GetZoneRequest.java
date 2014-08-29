package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class GetZoneRequest extends ARDRequest {

    public GetZoneRequest(Integer inputIndex) throws JSONException {
        super(0,"get_zone");
        json.put("input_idx",inputIndex);
    }

}
