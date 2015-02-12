package appsgate.ard.protocol.adaptor.callbacks;

import appsgate.ard.protocol.adaptor.ARDBadgeDoor;
import appsgate.ard.protocol.controller.ARDController;
import appsgate.ard.protocol.model.command.listener.ARDMessage;
import appsgate.lig.ard.badge.door.messages.ARDBadgeDoorContactNotificationMsg;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardMessageCallback implements ARDMessage {
    private static Logger logger = LoggerFactory.getLogger(CardMessageCallback.class);
    private ARDBadgeDoor door;
    private String oldvalue;

    public CardMessageCallback(ARDBadgeDoor door,String oldvalue){
        this.door=door;
        this.oldvalue=oldvalue;
    }

    @Override
    public void ardMessageReceived(JSONObject json) throws JSONException {
        JSONObject eventNode=json.getJSONObject("event");

        String newCard="-1";
        try {
            newCard=door.getCardNumber(eventNode.getInt("card_idx"));
        }catch(JSONException e){
            logger.warn("No ID CARD received.",e);
        }finally {
            door.triggerApamMessage(new ARDBadgeDoorContactNotificationMsg("lastCard", oldvalue, newCard, door));
            door.triggerApamMessage(new ARDBadgeDoorContactNotificationMsg("card_idx","",newCard,door));
            door.setLastCard(newCard);
        }
    }
}
