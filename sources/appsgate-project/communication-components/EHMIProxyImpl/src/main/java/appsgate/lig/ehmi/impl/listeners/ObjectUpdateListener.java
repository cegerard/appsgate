package appsgate.lig.ehmi.impl.listeners;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.chmi.spec.listeners.CoreUpdatesListener;
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

    private String coreType = "";
    private String userType = "";
    private JSONObject description = new JSONObject();
    private JSONObject behavior;

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
    public String getCoreType() {
        return coreType;
    }

    @Override
    public String getUserType() {
        return userType;
    }

    @Override
    public JSONObject getObjectDescription() {
        return description;
    }

    @Override
    public JSONObject getBehaviorDescription() {
        return behavior;
    }

    @Override
    public void notifyUpdate(String coreType, String objectId, String userType, JSONObject description, JSONObject behavior) {
        logger.debug("Update core notification received for: " + objectId);
        this.coreType = coreType;
        this.userType = userType;
        this.behavior = behavior;
        this.description = description;

        String name = EHMIProxy.getUserObjectName(objectId, "");
        String placeId = EHMIProxy.getCoreObjectPlaceId(objectId);

        if (coreType.contains("new")) { //New device added
            EHMIProxy.addGrammar(objectId, userType, new GrammarDescription(behavior));
            
            sendObjectPlace(coreType, objectId, placeId);
            sendObjectName(objectId, name);

            if (userType.contentEquals("21") && !EHMIProxy.getSystemClock().isRemote()) { //The new device is a clock and no remote clock
                EHMIProxy.startRemoteClockSync();										//Is already used.
            }
            if(traceManager != null) {
            	traceManager.coreUpdateNotify(EHMIProxy.getCurrentTimeInMillis(), objectId, coreType, userType, name, description, "new");
            }
            EHMIProxy.newDeviceStatus(objectId, Boolean.TRUE);

        } else if (coreType.contains("remove")) { //A device has been removed

            if (userType.contentEquals("21") && EHMIProxy.getSystemClock().isRemote() //The removed device is a clock, the system is remotely synchronized
                    && objectId.contentEquals(EHMIProxy.getSystemClock().getAbstractObjectId())) { //and it is the clock what we are synchronized with
                EHMIProxy.stopRemoteClockSync();
            }
            traceManager.coreUpdateNotify(EHMIProxy.getCurrentTimeInMillis(), objectId, coreType, userType, name, description, "remove");
            EHMIProxy.newDeviceStatus(objectId, Boolean.FALSE);

        }

    }

    /**
     * Update the current name of a newly added object
     *
     * @param objectId the object identifier
     * @param name the name to add to the object
     */
    private void sendObjectName(String objectId, String name) {
        JSONObject notif = new JSONObject();
        try {
            notif.put("objectId", objectId);
            notif.put("userId", "");
            notif.put("varName", "name");
            notif.put("value", name);

        } catch (JSONException e) {
            logger.error(e.getMessage());
        }

        EHMIProxy.sendToClients(notif);
    }

    /**
     * Update the current place of a newly discovered object
     *
     * @param coreType the core type of the object (device or service)
     * @param objId the identifier of this object
     * @param placeId the place where to set this object
     */
    private void sendObjectPlace(String coreType, String objId, String placeId) {
        JSONObject notif = new JSONObject();
        JSONObject content = new JSONObject();

        try {
            content.put("srcLocationId", "-1");
            content.put("destLocationId", placeId);
            if (coreType.contentEquals("newDevice") || coreType.contentEquals("newSimulatedDevice")) {
                content.put("deviceId", objId);
                notif.put("moveDevice", content);
            } else if (coreType.contentEquals("newService") || coreType.contentEquals("newSimulatedService")) {
                content.put("serviceId", objId);
                notif.put("moveService", content);
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }

        EHMIProxy.sendToClients(notif);
    }

    public void setTraceManager(TraceManSpec traceManager) {
        this.traceManager = traceManager;
    }

}
