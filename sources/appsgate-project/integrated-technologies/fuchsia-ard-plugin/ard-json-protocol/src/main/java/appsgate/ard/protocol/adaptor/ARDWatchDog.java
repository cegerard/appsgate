package appsgate.ard.protocol.adaptor;

import appsgate.ard.protocol.model.command.listener.ARDMessage;
import appsgate.ard.protocol.spec.ARDWatchDogSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ARDWatchDog extends CoreObjectBehavior implements ARDMessage, CoreObjectSpec,ARDWatchDogSpec {

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
    private String sensoreType;

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

    private static Logger logger = LoggerFactory.getLogger(ARDWatchDog.class);

    public String getAbstractObjectId() {
        return "myObject";
    }

    public String getUserType() {
        return "21";
    }

    public int getObjectStatus() {
        return 0;
    }

    public String getPictureId() {
        return "nopic";
    }

    public JSONObject getDescription() throws JSONException {
        return new JSONObject();
    }

    public void setPictureId(String pictureId) {

    }

    public CORE_TYPE getCoreType() {
        return CORE_TYPE.DEVICE;
    }

    /*
    public NotificationMsg notifyChanges(String varName, String value) {
        return new ContactNotificationMsg(true, varName, value, this);
    }
    */

    public void newInst() {
        logger.info("New contact sensor detected, "+sensorId);
    }

    public void deleteInst() {
        logger.info("Contact sensor desapeared, " + sensorId);
    }

    public boolean getContactStatus() {
        return false;
    }

    public String getLastCard() {
        return null;
    }

    public String getARDClass() {
        return null;
    }

    public int getDoorID() {
        return 0;
    }

    public int getCardID() {
        return 0;
    }

    public String getStatus() {
        return null;
    }

    public NotificationMsg triggerApamMessage(final JSONObject json){
        logger.info("Fowarding ARDMessage as ApamMessage (message:{})",json.toString());
        return new NotificationMsg() {
            public CoreObjectSpec getSource() {
                return ARDWatchDog.this;
            }
            public String getOldValue() {
                return "";
            }

            public String getNewValue() {
                return json.toString();
            }

            public String getVarName() {
                return "json";
            }

            public JSONObject JSONize() {
                return json;
            }
        };
    }

    public void ardMessageReceived(JSONObject json) throws JSONException {
        triggerApamMessage(json);
    }
}
