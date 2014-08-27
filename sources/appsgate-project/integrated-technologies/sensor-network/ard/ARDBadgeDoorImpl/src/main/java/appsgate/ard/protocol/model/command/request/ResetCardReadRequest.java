package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class ResetCardReadRequest extends ARDRequest {

    public ResetCardReadRequest() throws JSONException {
        super(0, "reset_card_read");
    }

}
