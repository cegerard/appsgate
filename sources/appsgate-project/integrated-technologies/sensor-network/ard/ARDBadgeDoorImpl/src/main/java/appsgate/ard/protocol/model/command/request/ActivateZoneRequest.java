package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class ActivateZoneRequest extends ARDRequest {

    public ActivateZoneRequest(Integer inputId) throws JSONException {
        super(0,"activate_zone");
        json.put("zone_idx",inputId);
    }

}
