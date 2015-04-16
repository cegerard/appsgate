package appsgate.lig.ehmi.impl.listeners;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.chmi.spec.listeners.CoreUpdatesListener;
import appsgate.lig.core.object.spec.CoreObjectSpec.CORE_TYPE;
import appsgate.lig.ehmi.impl.EHMIProxyImpl;
import appsgate.lig.ehmi.spec.GrammarDescription;
import appsgate.lig.ehmi.spec.trace.TraceManSpec;

/**
 *
 * @author Cédric Gérard
 * @since April 23, 2014
 * @version 1.0.0
 *
 */
public class ObjectUpdateListener implements CoreUpdatesListener {

    private final EHMIProxyImpl EHMIProxy;
    private TraceManSpec traceManager;

    /**
     * Static class member uses to log what happened in each instances
     */
    private final static Logger logger = LoggerFactory.getLogger(ObjectUpdateListener.class);

    public ObjectUpdateListener(EHMIProxyImpl eHMIProxy) {
        super();
        EHMIProxy = eHMIProxy;
    }

    @Override
    public void notifyUpdate(UPDATE_TYPE updateType, CORE_TYPE coreType, String objectId, String userType, JSONObject description, JSONObject behavior) {
        logger.trace("notifyUpdate(UPDATE_TYPE updateType : {}, CORE_TYPE coreType : {}, String objectId : {}"
        		+ ", String userType: {}, JSONObject description: {}, JSONObject behavior: {})",
        		updateType, coreType, objectId, userType, description, behavior);

        String name = "";
    	if(EHMIProxy.getDevicePropertiesTable() != null) {
    		name = EHMIProxy.getDevicePropertiesTable().getName(objectId, "");
    	}
    	
        if (UPDATE_TYPE.NEW.equals(updateType)) { //New device added
        	if(EHMIProxy.getDevicePropertiesTable() != null) {
        		EHMIProxy.getDevicePropertiesTable().addGrammarForDevice(objectId, userType, new GrammarDescription(behavior));
        	}
            
            EHMIProxy.addContextData(description, objectId);
            
    		JSONObject jsonResponse =  new JSONObject();
    		try {
    			jsonResponse.put(updateType.getName()+coreType.getName(), description);
                EHMIProxy.sendToClients(jsonResponse);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

            if(traceManager != null) {
            	traceManager.coreUpdateNotify(EHMIProxy.getCurrentTimeInMillis(), objectId, updateType.getName()+coreType.getName(), userType, name, description, updateType.getName());
            }
            EHMIProxy.newDeviceStatus(objectId, Boolean.TRUE);

        } else if (UPDATE_TYPE.REMOVE.equals(updateType)) { //A device has been removed

    		JSONObject jsonResponse =  new JSONObject();
    		try {
    			jsonResponse.put(updateType.getName()+coreType.getName(), new JSONObject().put("objectId", objectId));
                EHMIProxy.sendToClients(jsonResponse);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
            traceManager.coreUpdateNotify(EHMIProxy.getCurrentTimeInMillis(), objectId, updateType.getName()+coreType.getName(), userType, name, description, updateType.getName());
            EHMIProxy.newDeviceStatus(objectId, Boolean.FALSE);
        }

    }

    public void setTraceManager(TraceManSpec traceManager) {
        this.traceManager = traceManager;
    }

}
