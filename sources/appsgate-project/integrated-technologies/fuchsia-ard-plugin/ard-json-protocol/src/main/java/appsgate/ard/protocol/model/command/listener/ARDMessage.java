package appsgate.ard.protocol.model.command.listener;


import org.json.JSONException;
import org.json.JSONObject;

public interface ARDMessage {

    public void ardMessageReceived(JSONObject json) throws JSONException;

}
