package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class SetDoorRequest extends ARDRequest {

    public SetDoorRequest(Integer doorId, String doorName) throws JSONException {
        super(0,"set_door");
        json.put("door_idx",doorId);
    }

}
