package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class GetCatRequest extends ARDRequest {

    public GetCatRequest(Integer catId) throws JSONException {
        super(0,"get_cat");
        json.put("cat_idx",catId);
    }

}
