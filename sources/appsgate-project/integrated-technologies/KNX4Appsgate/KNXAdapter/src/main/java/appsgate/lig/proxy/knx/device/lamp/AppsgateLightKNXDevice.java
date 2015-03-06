package appsgate.lig.proxy.knx.device.lamp;

import appsgate.lig.colorLight.actuator.messages.ColorLightNotificationMsg;
import appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import fr.imag.adele.apam.CST;
import org.json.JSONException;
import org.json.JSONObject;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.Switch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppsgateLightKNXDevice extends CoreObjectBehavior implements CoreColorLightSpec, CoreObjectSpec{

    private static final Logger logger = LoggerFactory.getLogger(AppsgateLightKNXDevice.class);

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
        return new ColorLightNotificationMsg(varName, oldValue, newValue, this);
    }

    @Override
    public JSONObject getLightStatus() {

        JSONObject jsonResponse = null;
        try {
            jsonResponse = new JSONObject();
            JSONObject lightState = new JSONObject();
            lightState.put("on", getDevice().isOn());
            jsonResponse.put("state", lightState);
            jsonResponse.put("type", getUserType());
            jsonResponse.put("name", deviceName);

        } catch (JSONException e) {
            logger.error("Failed creating return for getLightStatus() with the message {}",e.getMessage(),e);
        }
        return jsonResponse;

    }

    @Override
    public boolean on() {

        getDevice().on();

        return true;
    }

    private Switch getDevice(){
        return (Switch)CST.apamResolver.findInstByName(null,deviceName).getServiceObject();
    }

    @Override
    public boolean off() {

        getDevice().off();

        return true;
    }

    @Override
    public boolean setWhite() {

        return on();

    }

    @Override
    public boolean toggle() {
        if(getCurrentState()) {
            return off();
        } else {
            return on();
        }
    }

    @Override
    public boolean getCurrentState() {
        return getDevice().isOn();
    }

    @Override
    public JSONObject getDescription() throws JSONException {
        JSONObject descr = new JSONObject();
        descr.put("id", deviceId);
        descr.put("type", userType); // 7 for color light
        descr.put("status", status);
        descr.put("value", false);//getCurrentState()
        descr.put("color", getLightColor());
        descr.put("saturation", getLightColorSaturation());
        descr.put("brightness", getLightBrightness());
        //Entry added for configuration GUI
        descr.put("deviceType", "KNX_LIGHT");

        return descr;
    }

    @Override
    public CORE_TYPE getCoreType() {
        return CORE_TYPE.DEVICE;
    }

    @Override
    public long getLightColor() {
        return 0;
    }

    @Override
    public int getLightBrightness() {
        return 0;
    }

    @Override
    public int getLightColorSaturation() {
        return 0;
    }

    @Override
    public JSONObject getManufacturerDetails() {
        return null;
    }

    @Override
    public boolean setStatus(JSONObject newStatus) {
        return false;
    }

    @Override
    public boolean setColor(long color) {
        return false;
    }

    @Override
    public boolean setBrightness(long brightness) {
        return false;
    }

    @Override
    public boolean setSaturation(int saturation) {
        return false;
    }

    @Override
    public boolean setRed() {
        return false;
    }

    @Override
    public boolean setBlue() {
        return false;
    }

    @Override
    public boolean setGreen() {
        return false;
    }

    @Override
    public boolean setYellow() {
        return false;
    }

    @Override
    public boolean setOrange() {
        return false;
    }

    @Override
    public boolean setPurple() {
        return false;
    }

    @Override
    public boolean setPink() {
        return false;
    }

    @Override
    public boolean setDefault() {
        return false;
    }

    @Override
    public boolean increaseBrightness(int step) {
        return false;
    }

    @Override
    public boolean decreaseBrightness(int step) {
        return false;
    }

    @Override
    public boolean blink() {
        return false;
    }

    @Override
    public boolean blink30() {
        return false;
    }

    @Override
    public boolean colorLoop() {
        return false;
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
    public boolean setColorJson(JSONObject color) {
        return false;
    }

}
