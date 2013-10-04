package appsgate.lig.context.follower.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.clock.sensor.spec.AlarmEventObserver;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.context.follower.listeners.CoreListener;
import appsgate.lig.context.follower.spec.ContextFollowerSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is use to allow other components to subscribe for specific
 * triggering event or request for system or devices state
 * 
 * @author Cédric Gérard
 * @since May 28, 2013
 * @version 1.0.0
 */
public class ContextFollowerImpl implements ContextFollowerSpec {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(ContextFollowerImpl.class);
	
	/**
	 * Events subscribers list
	 */
	private HashMap<Entry, ArrayList<CoreListener>> eventsListeners = new HashMap<Entry, ArrayList<CoreListener>>();
	
	/**
	 * Hash map use to conserve alarm identifier.
	 */
	private HashMap<Entry, Integer> alarmListenerList = new HashMap<Entry, Integer>();
	
	/**
	 * The AppsGate time sensor 
	 */
	private CoreClockSpec coreClock;

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
		Entry eventKey = new Entry(coreListener.getObjectId(), coreListener.getEvent(), coreListener.getValue());
		
		//Check if the need to by register in the core clock implementation
		CoreObjectSpec abstractClock = (CoreObjectSpec)coreClock;
		if(abstractClock.getAbstractObjectId().contentEquals(coreListener.getObjectId()) && eventKey.getVarName().contentEquals("ClockAlarm")  && !eventKey.isEventOnly()) {
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
		
		while(keysIt.hasNext() && !added) {
			Entry key = keysIt.next();
			if(eventKey.equals(key)) {
				eventsListeners.get(key).add(coreListener);
				logger.debug("Add follower to existing listener list");
				added = true;
			}
		}
		
		if(!added) {
			ArrayList<CoreListener> coreListenerList = new ArrayList<CoreListener>();
			coreListenerList.add(coreListener);
			eventsListeners.put(eventKey, coreListenerList);
			logger.debug("Add new event follower list");
		}
		
	}

	@Override
	public synchronized void deleteListener(CoreListener coreListener) {
		logger.debug("Deleting a listener...");
		Entry eventKey = new Entry(coreListener.getObjectId(), coreListener.getEvent(), coreListener.getValue());
		
		Set<Entry> keys = eventsListeners.keySet();
		Iterator<Entry> keysIt = keys.iterator();
		
		while(keysIt.hasNext()) {
			Entry key = keysIt.next();
			if(eventKey.equals(key)) {
				ArrayList<CoreListener> coreListenerList = eventsListeners.get(key);
				coreListenerList.remove(coreListener);
				if(coreListenerList.isEmpty()) {
					eventsListeners.remove(key);
				}
				Integer alarmId  = alarmListenerList.get(key);
				if(alarmId != null) {
					coreClock.unregisterAlarm(alarmId);
					alarmListenerList.remove(key);
				}
				break;
			}
		}

		logger.debug("Listeners deleted");
	}

	/**
	 * Called by ApAM when Notification message comes
	 * 
	 * @param notif
	 *            the notification message from ApAM
	 */
	@SuppressWarnings("unchecked")
	public void gotNotification(NotificationMsg notif) {
		try {
			logger.debug("Event message receive, " + notif.JSONize());
			JSONObject event = notif.JSONize();
			
			//TODO delete this test when the notification
			//mechanism will be totally define. Use to ignore context notification (device name, place added etc.)
			if(!event.has("objectId")) {
				return;
			}
				
			Entry eventKey = new Entry(event.getString("objectId"), event.getString("varName"), event.getString("value"));
			ArrayList<Entry> keys = new ArrayList<Entry>();
		
			//Copy the listener just to avoid concurrent exeption with program
			//when daemon try to add listener again when they restart
			synchronized(this) {
				Iterator<Entry> tempKeys = eventsListeners.keySet().iterator();
				while(tempKeys.hasNext()){
					keys.add(tempKeys.next());
				}
			}
			
			Iterator<Entry> keysIt = keys.iterator();
			
			while(keysIt.hasNext()) {
				Entry key = keysIt.next();
				if(eventKey.equals(key)) {
					logger.debug("Event is followed, retreiving listeners...");
					
					ArrayList<CoreListener> coreListenerList;
					synchronized(this) {
						//TODO the clone method is used because is necessary if in the notifyEvent
						//override method someone call the deleteListener method that made deadlock
						coreListenerList = (ArrayList<CoreListener>) eventsListeners.get(key).clone();
					}

					Iterator<CoreListener> it = coreListenerList.iterator();
					while(it.hasNext()) {
						it.next().notifyEvent();
						logger.debug("Notify listener.");
					}
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
		 * The kind of entry
		 * "eventValue": for specific value of an event
		 * "eventName" : for a specific event but don't care about the value
		 */
		private String entryType;

		/**
		 * Constructor for an Entry
		 * @param objectId
		 * @param varName
		 * @param value
		 */
		public Entry(String objectId, String varName, String value) {
			this.objectId = objectId;
			this.varName = varName;
			this.value = value;
			if(value.contentEquals("")) {
				entryType = "eventName";
			}else{
				entryType = "eventValue";
			}
		}

		public String getObjectId() {
			return objectId;
		}

		public String getVarName() {
			return varName;
		}

		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
		
		public String getEntryType() {
			return entryType;
		}
		
		/**
		 * Test if the Entry is only considering the event name and
		 * not the event value
		 * @return true if the entry doesn't take in consideration the event value, false otherwise
		 */
		public boolean isEventOnly() {
			return entryType.contentEquals("eventName");
		}

		/**
		 * Override the equals method to compare two entry together but 
		 * compare their contents
		 */
		@Override
		public boolean equals(Object eventEntry) {
			if (eventEntry instanceof Entry) {
				Entry entry = (Entry) eventEntry;
				return ( entry.getObjectId().contentEquals(objectId) &&
						 entry.getVarName().contentEquals(varName) && 
						 (entry.getValue().contentEquals(value)  ||  entry.getValue().contentEquals("")	)
					   );
			}
			return false;
		}

	}
	
	/**
	 * Inner class to register time notification through the core clock
	 * @author Cédric Gérard
	 * @since September 25, 2013
	 * @version 1.0.0
	 */
	private class TimeObserver implements AlarmEventObserver {

		@Override
		public void alarmEventFired(int alarmEventId) {
			logger.debug("Alarm notification fired: "+alarmEventId);
		}
		
	}
	

}
