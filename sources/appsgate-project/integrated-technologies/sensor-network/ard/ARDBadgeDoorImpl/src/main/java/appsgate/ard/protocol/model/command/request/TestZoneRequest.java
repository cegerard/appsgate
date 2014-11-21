package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class TestZoneRequest extends ARDRequest {

    public TestZoneRequest(Integer inputId) throws JSONException {
        super(0,"test_zone");
        json.put("zone_idx",inputId);
    }

}
