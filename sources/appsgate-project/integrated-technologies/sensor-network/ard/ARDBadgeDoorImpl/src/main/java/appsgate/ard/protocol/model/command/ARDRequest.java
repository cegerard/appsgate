package appsgate.ard.protocol.model.command;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class ARDRequest implements JSONARDCommand {

    protected JSONObject json=new JSONObject();

    public ARDRequest(int requestId,String request) throws JSONException {
        Integer id = requestId;

        if(id==0) {
            id = RequestIdGenerator.getInstance().genId();
        }

        json.put("request_id",id);
        json.put("request",request);

    }

    public final Integer getRequestId() throws JSONException {
        return json.getInt("request_id");
    }

    public String getJSON() throws JSONException{

        return json.toString();

    }

}
