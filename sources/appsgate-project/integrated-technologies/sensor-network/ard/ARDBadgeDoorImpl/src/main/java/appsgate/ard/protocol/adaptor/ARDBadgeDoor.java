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

    /**
     * the system name of this sensor.
     */
    private String sensorName;

    /**
     * The network sensor id
     */
    private String sensorId;

    /**
     * The sensor type (Actuator or Sensor)
     */
    private String sensorType;

    /**
     * True if the device is paired with EnOcean proxy false otherwise
     */
    private String isPaired;

    /**
     * Hold the last signal strength in DBM
     */
    private String signal;

    /**
     * The current status = the last value received from this sensor
     */
    private String currentStatus;

    /**
     * The type for user of this sensor
     */
    private String userType;

    /**
     * The current sensor status.
     *
     * 0 = Off line or out of range
     * 1 = In validation mode (test range for sensor for instance)
     * 2 = In line or connected
     */
    private String status;

    /**
     * The current picture identifier
     */
    private String pictureId;

    private static Logger logger = LoggerFactory.getLogger(ARDBadgeDoor.class);

    public String getAbstractObjectId() {
        return "myObject";
    }

    public String getUserType() {
        return userType;
    }

    public int getObjectStatus() {
        return 0;
    }

    public String getPictureId() {
        return "nopic";
    }

    public JSONObject getDescription() throws JSONException {
        JSONObject descr = new JSONObject();
        descr.put("id", sensorId);
        descr.put("type", userType); //3 for contact sensor
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
    public String getLastCard() {
        return null;
    }

    @Override
    public String getARDClass() {
        return null;
    }

    @Override
    public int getDoorID() {
        return 0;
    }

    @Override
    public int getCardID() {
        return 0;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public NotificationMsg triggerApamMessage(final JSONObject json){
        logger.info("Fowarding ARDMessage as ApamMessage (message:{})",json.toString());
        return new ARDBadgeDoorContactNotificationMsg("json","",json.toString(),this);
    }

    public void ardMessageReceived(JSONObject json) throws JSONException {
        triggerApamMessage(json);
    }

}
