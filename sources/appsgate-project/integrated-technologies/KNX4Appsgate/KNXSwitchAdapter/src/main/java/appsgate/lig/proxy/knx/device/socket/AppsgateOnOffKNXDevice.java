package appsgate.lig.proxy.knx.device.socket;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.on_off.actuator.messages.OnOffActuatorNotificationMsg;
import appsgate.lig.on_off.actuator.spec.CoreOnOffActuatorSpec;
import fr.imag.adele.apam.CST;
import org.json.JSONException;
import org.json.JSONObject;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.Switch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppsgateOnOffKNXDevice extends CoreObjectBehavior implements CoreObjectSpec, CoreOnOffActuatorSpec {

    protected String deviceName;
    protected String deviceId;
    protected String deviceType;

    protected String pictureId;
    protected String userType;
    protected String status;

    private static final Logger logger = LoggerFactory.getLogger(AppsgateOnOffKNXDevice.class);

    @Override
    public String getAbstractObjectId() {
        return deviceId;
    }

    @Override
    public String getUserType() {
        return userType;
    }

    @Override
    public int getObjectStatus() {
        return 0;
    }

    @Override
    public String getPictureId() {
        return null;
    }

    @Override
    public JSONObject getDescription() throws JSONException {


        JSONObject descr = new JSONObject();

        descr.put("id", deviceId);
        descr.put("type", userType);
        descr.put("status", status);
        descr.put("isOn",false );//getTargetState()
        descr.put("deviceType", "KNX_SOCKET");

        return descr;
    }

    private Switch getDevice(){
        return (Switch) CST.apamResolver.findInstByName(null,deviceName).getServiceObject();
    }

    @Override
    public void setPictureId(String pictureId) {

    }

    @Override
    public CORE_TYPE getCoreType() {
        return CORE_TYPE.DEVICE;
    }

    @Override
    public boolean getTargetState() {
        return getDevice().isOn();
    }

    @Override
    public void on() {
        getDevice().on();
    }

    @Override
    public void off() {
        getDevice().off();
    }

    private NotificationMsg notifyChanges(String varName, String oldValue, String newValue) {
        return new OnOffActuatorNotificationMsg(varName, oldValue, newValue, this);
    }

    public void statusChanged(String newStatus) {

        status = newStatus;

        notifyChanges("status", status, newStatus);

    }
}
