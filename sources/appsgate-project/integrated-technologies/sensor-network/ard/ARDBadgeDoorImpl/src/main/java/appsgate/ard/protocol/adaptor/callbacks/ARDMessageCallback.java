package appsgate.ard.protocol.adaptor.callbacks;

import appsgate.ard.protocol.adaptor.ARDBadgeDoor;
import appsgate.ard.protocol.model.command.listener.ARDMessage;
import appsgate.lig.ard.badge.door.messages.ARDBadgeDoorContactNotificationMsg;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by adele on 11/02/15.
 */
public class ARDMessageCallback implements ARDMessage {
    private static Logger logger = LoggerFactory.getLogger(ARDMessageCallback.class);
    private ARDBadgeDoor door;
    private String oldvalue;

    public ARDMessageCallback(ARDBadgeDoor door,String oldvalue){
        this.door=door;
        this.oldvalue=oldvalue;
    }

    @Override
    public void ardMessageReceived(JSONObject json) throws JSONException {
        JSONObject eventNode=json.getJSONObject("event");
        String newLastMessage="";
        try {
            newLastMessage=eventNode.getString("cause");
        }catch(JSONException e){
            logger.warn("No cause received.",e);
        }finally {
            door.triggerApamMessage(new ARDBadgeDoorContactNotificationMsg("lastMessage", oldvalue, newLastMessage, door));
            door.setLastMessage(newLastMessage);
        }

    }
}
