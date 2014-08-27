package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class ResetZoneRequest extends ARDRequest {

    public ResetZoneRequest(Integer zoneIndex) throws JSONException {
        super(0,"reset_zone");
        json.put("zone_idx",zoneIndex);
    }

}
