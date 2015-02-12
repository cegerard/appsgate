package appsgate.ard.protocol.adaptor.callbacks;

import appsgate.ard.protocol.adaptor.ARDBadgeDoor;
import appsgate.ard.protocol.model.command.listener.ARDMessage;
import appsgate.lig.ard.badge.door.messages.ARDBadgeDoorContactNotificationMsg;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adele on 11/02/15.
 */
public class AlarmCallback implements ARDMessage {

    private ARDBadgeDoor door;

    public AlarmCallback(ARDBadgeDoor door){
        this.door=door;
    }

    @Override
    public void ardMessageReceived(JSONObject json) throws JSONException {
        door.triggerApamMessage(new ARDBadgeDoorContactNotificationMsg("alarmFired", "false", "true", door));
    }
}
