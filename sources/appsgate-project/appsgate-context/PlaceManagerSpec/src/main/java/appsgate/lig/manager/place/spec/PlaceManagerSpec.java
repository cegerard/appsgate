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
	 * Add a new place to the hash map.
	 * @param name the place name
	 * @param parent the parent of this place, or null if this place is a root
	 * @return the id of the new place null otherwise.
	 */
	public String addPlace(String name, SymbolicPlace parent);
	
	/**
	 * Remove a place from the numeric representation of the smart space.
	 * @param placeId the identifier of the place
	 * @return true if the place has been removed, false otherwise
	 */
	public boolean removePlace(String placeId);
	
	/**
	 * Move a core object to a specify place.
	 * @param objId the core object identifier to move
	 * @param oldPlaceID the source place of the core object
	 * @param newPlaceID the destination place of the core object
	 * @return true if the device has moved, false otherwise
	 */
	public boolean moveObject(String objId, String oldPlaceID, String newPlaceID);
	
	/**
	 * Rename a place on the smart space
	 * @param placeId the identifier of the place to rename
	 * @param newName the new name of the place
	 * @return true if the place name has been updated, false otherwise
	 */
	public boolean renamePlace(String placeId, String newName);
	
	/**
	 * Get the symbolic place object from its identifier
	 * @param placId the place identifier
	 * @return the SymbolicPlace instance
	 */
	public SymbolicPlace getSymbolicPlace(String placId);
	
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
