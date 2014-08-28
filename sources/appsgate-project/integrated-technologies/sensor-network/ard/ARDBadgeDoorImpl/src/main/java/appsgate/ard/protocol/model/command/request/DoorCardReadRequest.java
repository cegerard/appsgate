package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class DoorCardReadRequest extends ARDRequest {

    public DoorCardReadRequest(Integer doorId) throws JSONException {
        super(0,"door_card_read");
        json.put("door_idx",doorId);
    }

}
