package appsgate.ard.protocol.adaptor;

import appsgate.ard.protocol.model.command.listener.ARDMessage;
import appsgate.lig.ard.badge.door.messages.ARDBadgeDoorContactNotificationMsg;
import appsgate.lig.ard.badge.door.spec.CoreARDBadgeDoorSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ARDBadgeDoor extends CoreObjectBehavior implements ARDMessage, CoreObjectSpec, CoreARDBadgeDoorSpec { //ARDWatchDogSpec

    private static Logger logger = LoggerFactory.getLogger(ARDBadgeDoor.class);
    private String sensorName;
    private String sensorId;
    private String sensorType;
    private String isPaired;
    private String signal;
    private String currentStatus;
    private String userType;
    /**
     * 0 = Off line or out of range
     * 1 = In validation mode (test range for sensor for instance)
     * 2 = In line or connected
     */
    private String status;
    private String pictureId;
    private Integer doorID;
    private Integer lastCard;
    private Boolean authorized;
    private String ardClass;

    public String getAbstractObjectId() {
        return sensorId;
    }

    public String getUserType() {
        return userType;
    }

    public int getObjectStatus() {
        return 0;
    }

    public String getPictureId() {
        return pictureId;
    }

    public JSONObject getDescription() throws JSONException {
        JSONObject descr = new JSONObject();
        descr.put("id", sensorId);
        descr.put("type", userType);
        descr.put("status", status);
        descr.put("contact", currentStatus);
        descr.put("deviceType", sensorType);

        return descr;
    }

    public void setPictureId(String pictureId) {

    }

    public CORE_TYPE getCoreType() {
        return CORE_TYPE.DEVICE;
    }

    public void newInst() {
        logger.info("New contact sensor detected, "+sensorId);
    }

    public void deleteInst() {
        logger.info("Contact sensor disappeared, " + sensorId);
    }

    @Override
    public boolean getContactStatus() {
        return false;
    }

    @Override
    public Integer getLastCard() {
        return lastCard;
    }

    @Override
    public String getARDClass() {
        return null;
    }

    @Override
    public Integer getDoorID() {
        return doorID;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public NotificationMsg triggerApamMessage(ARDBadgeDoorContactNotificationMsg apamMessage){
        logger.info("Forwarding ARDMessage as ApamMessage, {}:{})",apamMessage.getVarName(),apamMessage.getNewValue());
        return apamMessage;
    }

    public void ardMessageReceived(JSONObject json)  {

        try {

            JSONObject eventNode=json.getJSONObject("event");

            Integer newCard=eventNode.getInt("card_idx");
            Integer newdoorID=eventNode.getInt("door_idx");
            Boolean newAuthorized=eventNode.getString("status").equalsIgnoreCase("ok")?true:false;
            String newArdClass=eventNode.getString("class");

            triggerApamMessage(new ARDBadgeDoorContactNotificationMsg("card_idx",lastCard.toString(),newCard.toString(),this));
            triggerApamMessage(new ARDBadgeDoorContactNotificationMsg("door_idx",doorID.toString(),newdoorID.toString(),this));
            triggerApamMessage(new ARDBadgeDoorContactNotificationMsg("authorized",authorized.toString(),newAuthorized.toString(),this));
            triggerApamMessage(new ARDBadgeDoorContactNotificationMsg("ardClass",ardClass,newArdClass,this));

            lastCard=newCard;
            doorID=newdoorID;
            authorized=newAuthorized;
            ardClass=newArdClass;

        }catch(JSONException e){
            logger.error("Failed parsing ARD JSON message.",e);
        }

    }

}
