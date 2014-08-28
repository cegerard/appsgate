package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class GetDoorRequest extends ARDRequest {

    public GetDoorRequest(Integer doorId) throws JSONException {
        super(0,"get_door");

        json.put("door_idx",doorId);
    }

}
