package appsgate.lig.chmi.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.chmi.impl.listeners.TimeObserver;
import appsgate.lig.chmi.spec.AsynchronousCommandResponseListener;
import appsgate.lig.chmi.spec.CHMIProxySpec;
import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.chmi.spec.listeners.CoreEventsListener;
import appsgate.lig.chmi.spec.listeners.CoreUpdatesListener;
import appsgate.lig.chmi.spec.listeners.CoreUpdatesListener.UPDATE_TYPE;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.core.object.spec.CoreObjectSpec.CORE_STATUS;
import appsgate.lig.core.object.spec.CoreObjectSpec.CORE_TYPE;
import fr.imag.adele.apam.Instance;

/**
 * This class is use to address with generic means all the devices and services
 * recruited through ApAM middleware.
 *
 * @author Cédric Gérard
 * @since February 14, 2013
 * @version 1.0.0
 *
 */
public class CHMIProxyImpl implements CHMIProxySpec {

    /**
     * Static class member uses to log what happened in each instances
     */
    private final static Logger logger = LoggerFactory.getLogger(CHMIProxyImpl.class);

    /**
     * Core Object list, resolved by ApAM
     */
    Map<String, CoreObjectSpec> abstractDevice;
    
    /**
     * Array list for updates listener
     */
    private ArrayList<CoreUpdatesListener> updatesListenerList;
    
    /**
     * Array list for events listener
     */
    private ArrayList<CoreEventsListener> eventsListenerList;
    
    /**
	 * HTTP service dependency resolve by iPojo. Allow to register HTML
	 * resources to the Felix HTTP server
	 */
	private HttpService httpService;

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
    	abstractDevice = new ConcurrentHashMap<String, CoreObjectSpec>();

        updatesListenerList = new ArrayList<CoreUpdatesListener>();
        eventsListenerList = new ArrayList<CoreEventsListener>();
        
		if (httpService != null) {
			final HttpContext httpContext = httpService.createDefaultHttpContext();
			final Dictionary<String, String> initParams = new Hashtable<String, String>();
			initParams.put("from", "HttpService");
			try {
				httpService.registerResources("/chmi", "/WEB", httpContext);
				logger.debug("Registered URL : "+httpContext.getResource("/WEB"));
				logger.info("CHMI web ressources registered.");
			} catch (NamespaceException ex) {
				logger.error("NameSpace exception");
			}
		}
        

        
        logger.debug("The CHMI proxy component has been initialized");
    }
    
    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
    	httpService.unregister("/chmi");
        logger.debug("The CHMI proxy component has been stopped");
    }

    /**
     * Called by ApAM when an undefined instance is added to the set.
     *
     * @param inst , the new undefined instance
     */
    public void addAbstractObject(Instance inst) {
        logger.debug("New abstract device added: " + inst.getName());
        try {
            //notify that a new device, service or simulated instance appeared
            CoreObjectSpec newObj = (CoreObjectSpec) inst.getServiceObject();
            if (newObj == null) {
                logger.error("Instance {} does not implement CoreObjectSpec", inst.getAllPropertiesString());
                return;
            }
            abstractDevice.put(newObj.getAbstractObjectId(), newObj);
            
            if(newObj.getObjectStatus() != 2 ) {
            	logger.info("Instance {} is not ready (status != 2)", inst.getAllPropertiesString());
            } else {
            	logger.trace("new core Object : {}, notifying listeners", newObj.getAbstractObjectId());
            	notifyAllUpdatesListeners(UPDATE_TYPE.NEW, newObj.getCoreType(), newObj.getAbstractObjectId(), newObj.getUserType(), newObj.getDescription(), newObj.getBehaviorDescription());
            }
            
        } catch (Exception ex) {
            logger.error("If getCoreType method error trace appeare below it is because the service or the device doesn't implement all methode in"
                    + "the CoreObjectSpec interface : ", ex);
        }
    }

    /**
     * Called by ApAM when an undefined instance is removed from the set
     *
     * @param inst , the removed undefined instance
     */
    public void removedAbstractObject(Instance inst) {
        logger.debug("Abstract device removed: " + inst.getName());
        JSONObject obj = new JSONObject();

        synchronized(this){ 
            CoreObjectSpec rmObj = (CoreObjectSpec) inst.getServiceObject();
            if (rmObj == null) {
                logger.error("Instance {} does not implement CoreObjectSpec", inst.getAllPropertiesString());
                return;
            }
            String deviceId = rmObj.getAbstractObjectId();
            logger.debug("removedAbstractObject(), device Id : " + deviceId);
            abstractDevice.remove(deviceId);
            
            
            try {
                obj.put("objectId", deviceId);
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }
            logger.debug("removedAbstractObject(), coreType: {}, user Type: {} ",rmObj.getCoreType(), rmObj.getUserType());
            notifyAllUpdatesListeners(UPDATE_TYPE.REMOVE, rmObj.getCoreType(), deviceId, rmObj.getUserType(), null, null);
        }
    }

    /**
     * Get the AbstractObjectSpec reference corresponding to the id objectID
     *
     * @param objectID the AbstractObjectSpec identifier
     * @return an AbstractObjectSpec object that have objectID as identifier
     */
    @Override
    public CoreObjectSpec getCoreDevice(String objectId) {
		logger.trace("getCoreDevice(String objectID : {})",objectId);
    	
    	if(objectId == null ||objectId.isEmpty()) {
    		logger.warn("getCoreDevice(...), objectId is null");
    		return null;
    	}
    	
        for (CoreObjectSpec adev : abstractDevice.values()) {
        	if (objectId.equalsIgnoreCase(adev.getAbstractObjectId())
        			&& CoreObjectSpec.CORE_STATUS.isAvailable(adev.getObjectStatus())) {
        		logger.trace("getCoreDevice(...); device found");
                return adev;
            }
        }
		logger.trace("getCoreDevice(...); device NOT found");
		return null;
    }
    
    @Override
    public GenericCommand executeCommand(int clientId, String objectId, String methodName, JSONArray jsonArgs, String callId,
    		AsynchronousCommandResponseListener listener) {
        return new GenericCommand(jsonArgs, getObject(objectId), objectId, methodName, callId, clientId, listener);

    }
    
    private Object getObject(String objectId) {
    	Object obj;
    	if("proxy".equals(objectId)) {
    		logger.trace("getObject(...), ObjectId: proxy, direct call to CHMI");
    		obj = this;
    	} else {
    		obj = getCoreDevice(objectId);
    	}
    	return obj;
    }
    

    /**
     * Called by ApAM when Notification message comes and forward it to client
     * part by calling the sendService
     *
     * @param notif the notification message from ApAM
     */
    public void gotNotification(NotificationMsg notif) {
        logger.debug("gotNotification(NotificationMsg notif : {}) ", notif.JSONize());
        if(notif == null || notif.getSource() == null || !abstractDevice.containsKey(notif.getSource())) {
        	logger.error("gotNotification(...), core object unknown");
            return;
        }
                
        if(CoreObjectSpec.KEY_STATUS.equals(notif.getVarName())) {
    		CoreObjectSpec obj = abstractDevice.get(notif.getSource());
        	if (CORE_STATUS.isAvailable(notif.getNewValue())) {
                logger.debug("gotNotification(...), device is available or become available");
            	notifyAllUpdatesListeners(UPDATE_TYPE.NEW,
            			obj.getCoreType(),
            			obj.getAbstractObjectId(),
            			obj.getUserType(),
            			obj.getDescription(),
            			obj.getBehaviorDescription());
        	} else {
                logger.debug("gotNotification(...), device is no more available");
                notifyAllUpdatesListeners(UPDATE_TYPE.REMOVE, obj.getCoreType(),
                		obj.getAbstractObjectId(), obj.getUserType(), null, null);
        	}
        	
        } else {
            logger.debug("gotNotification(...), event does not concern availability status");
        	notifyAllEventsListeners(notif);
        }
    }

    /**
     * Send all the devices description to one client
     */
    @Override
    public JSONArray getDevicesDescription() {
    	logger.trace("getDevicesDescription()");

        if (abstractDevice != null && !abstractDevice.isEmpty()) {
            JSONArray jsonDeviceList = new JSONArray();

            for (CoreObjectSpec adev : abstractDevice.values()) {
            	if(CoreObjectSpec.CORE_STATUS.isAvailable(adev.getObjectStatus())) {
	            	logger.debug("getDevicesDescription(), getting description for object id : "+adev.getAbstractObjectId());
	            	JSONObject obj = getObjectDescription(adev);
	                jsonDeviceList.put(obj);
            	}
            }
            logger.debug("getDevicesDescription(), returning "+jsonDeviceList);

            return jsonDeviceList;

        } else {
            logger.debug("No Core Object detected.");
            return new JSONArray();
        }
    }
    
    @Override
    public JSONArray getDevicesId() {
    	logger.trace("getDevicesId()");
        JSONArray jsonDeviceList = new JSONArray();
        if (abstractDevice != null && !abstractDevice.isEmpty()) {

            for (CoreObjectSpec dev : abstractDevice.values()) {
            	if(CoreObjectSpec.CORE_STATUS.isAvailable(dev.getObjectStatus())) {            	
            		jsonDeviceList.put(dev.getAbstractObjectId());
            	}
            }
            logger.debug("getDevicesId(), returning "+jsonDeviceList);
            return jsonDeviceList;
        } else {
            logger.debug("No CoreObject detected.");
            return jsonDeviceList;
        }
    }

    @Override
    public JSONObject getDeviceDescription(String objectId) {
    	logger.trace("getDeviceDescription(String objectId : {})",objectId);

    	CoreObjectSpec obj = getCoreDevice(objectId);

        if (obj != null && CoreObjectSpec.CORE_STATUS.isAvailable(obj.getObjectStatus())) {
            return obj.getDescription();
        }

        return new JSONObject();
    }
    

    @Override
    public JSONArray getDevicesDescriptionFromType(String type) {
    	logger.trace("getDevicesDescriptionFromType(String type : {})",type);

        Iterator<CoreObjectSpec> devices = abstractDevice.values().iterator();

        if (devices != null) {
            JSONArray jsonDeviceList = new JSONArray();

            while (devices.hasNext()) {
                CoreObjectSpec adev = devices.next();
                if (type.contentEquals(adev.getUserType())
                		&& CoreObjectSpec.CORE_STATUS.isAvailable(adev.getObjectStatus())) {
                    jsonDeviceList.put(getObjectDescription(adev));
                }
            }

            return jsonDeviceList;

        } else {
            logger.debug("No CoreObject detected.");
            return new JSONArray();
        }
    }
    
    @Override
    public JSONArray getDevicesIdFromType(String type) {
    	logger.trace("getDevicesDescriptionFromType(String type : {})",type);

        Iterator<CoreObjectSpec> devices = abstractDevice.values().iterator();

        if (devices != null) {
            JSONArray jsonDeviceList = new JSONArray();

            while (devices.hasNext()) {
                CoreObjectSpec adev = devices.next();
                if (type.contentEquals(adev.getUserType())
                		&& CoreObjectSpec.CORE_STATUS.isAvailable(adev.getObjectStatus())) {
                    jsonDeviceList.put(adev.getAbstractObjectId());
                }
            }
            return jsonDeviceList;

        } else {
            logger.debug("No Core Object detected.");
            return new JSONArray();
        }
    }    

    @Override
    public JSONObject getDeviceBehaviorFromType(String type) {
        Iterator<CoreObjectSpec> devices = abstractDevice.values().iterator();

        if (devices != null) {
            // Find the first device of the corresponding type (they all share the same description)

            while (devices.hasNext()) {
                CoreObjectSpec adev = devices.next();
                if (type.contentEquals(adev.getUserType())
                		&& CoreObjectSpec.CORE_STATUS.isAvailable(adev.getObjectStatus())) {
                    return adev.getBehaviorDescription();
                }
            }
        }

        logger.warn("No device behavior for type "+type);
        return null;
    }

    @Override
    public String getCoreClockObjectId() {
    	JSONArray clocks = getDevicesIdFromType("21");
    	String clock = null;
    	int nbClocks = clocks.length();
    	int i = 0;
    	try {
    		while(i < nbClocks) {
				clock = clocks.optString(i);
				i++;
    		}
    		if(clock != null) {
    			return clock;
    		}
    	} catch (JSONException e) {
    		logger.debug(e.getMessage());
		}
    	return null;
    }

    /**
     * This method get the auto description of an object and add the contextual
     * information associate to this object for a specified user
     *
     * @param obj the object from which to get the description
     * @return the complete contextual description of an object
     */
    private JSONObject getObjectDescription(CoreObjectSpec obj) {
    	if(obj == null
    			|| !CoreObjectSpec.CORE_STATUS.isAvailable(obj.getObjectStatus())) {
    		return null;
    	}
    	
        JSONObject JSONDescription = null;
        try {
            // Get object auto description
            JSONDescription = obj.getDescription();
    		logger.trace("getObjectDescription(CoreObjectSpec obj), description : "
    				+JSONDescription);	
        } catch (JSONException e) {
            logger.error(e.getMessage(),e);
        } catch (Exception e) {
            logger.error("ApAM error",e);
        }
        return JSONDescription;
    }

	@Override
	public boolean CoreUpdatesSubscribe(CoreUpdatesListener coreUpdatesListener) {
		logger.trace("CoreUpdatesSubscribe(CoreUpdatesListener coreUpdatesListener : "
				+coreUpdatesListener+")");		
		return updatesListenerList.add(coreUpdatesListener);
	}

	@Override
	public boolean CoreUpdatesUnsubscribe(CoreUpdatesListener coreUpdatesListener) {
		logger.trace("CoreUpdatesUnsubscribe(CoreUpdatesListener coreUpdatesListener : "
				+coreUpdatesListener+")");		
		return updatesListenerList.remove(coreUpdatesListener);
	}

	@Override
	public boolean CoreEventsSubscribe(CoreEventsListener coreEventsListener) {
		logger.trace("CoreEventsSubscribe(CoreEventsListener coreEventsListener : "
				+coreEventsListener+")");
		return eventsListenerList.add(coreEventsListener);
	}

	@Override
	public boolean CoreEventsUnsubscribe(CoreEventsListener coreEventsListener) {
		logger.trace("CoreEventsUnsubscribe(CoreEventsListener coreEventsListener : "
				+coreEventsListener+")");
		return eventsListenerList.remove(coreEventsListener);
	}
	
	/**
	 * Notify all updates listeners that something happens
	 * @param coreType the type of object (device, service, etc.)
	 * @param objectId the object identifier
	 * @param userType the user type of the object (Light, switch, etc.)
	 * @param desc the object description (state, id etc.)
	 * @param behavior the object behavior, can be empty JSONObject
	 */
	private void notifyAllUpdatesListeners(UPDATE_TYPE updateType, CORE_TYPE coreType, String objectId, String userType, JSONObject descr, JSONObject behavior) {
		for(CoreUpdatesListener listener : updatesListenerList) {
			listener.notifyUpdate(updateType, coreType, objectId, userType, descr, behavior);
		}
	}

	/**
	 * Notify all events listeners that something happens
	 * @param srcId the core identifier of the source
	 * @param varName the name of the variable that changed
	 * @param value the new value of the variable
	 */
	private void notifyAllEventsListeners(NotificationMsg msg) {
		for(CoreEventsListener listener : eventsListenerList) {
			listener.notifyEvent(msg);
		}
	}
	
	@Override
	public int registerTimeAlarm(Calendar calendar, String message) {
		CoreObjectSpec obj = getCoreDevice(getCoreClockObjectId());
		if(obj != null) {
			CoreClockSpec clock = (CoreClockSpec)obj;
			
			return clock.registerPeriodicAlarm(calendar, 1000*60*60*24 ,new TimeObserver(message));
		}
		return -1;
	}

	@Override
	public void unregisterTimeAlarm(Integer alarmId) {
		CoreObjectSpec obj = getCoreDevice(getCoreClockObjectId());
		if(obj != null) {
			CoreClockSpec clock = (CoreClockSpec)obj;
			clock.unregisterAlarm(alarmId);
			clock.calculateNextTimer();
		}
	}

	@Override
	public long getCurrentTimeInMillis() {
		CoreObjectSpec obj = getCoreDevice(getCoreClockObjectId());
		if(obj != null) {
			CoreClockSpec clock = (CoreClockSpec)obj;
			return clock.getCurrentTimeInMillis();
		}
		return 0;
	}

	@Override
	public double getTimeFlowRate() {
		CoreObjectSpec obj = getCoreDevice(getCoreClockObjectId());
		if(obj != null) {
			CoreClockSpec clock = (CoreClockSpec)obj;
			return clock.getTimeFlowRate();
		}
		return 1.0;
	}
	
	@Override
	public void shutdown() {
		BundleContext ctx = FrameworkUtil.getBundle(CHMIProxyImpl.class).getBundleContext();
		Bundle systemBundle = ctx.getBundle(0);
		try {
			systemBundle.stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void restart() {
		BundleContext ctx = FrameworkUtil.getBundle(CHMIProxyImpl.class).getBundleContext();
		Bundle systemBundle = ctx.getBundle(0);
		try {
			systemBundle.update();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}
	
}
