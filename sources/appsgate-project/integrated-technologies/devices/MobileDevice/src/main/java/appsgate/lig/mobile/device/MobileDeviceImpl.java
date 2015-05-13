package appsgate.lig.mobile.device;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.mobile.device.adapter.spec.MobileDeviceAdapterServices;
import appsgate.lig.mobile.device.messages.MobileDeviceNotificationMsg;
import appsgate.lig.mobile.device.spec.CoreMobileDeviceSpec;

/**
 * This is the class that represent Mobile device (SamrtPhone, Tablet)
 * implementation in ApAM.
 *
 * @author Cédric Gérard
 * @since April 12, 2014
 * @version 1.0.0
 *
 * @see CoreObjectBehavior
 * @see CoreObjectSpec
 * @see CoreMobileDeviceSpec
 */
public class MobileDeviceImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreMobileDeviceSpec {

    public static final String IMPL_NAME = "MobileDeviceImpl";

    private MobileDeviceAdapterServices mobileDeviceAdapter;

    private String deviceName;
    private String deviceId;
    private String deviceType;
    private String userType;
    private String status;

    /**
     * Static class member uses to log what happened in each instances
     */
    private static final Logger logger = LoggerFactory.getLogger(MobileDeviceImpl.class);

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        logger.info("New mobile device detected");
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        logger.info("A mobile device desapeared");
    }

    /**
     * This method uses the ApAM message model. Each call produce a
     * MobileDeviceNotificationMsg object and notifies ApAM that a new message
     * has been released.
     *
     * @return nothing, it just notifies ApAM that a new message has been
     * posted.
     */
    public NotificationMsg notifyChanges(String varName, String value) {
        return new MobileDeviceNotificationMsg(varName, value, this.getAbstractObjectId());
    }

    @Override
    public JSONObject getCapabilites() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject getActivatedCapabilites() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasCapability(String capability) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCapabilityAsctivated(String capability) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean sendNotifcation(String msg, int flag) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean display(String message) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getAbstractObjectId() {
        // TODO Auto-generated method stub
        return "DEFAULT";
    }

    @Override
    public String getUserType() {
        return "MobileDevice";
    }

    @Override
    public int getObjectStatus() {
        return 0;
    }


    @Override
    public CORE_TYPE getCoreType() {
        return CORE_TYPE.DEVICE;
    }

    /**
     * Called by ApAM when the status value changed
     *
     * @param newStatus the new status value. its a string the represent a
     * integer value for the status code.
     */
    public void statusChanged(String newStatus) {
        logger.info("The device, " + deviceId + " status changed to " + newStatus);
        notifyChanges("status", newStatus);
    }

    /**
     * 
     * @param aThis 
     */
    public void setAdapter(MobileDeviceAdapterServices aThis) {
        this.mobileDeviceAdapter = aThis;
    }

}
