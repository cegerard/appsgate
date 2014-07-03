package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class ResetAllCardsRequest extends ARDRequest {

    public ResetAllCardsRequest() throws JSONException {
        super(0, "reset_all_cards");
    }

}
