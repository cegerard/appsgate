package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class GetTimeRequest extends ARDRequest {

    public GetTimeRequest(int requestId) throws JSONException {
        super(requestId,"get_date_time");
    }

    public GetTimeRequest() throws JSONException {
        super(0,"get_date_time");
    }

}
