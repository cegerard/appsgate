package appsgate.lig.manager.location.spec;

import org.json.JSONArray;

/**
 * Specification of services offer by a place manager.
 * 
 * @author Cédric Gérard
 * @since February 26, 2013
 * @version 1.0.0
 *
 */
public interface PlaceManagerSpec {
	
	/**
	 * Add a new place to the numeric representation of the smart space.
	 * @param locationId the place identifier
	 * @param name the place name
	 */
	public void addPlace(String locationId, String name);
	
	/**
	 * Remove a place from the numeric representation of the smart space.
	 * @param locationId the identifier of the place
	 */
	public void removePlace(String locationId);
	
	/**
	 * Move a core object to a specify place.
	 * @param objId the core object identifier to move
	 * @param oldPlaceID the source place of the core object
	 * @param newPlaceID the destination place of the core object
	 */
	public void moveObject(String objId, String oldPlaceID, String newPlaceID);
	
	/**
	 * Rename a location on the smart space
	 * @param locationId the identifier of the place to rename
	 * @param newName the new name of the place
	 */
	public void renameLocation(String locationId, String newName);
	
	/**
	 * Get a JSON formatted representation of the smart space.
	 * @return a JSON array that describe each place.
	 */
	public JSONArray getJSONLocations();
	
	/**
	 * Get the location identifier of a core object
	 * @param objId the core object identifier 
	 * @return the identifier of the location where the core object is placed.
	 */
	public String getCoreObjectLocationId(String objId);
}
