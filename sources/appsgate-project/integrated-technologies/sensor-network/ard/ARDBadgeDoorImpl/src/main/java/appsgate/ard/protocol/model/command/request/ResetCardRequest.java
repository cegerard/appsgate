package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class ResetCardRequest extends ARDRequest {

    public ResetCardRequest(Integer cardId) throws JSONException {

        super(0, "reset_card");
        json.put("card_idx",cardId);
        
    }

}
