package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class StartSequenceRequest extends ARDRequest {

    public StartSequenceRequest(Integer seqId) throws JSONException {
        super(0,"start_sequence");
        json.put("seq_idx",seqId);
    }

}
