package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class GetSequenceRequest extends ARDRequest {

    public GetSequenceRequest(Integer seqId) throws JSONException {
        super(0,"get_sequence");
        json.put("seq_idx",seqId);
    }

}
