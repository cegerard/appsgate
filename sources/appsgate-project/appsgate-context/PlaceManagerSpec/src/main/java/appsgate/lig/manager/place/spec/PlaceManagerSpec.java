package appsgate.lig.manager.place.spec;

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
	 * @param placeId the place identifier
	 * @param name the place name
	 */
	public void addPlace(String placeId, String name);
	
	/**
	 * Remove a place from the numeric representation of the smart space.
	 * @param placeId the identifier of the place
	 */
	public void removePlace(String placeId);
	
	/**
	 * Move a core object to a specify place.
	 * @param objId the core object identifier to move
	 * @param oldPlaceID the source place of the core object
	 * @param newPlaceID the destination place of the core object
	 */
	public void moveObject(String objId, String oldPlaceID, String newPlaceID);
	
	/**
	 * Rename a place on the smart space
	 * @param placeId the identifier of the place to rename
	 * @param newName the new name of the place
	 */
	public void renamePlace(String placeId, String newName);
	
	/**
	 * Get a JSON formatted representation of the smart space.
	 * @return a JSON array that describe each place.
	 */
	public JSONArray getJSONPlaces();
	
	/**
	 * Get the place identifier of a core object
	 * @param objId the core object identifier 
	 * @return the identifier of the place where the core object is placed.
	 */
	public String getCoreObjectPlaceId(String objId);
}
