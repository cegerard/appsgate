package appsgate.lig.proxy.knx.device.shutter;

import appsgate.lig.colorLight.actuator.messages.ColorLightNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.shutter.actuator.spec.CoreRollerShutterSpec;
import fr.imag.adele.apam.CST;
import org.json.JSONException;
import org.json.JSONObject;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppsgateShutterKNXDevice extends CoreObjectBehavior implements CoreObjectSpec, CoreRollerShutterSpec {

    private static final Logger logger = LoggerFactory.getLogger(AppsgateShutterKNXDevice.class);

    protected String deviceName;
    protected String deviceId;
    protected String deviceType;

    protected String userType;
    protected String status;

    protected String lightBridgeId;
    protected String lightBridgeIP;
    protected String reachable;

    private void start(){
        logger.info("Appsgate Device {} created",deviceId);
    }

    private void stop(){
        logger.info("Appsgate Device {} removed", deviceId);
    }

    public void reachableChanged(String newReachable){
        this.reachable=newReachable;
    }

    public void statusChanged(String newStatus) {

        status = newStatus;

        notifyChanges("status", status, newStatus);

    }

    private NotificationMsg notifyChanges(String varName, String oldValue, String newValue) {
        return new ColorLightNotificationMsg(varName, oldValue, newValue, this.getAbstractObjectId());
    }

    private Step getDevice(){
        return (Step) CST.apamResolver.findInstByName(null,deviceName).getServiceObject();
    }

    @Override
    public JSONObject getDescription() throws JSONException {

        JSONObject descr = new JSONObject();
        descr.put("id", deviceId);
        descr.put("type", userType); // 7 for color light
        descr.put("status", status);
        descr.put("value", false);
        descr.put("deviceType", "KNX_SHUTTER");

        return descr;
    }

    @Override
    public CORE_TYPE getCoreType() {
        return CORE_TYPE.DEVICE;
    }

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
    public void open() {
        getDevice().decrease();
    }

    @Override
    public void close() {
        getDevice().increase();
    }
}
