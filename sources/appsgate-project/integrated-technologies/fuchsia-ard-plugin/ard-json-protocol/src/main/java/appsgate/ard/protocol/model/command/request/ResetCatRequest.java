package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class ResetCatRequest extends ARDRequest {

    public ResetCatRequest(Integer catId) throws JSONException {
        super(0,"reset_cat");
        json.put("cat_idx",catId);
    }

}
