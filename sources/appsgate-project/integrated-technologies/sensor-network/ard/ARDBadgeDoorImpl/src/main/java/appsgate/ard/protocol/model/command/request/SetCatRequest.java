package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

import java.util.Collection;

public class SetCatRequest extends ARDRequest {

    public SetCatRequest(Integer catId, String name,Collection<Integer> doors) throws JSONException {
        super(0,"set_cat");
        json.put("cat_idx",catId);
        json.put("name",catId);
        json.put("doors",doors);
    }

}
