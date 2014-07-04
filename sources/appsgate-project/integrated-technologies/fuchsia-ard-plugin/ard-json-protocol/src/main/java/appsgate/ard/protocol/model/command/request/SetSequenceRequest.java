package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class SetSequenceRequest extends ARDRequest {

    public SetSequenceRequest(Integer seqId,String name) throws JSONException {
        super(0,"set_sequence");
        json.put("seq_idx",seqId);
        json.put("name",name);
    }

}
