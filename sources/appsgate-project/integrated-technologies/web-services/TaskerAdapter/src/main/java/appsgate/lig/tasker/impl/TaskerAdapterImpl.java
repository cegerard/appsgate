package appsgate.lig.tasker.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.mobile.device.spec.CoreMobileDeviceSpec;

/**
 * This class holds the CoreObjectSpec and TTS business functions
 *
 * @author thibaud
 *
 */
public class TaskerAdapterImpl extends CoreObjectBehavior implements CoreMobileDeviceSpec, CoreObjectSpec {
    
    private final static Logger logger = LoggerFactory.getLogger(TaskerAdapterImpl.class);

    private String serviceId;

    /**
     * Default Constructor
     */
    public TaskerAdapterImpl() {

    }


    /**
     * Method called on apam instanciation.
     */
    public void newInst(){
        logger.info("Tasker instantiated");
    }
    
    /**
     * Method called on apam deletion.
     */
    public void deleteInst(){
        logger.info("Tasker deleted");
        
    }
    
    
    
    @Override
    public JSONObject getDescription() throws JSONException {
        JSONObject descr = new JSONObject();
        descr.put("id", getAbstractObjectId());
        descr.put("type", getUserType());
        descr.put("status", getObjectStatus());

        return descr;
    }

    @Override
    public JSONObject getBehaviorDescription() {
        return super.getBehaviorDescription();
    }

    @Override
    public String getAbstractObjectId() {
        return serviceId;
    }

    @Override
    public CORE_TYPE getCoreType() {
        return CORE_TYPE.SERVICE;
    }

    @Override
    public int getObjectStatus() {
        return 0;
    }

    @Override
    public String getUserType() {
        return "TaskerAdapter";
    }

    @Override
    public JSONObject getCapabilites() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getActivatedCapabilites() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasCapability(String capability) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isCapabilityAsctivated(String capability) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean sendNotifcation(String msg, int flag) {
        logger.debug("Send a notification : {}", msg);
        return true;
    }

    @Override
    public boolean display(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
