package appsgate.lig.manager.location.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.history.services.DataBasePullService;
import appsgate.lig.context.history.services.DataBasePushService;
import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.manager.location.messages.MoveObjectNotification;
import appsgate.lig.manager.location.messages.PlaceManagerNotification;
import appsgate.lig.manager.location.spec.PlaceManagerSpec;

/**
 * This ApAM component is used to maintain location information for any object
 * in the smart habitat.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 26, 2013
 * 
 */
public class PlaceManagerImpl implements PlaceManagerSpec {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(PlaceManagerImpl.class);

	/**
	 * This is the hash map to match the location of devices.
	 */
	private HashMap<String, SymbolicLocation> locationObjectsMap;
	
	/**
	 * Context history pull service to get past locations state
	 */
	private DataBasePullService contextHistory_pull;
	
	/**
	 * Context history push service to save the current locations
	 */
	private DataBasePushService contextHistory_push;

	/**
	 * Called by ApAM when all dependencies are available
	 */
	public void newInst() {
		logger.info("Place manager started.");
		locationObjectsMap = new HashMap<String, SymbolicLocation>();
		
		JSONObject locationMap = contextHistory_pull.pullLastObjectVersion(this.getClass().getSimpleName());
		if(locationMap != null){
			try {
				JSONArray state = locationMap.getJSONArray("state");
				int length = state.length();
				int i = 0;
				
				while(i < length) {
					JSONObject obj = state.getJSONObject(i);
					
					String locationId = (String)obj.keys().next();
					JSONObject jsonLocation = new JSONObject(obj.getString(locationId));
					SymbolicLocation loc = new SymbolicLocation(locationId, jsonLocation.getString("name"));
					
					JSONArray objects = jsonLocation.getJSONArray("devices");
					
					int l = objects.length();
					int j = 0;
					
					while(j < l) {
						loc.addObject(objects.getString(j));
						j++;
					}

					locationObjectsMap.put(locationId, loc);
					i++;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		logger.debug("The place manager has been initialized");
	}

	/**
	 * Called by ApAM when the component is not available
	 */
	public void deleteInst() {
		logger.info("Removing place manager...");
	}

	/**
	 * Add a new place to the hash map.
	 * 
	 * @param locationId
	 *            , the id of the new location
	 * @param name
	 *            , the place name
	 */
	public synchronized void addPlace(String locationId, String name) {
		if (!locationObjectsMap.containsKey(locationId)) {
			locationObjectsMap.put(locationId, new SymbolicLocation(locationId, name));
			notifyPlace(locationId, name, 0);
			
			// save the new devices name table 
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
			
			Set<String> keys = locationObjectsMap.keySet();
			for(String e : keys) {
				SymbolicLocation sl = locationObjectsMap.get(e);
				properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}
			
			contextHistory_push.pushData_add(this.getClass().getSimpleName(), locationId, name, properties);
		}
	}

	/**
	 * Remove the designed place
	 * 
	 * @param locationId
	 *            , the place to removed
	 */
	public synchronized void removePlace(String locationId) {
		SymbolicLocation loc = locationObjectsMap.get(locationId);
		loc.removeAll();
		locationObjectsMap.remove(locationId);
		
		// save the new devices name table 
		ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
					
		Set<String> keys = locationObjectsMap.keySet();
		for(String e : keys) {
			SymbolicLocation sl = locationObjectsMap.get(e);
			properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
		}
					
		contextHistory_push.pushData_remove(this.getClass().getSimpleName(), locationId, loc.getName(), properties);		
	}

	/**
	 * Add an object to the designed place.
	 * 
	 * @param obj
	 *            , the object to locate
	 * @param locationId
	 *            , the target place
	 */
	private synchronized void addObject(String objId,
			String locationId) {
		SymbolicLocation loc = locationObjectsMap.get(locationId);
		loc.addObject(objId);
		
		// save the new devices name table 
		ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
							
		Set<String> keys = locationObjectsMap.keySet();
		for(String e : keys) {
			SymbolicLocation sl = locationObjectsMap.get(e);
			properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
		}
							
		contextHistory_push.pushData_add(this.getClass().getSimpleName(), locationId, objId, properties);
		
	}

	/**
	 * Move the object obj from oldPlace to the newPlace.
	 * 
	 * @param obj
	 *            , the object to move.
	 * @param oldPlaceID
	 *            , the former place where obj was located
	 * @param newPlaceID
	 *            , the new place where obj is locate.
	 */
	public synchronized void moveObject(String objId,
			String oldPlaceID, String newPlaceID) {
		
		if (oldPlaceID.contentEquals("-1")) {
			addObject(objId, newPlaceID);
		} else {
			SymbolicLocation oldLoc = locationObjectsMap.get(oldPlaceID);
			SymbolicLocation newLoc = locationObjectsMap.get(newPlaceID);
			oldLoc.removeObject(objId);
			newLoc.addObject(objId);
		}
		notifyMove(oldPlaceID, newPlaceID, objId);
		
		// save the new devices name table 
		ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
									
		Set<String> keys = locationObjectsMap.keySet();
		for(String e : keys) {
			SymbolicLocation sl = locationObjectsMap.get(e);
			properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
		}
									
		contextHistory_push.pushData_change(this.getClass().getSimpleName(), objId, oldPlaceID, newPlaceID, properties);
	}

	/**
	 * Rename the location
	 */
	public synchronized void renameLocation(String locationId, String newName) {
		SymbolicLocation loc = locationObjectsMap.get(locationId);
		String oldName = loc.getName();
		loc.setName(newName);
		
		notifyPlace(locationId, newName, 1);
		
		// save the new devices name table 
		ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
							
		Set<String> keys = locationObjectsMap.keySet();
		for(String e : keys) {
			SymbolicLocation sl = locationObjectsMap.get(e);
			properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
		}
							
		contextHistory_push.pushData_change(this.getClass().getSimpleName(), locationId, oldName, newName, properties);		
	}

	/**
	 * Get the JSON tab of all location
	 * 
	 * @return all the location as a JSONArray
	 */
	public synchronized JSONArray getJSONLocations() {
		Iterator<SymbolicLocation> locations = locationObjectsMap.values().iterator();
		JSONArray jsonLocationList = new JSONArray();
		SymbolicLocation loc;

		while (locations.hasNext()) {
			loc = locations.next();
			jsonLocationList.put(loc.getDescription());
		}

		return jsonLocationList;
	}

	/**
	 * Use to notify that a AbstractObject has moved.
	 * 
	 * @param oldLocationId the former AbstractObject location
	 * @param newLocationId the new AbstractObject location
	 * @param object the AbstractObject which has been moved.
	 */
	private void notifyMove(String oldLocationId, String newLocationId, String objId) {
		notifyChanged(new MoveObjectNotification(oldLocationId, newLocationId, objId));
	}
	
	/**
	 * Use to notify that new place has been created or has changed
	 * 
	 * @param locationId the location identifier
	 * @param locationName the user name of this location
	 * @param type indicate if this notification is a place creation (0) or an update (1)
	 */
	private void notifyPlace(String locationId, String locationName, int type) {
		notifyChanged(new PlaceManagerNotification(locationId, locationName, type));
	}
	
	/**
	 * This method notify ApAM that a new notification message has been produced.
	 * @param notif the notification message to send.
	 * @return nothing it just notify ApAM.
	 */
	public NotificationMsg notifyChanged (NotificationMsg notif) {
		logger.debug("Location Notify: "+ notif);
		return notif;
	}

	/**
	 * Get the location identifier of the core object give in parameter.
	 * @param the object from which get the location
	 */
	@Override
	public String getCoreObjectLocationId(String objId) {
		Iterator<SymbolicLocation>  it = locationObjectsMap.values().iterator();
		
		SymbolicLocation loc = null;
		boolean found = false;
		
		while(it.hasNext() && !found) {
			
			loc = it.next();
			if(loc.isHere(objId)) {
				found = true;
			}
		}
		
		if(found) {
			return loc.getId();
		} else {
			return "-1";
		}
	}

}
