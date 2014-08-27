package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class GetCardRequest extends ARDRequest {

    public GetCardRequest(Integer cardId) throws JSONException {
        super(0, "get_card");
        json.put("card_idx",cardId);
    }

}
