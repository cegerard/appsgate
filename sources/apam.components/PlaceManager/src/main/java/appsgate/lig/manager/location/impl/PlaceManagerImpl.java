package appsgate.lig.manager.location.impl;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.communication.service.send.SendWebsocketsService;
import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;
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
	private HashMap<String, Location> locationObjectsMap;

	/**
	 * Called by ApAM when all dependencies are available
	 */
	public void newInst() {
		logger.info("Place manager started.");
		locationObjectsMap = new HashMap<String, Location>();
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
			locationObjectsMap.put(locationId, new Location(locationId, name));
			notifyPlace(locationId, name, 0);
		}
	}

	/**
	 * Remove the designed place
	 * 
	 * @param locationId
	 *            , the place to removed
	 */
	public synchronized void removePlace(String locationId) {
		Location loc = locationObjectsMap.get(locationId);
		loc.removeAll();
		locationObjectsMap.remove(locationId);
	}

	/**
	 * Add an object to the designed place.
	 * 
	 * @param obj
	 *            , the object to locate
	 * @param locationId
	 *            , the target place
	 */
	private synchronized void addObject(AbstractObjectSpec obj,
			String locationId) {
		Location loc = locationObjectsMap.get(locationId);
		loc.addObject(obj);
	}

	/**
	 * Move the object obj from oldPlace to the newPlace.
	 * 
	 * @param obj
	 *            , the object to move.
	 * @param oldPlaceId
	 *            , the former place where obj was located
	 * @param newPlaceId
	 *            , the new place where obj is locate.
	 */
	public synchronized void moveObject(AbstractObjectSpec obj,
			String oldPlaceID, String newPlaceID) {

		int locId = obj.getLocationId();

		if (locId == -1) {
			addObject(obj, newPlaceID);
		} else {
			Location oldLoc = locationObjectsMap.get(oldPlaceID);
			Location newLoc = locationObjectsMap.get(newPlaceID);
			oldLoc.removeObject(obj);
			newLoc.addObject(obj);
		}

		notifyMove(oldPlaceID, newPlaceID, obj);
	}

	/**
	 * Rename the location
	 */
	public synchronized void renameLocation(String locationId, String newName) {
		Location loc = locationObjectsMap.get(locationId);
		loc.setName(newName);
		
		notifyPlace(locationId, newName, 1);
	}

	/**
	 * Get the JSON tab of all location
	 * 
	 * @return, the JSONArray
	 */
	public synchronized JSONArray getJSONLocations() {
		Iterator<Location> locations = locationObjectsMap.values().iterator();
		JSONArray jsonLocationList = new JSONArray();
		Location loc;

		while (locations.hasNext()) {
			loc = locations.next();
			jsonLocationList.put(loc.getDescription());
		}

		return jsonLocationList;
	}

	private void notifyMove(String oldLocationId, String newLocationId, AbstractObjectSpec object) {
		//TODO use this line when the notifications system works
		//notifyChanged(new MoveObjectNotification(oldLocationId, newLocationId, object));
		try {
			sendToClientService.send(new MoveObjectNotification(oldLocationId, newLocationId, object).JSONize().toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void notifyPlace(String locationId, String locationName, int type) {
		//TODO use this line when the notification system works
		//notifyChanged(new PlaceManagerNotification(locationId, locationName, type));
		try {
			sendToClientService.send(new PlaceManagerNotification(locationId, locationName, type).JSONize().toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method notify ApAM that a new notification message has been produced.
	 * @param notif, the notification message to send.
	 * @return nothing it just notify ApAM.
	 */
	public NotificationMsg notifyChanged (NotificationMsg notif) {
		return notif;
	}
	
	/**
	 * Service to communicate with clients (TEMP)
	 */
	//TODO remove this class member when Adele commit the message fix.
	private SendWebsocketsService sendToClientService;

}
