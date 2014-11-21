package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class DeactivateZoneRequest extends ARDRequest {

    public DeactivateZoneRequest(Integer inputId) throws JSONException {
        super(0,"deactivate_zone");
        json.put("zone_idx",inputId);
    }

}
