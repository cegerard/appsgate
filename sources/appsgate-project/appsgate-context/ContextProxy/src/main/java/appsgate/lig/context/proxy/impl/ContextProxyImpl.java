package appsgate.lig.context.proxy.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.clock.sensor.spec.AlarmEventObserver;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.context.device.properties.table.spec.DevicePropertiesTableSpec;
import appsgate.lig.context.proxy.listeners.CoreListener;
import appsgate.lig.context.proxy.spec.ContextProxySpec;
import appsgate.lig.context.proxy.spec.StateDescription;
import appsgate.lig.context.userbase.spec.UserBaseSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.main.spec.CHMIProxySpec;
import appsgate.lig.manager.place.spec.PlaceManagerSpec;
import appsgate.lig.manager.place.spec.SymbolicPlace;
//import appsgate.lig.manager.context.spec.ContextManagerSpec;
//import appsgate.lig.manager.space.spec.subSpace.Space;
//import appsgate.lig.manager.space.spec.subSpace.Space.TYPE;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class is use to allow other components to subscribe for specific
 * triggering event or request for system or devices state
 *
 * @author Cédric Gérard
 * @since May 28, 2013
 * @version 1.0.0
 */
public class ContextProxyImpl implements ContextProxySpec {

    /**
     * Static class member uses to log what happened in each instances
     */
    private final static Logger logger = LoggerFactory.getLogger(ContextProxyImpl.class);

    /**
     * Events subscribers list
     */
    private final HashMap<Entry, ArrayList<CoreListener>> eventsListeners = new HashMap<Entry, ArrayList<CoreListener>>();

    /**
     * Hash map use to conserve alarm identifier.
     */
    private final HashMap<Entry, Integer> alarmListenerList = new HashMap<Entry, Integer>();

    /**
     * The AppsGate time sensor
     */
    private CoreClockSpec coreClock;

//    /**
//     * Field to handle the space manager API
//     */
//    private ContextManagerSpec contextManager;
    
    /**
 	* Field to handle the place manager service
 	*/
    private PlaceManagerSpec placeManager;
    
    /**
 	* Field to handle the user base service
 	*/
    private UserBaseSpec userBaseManager;
    
    /**
 	* Field to handle the device name table  service
 	*/
    private DevicePropertiesTableSpec devicePropertiesManager;
    
    /**
 	* Field to handle the router service interface
 	*/
    private CHMIProxySpec chmiProxy;

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        logger.debug("The context follower ApAM component has been initialized");
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        logger.debug("The context follower ApAM component has been stopped");
    }

    @Override
    public synchronized void addListener(CoreListener coreListener) {
        logger.debug("Adding a listener...");
        Entry eventKey = new Entry(coreListener);

        //Check if the need to by register in the core clock implementation
        CoreObjectSpec abstractClock = (CoreObjectSpec) coreClock;
        if (abstractClock.getAbstractObjectId().contentEquals(coreListener.getObjectId()) && eventKey.getVarName().contentEquals("ClockAlarm") && !eventKey.isEventOnly()) {
            logger.debug("Adding an alarm listener...");
            //Generate calendar java object for core clock
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(coreListener.getValue()));
            //register the alarm
            int alarmId = coreClock.registerAlarm(calendar, new TimeObserver());
            //change the event entry with the alarmId value
            eventKey.setValue(String.valueOf(alarmId));
            //save the alarm identifier
            alarmListenerList.put(eventKey, alarmId);
            logger.debug("Alarm listener added.");
        }

        Set<Entry> keys = eventsListeners.keySet();
        Iterator<Entry> keysIt = keys.iterator();
        boolean added = false;

        while (keysIt.hasNext() && !added) {
            Entry key = keysIt.next();
            if (eventKey.equals(key)) {
                eventsListeners.get(key).add(coreListener);
                logger.debug("Add follower to existing listener list");
                added = true;
            }
        }

        if (!added) {
            ArrayList<CoreListener> coreListenerList = new ArrayList<CoreListener>();
            coreListenerList.add(coreListener);
            eventsListeners.put(eventKey, coreListenerList);
            logger.debug("Add new event follower list");
        }

    }

    @Override
    public synchronized void deleteListener(CoreListener coreListener) {
        logger.debug("Deleting a listener...");
        Entry eventKey = new Entry(coreListener);

        Set<Entry> keys = eventsListeners.keySet();
        Iterator<Entry> keysIt = keys.iterator();

        while (keysIt.hasNext()) {
            Entry key = keysIt.next();
            if (key.equals(eventKey)) {
                ArrayList<CoreListener> coreListenerList = eventsListeners.get(key);
                coreListenerList.remove(coreListener);
                if (coreListenerList.isEmpty()) {
                    eventsListeners.remove(key);
                }
                Integer alarmId = alarmListenerList.get(key);
                if (alarmId != null) {
                    logger.debug("Deleting an alarm listener with id: " + alarmId);
                    coreClock.unregisterAlarm(alarmId);
                    alarmListenerList.remove(key);
                }
                break;
            }
        }

        logger.debug("Listeners deleted");
    }

    @Override
    public ArrayList<String> getDevicesInSpaces(ArrayList<String> typeList,
            ArrayList<String> spaces) {
    	
    
    	ArrayList<String> coreObjectInPlace = new ArrayList<String>();
    	ArrayList<String> coreObjectOfType = new ArrayList<String>();
    	
    	//First we get all objects in each place, if the list is empty we get all placed objects.
    	if (!spaces.isEmpty()) {
    		for (String placeId : spaces) {
    			SymbolicPlace place = placeManager.getSymbolicPlace(placeId);
                        if (place != null) {
    			coreObjectInPlace.addAll(place.getDevices());
                        } else {
                            logger.warn("No such place found: {}",placeId);
                        }
    		}
    	} else {
    		for (SymbolicPlace symbolicPlace : placeManager.getPlaces()) {
    			coreObjectInPlace.addAll(symbolicPlace.getDevices());
    		}
    	}

		// Now we get all identifier of device that match one types of the type
		// list
		try {
			if (!typeList.isEmpty()) {
				for (String type : typeList) {
					JSONArray devicesOfType = chmiProxy.getDevices(type);
					int size = devicesOfType.length();
					for (int i = 0; i < size; i++) {
						coreObjectOfType.add(devicesOfType.getJSONObject(i).getString("id"));
					}
				}
			} else {
				JSONArray allDevices = chmiProxy.getDevices();
				int size = allDevices.length();
				for (int i = 0; i < size; i++) {
					coreObjectOfType.add(allDevices.getJSONObject(i).getString("id"));
				}
			}
			
			//We get the intersection between placed object and object of specified type
	    	coreObjectInPlace.retainAll(coreObjectOfType);
	    	
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
        return coreObjectInPlace;
    }

    /**
     * Called by ApAM when Notification message comes
     *
     * @param notif the notification message from ApAM
     */
    @SuppressWarnings("unchecked")
    public void gotNotification(NotificationMsg notif) {
        logger.debug("Event message receive, " + notif.JSONize());
        JSONObject event = notif.JSONize();
        Entry eventKey;

        if (event.has("newspace")) {
            logger.debug("New space detected");
            try {
                eventKey = new Entry("connected", event.getJSONObject("newspace").getJSONArray("properties"));
            } catch (JSONException ex) {
                return;
            }
        } else if (event.has("objectId")) {
            eventKey = new Entry(event);
        } else {
            logger.trace("this kind of message is not treated");
            return;
        }

        ArrayList<Entry> keys = new ArrayList<Entry>();

        // Copy the listener just to avoid concurrent exception with program
        // when daemon try to add listener again when they restart
        synchronized (this) {
            Iterator<Entry> tempKeys = eventsListeners.keySet().iterator();
            while (tempKeys.hasNext()) {
                keys.add(tempKeys.next());
            }
        }

        for (Entry key : keys) {
            if (eventKey.match(key)) {
                logger.trace("Event is followed, retreiving listeners...");

                ArrayList<CoreListener> coreListenerList;
                synchronized (this) {
                    // TODO the clone method is used because is necessary if in the notifyEvent
                    // override method someone call the deleteListener method that made deadlock
                    coreListenerList = (ArrayList<CoreListener>) eventsListeners.get(key).clone();
                }
                for (CoreListener listener : coreListenerList) {
                    logger.trace("Notify listener.");
                    listener.notifyEvent();
                }
            }
        }

    }

    @Override
    public StateDescription getEventsFromState(String objectId, String stateName) {
    	JSONObject deviceDetails = chmiProxy.getDevice(objectId);
    	StateDescription stateDescription = null;
    	try {
    		JSONObject grammar = devicePropertiesManager.getGrammarFromType(deviceDetails.getString("type"));
    		JSONArray grammarStates = grammar.getJSONArray("states");
    		for (int i = 0; i < grammarStates.length(); i++) {
    			if (grammarStates.getJSONObject(i).getString("name").equalsIgnoreCase(stateName)) {
    				stateDescription = new StateDescription(grammarStates.getJSONObject(i));
    				break;
    			}
    		}
    	}catch (JSONException ex) {
    		logger.error("Grammar not well formatted");
    		return null;
    	}
    	
    	return stateDescription;
    }

    /**
     * Inner class use to create an event notify condition
     *
     * @author Cédric Gérard
     * @since May 28, 2013
     * @version 1.0.0
     */
    private class Entry {

        /**
         * Identifier of the source f the event
         */
        private String objectId;

        /**
         * The variable name to follow
         */
        private String varName;

        /**
         * The threshold value
         */
        private String value;

        /**
         * The kind of entry "eventValue": for specific value of an event
         * "eventName" : for a specific event but don't care about the value
         */
        private String entryType;


        /**
         * Constructor for an Entry
         *
         * @param objectId
         * @param varName
         * @param value
         */
        public Entry(String objectId, String varName, String value) {
            initWith(objectId, varName, value);
        }

        public Entry(JSONObject event) {
            initWith(event.optString("objectId"), event.optString("varName"), event.optString("value"));
        }

        public Entry(CoreListener core) {
            initWith(core.getObjectId(), core.getEvent(), core.getValue());
        }

        public Entry(String vName, JSONArray a) throws JSONException {
            String v = null;
            String id = null;
            for (int i = 0; i < a.length(); i++) {
                JSONObject jsonObject = a.getJSONObject(i);
                if (jsonObject.has("deviceType")) {
                    v = jsonObject.getString("deviceType");
                }
                if (jsonObject.has("ref")) {
                    id = jsonObject.getString("ref");
                }
            }
            initWith(vName, v, id);
        }
        /**
         * 
         * @param objectId
         * @param varName
         * @param value 
         */
        private void initWith(String objectId, String varName, String value) {
            this.objectId = objectId;
            this.varName = varName;
            this.value = value;
            if (value.contentEquals("")) {
                entryType = "eventName";
            } else {
                entryType = "eventValue";
            }
        }

        /**
         *
         * @return the object Id
         */
        public String getObjectId() {
            return objectId;
        }

        /**
         * get the var name
         *
         * @return
         */
        public String getVarName() {
            return varName;
        }

        /**
         * Get the value
         *
         * @return
         */
        public String getValue() {
            return value;
        }

        /**
         * Set the value
         *
         * @param value
         */
        public void setValue(String value) {
            this.value = value;
        }

        @SuppressWarnings("unused")
        public String getEntryType() {
            return entryType;
        }

        /**
         * Test if the Entry is only considering the event name and not the
         * event value
         *
         * @return true if the entry doesn't take in consideration the event
         * value, false otherwise
         */
        public boolean isEventOnly() {
            return entryType.contentEquals("eventName");
        }

        /**
         * Override the equals method to compare two entry together but compare
         * their contents
         */
        @Override
        public boolean equals(Object eventEntry) {
            if (eventEntry instanceof Entry) {
                Entry entry = (Entry) eventEntry;
                return (entry.getObjectId().contentEquals(objectId)
                        && entry.getVarName().contentEquals(varName)
                        && (entry.getValue().contentEquals(value) || entry.getValue().contentEquals("")));
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + (this.objectId != null ? this.objectId.hashCode() : 0);
            hash = 23 * hash + (this.varName != null ? this.varName.hashCode() : 0);
            hash = 23 * hash + (this.value != null ? this.value.hashCode() : 0);
            return hash;
        }

        /**
         *
         * @param key
         * @return
         */
        private boolean match(Entry key) {
            // if value does not match
            if (key.value == null ? this.value != null : !key.value.equals(this.value)) {
                return false;
            }
            // if var name does not match
            if (key.varName == null ? this.varName != null : !key.varName.equals(this.varName)) {
                return false;
            }
            if (key.objectId == null || key.objectId.isEmpty()) {
                return true;
            }
            return key.objectId.equals(this.objectId);
        }

    }

    /**
     * Inner class to register time notification through the core clock
     *
     * @author Cédric Gérard
     * @since September 25, 2013
     * @version 1.0.0
     */
    private class TimeObserver implements AlarmEventObserver {

        @Override
        public void alarmEventFired(int alarmEventId) {
            logger.debug("Alarm notification fired: " + alarmEventId);
        }

    }

}
