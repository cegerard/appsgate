package appsgate.lig.manager.place.spec;

import java.util.ArrayList;
import java.util.HashMap;

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
	 * @param parent the parent id of this place, or null if this place is a root
	 * @return the id of the new place null otherwise.
	 */
	public String addPlace(String name, String parent);
	
	/**
	 * Remove a place from the numeric representation of the smart space.
	 * @param placeId the identifier of the place
	 * @return true if the place has been removed, false otherwise
	 */
	public boolean removePlace(String placeId);
	
	/**
	 * Move this place under a new parent place
	 * @param placeId the place id of the place that move
	 * @param newParent the identifier of the  new parent place
	 * @return true if the place has been moved, false otherwise
	 */
	public boolean movePlace(String placeId, String newParentId);
	
	/**
	 * Set the tags list of this place
	 * @param placeId the place identifier
	 * @param tags the new tags list
	 */
	public void setTagsList(String placeId, ArrayList<String> tags);
	
	/**
	 * Empty the list of tag of this place
	 * @param placeId the place identifier
	 */
	public void clearTagsList(String placeId);
	
	/**
	 * Add a new tag to this place
	 * @param placeId the place identifier
	 * @param tag the new tag to add
	 * @return true if the tag has been add, false otherwise
	 */
	public boolean addTag(String placeId, String tag);
	
	/**
	 * remove a tag from this place
	 * @param placeId the place identifier
	 * @param tag the tag to remove
	 * @return true if the tag has been removed, false otherwise
	 */
	public boolean removeTag(String placeId, String tag);
	
	/**
	 * Set the properties of this place
	 * @param placeId the place identifier
	 * @param properties the new properties list
	 */
	public void setProperties(String placeId, HashMap<String, String> properties);
	
	/**
	 * Empty the properties list of this place
	 * @param placeId the place identifier
	 */
	public void clearPropertiesList(String placeId);
	
	/**
	 * Add a new property to the list or update an existing one
	 * @param placeId the place identifier 
	 * @param key the key of the property
	 * @param value the value to set
	 * @return true if key is a newly added false if the key exist and the value has been changed
	 */
	public boolean addProperty(String placeId, String key, String value);
	
	/**
	 * Remove an existing property
	 * @param placeId the place identifier
	 * @param key the key of the property to remove
	 * @return true if the property has been removed, false otherwise
	 */
	public boolean removeProperty(String placeId, String key);
	
	/**
	 * Move a core object to a specify place. If newPalceID attribute equal -1
	 * the object is just removed from this place 
	 * @param objId the core object identifier to move
	 * @param oldPlaceID the source place of the core object
	 * @param newPlaceID the destination place of the core object
	 * @return true if the device has moved, false otherwise
	 */
	public boolean moveObject(String objId, String oldPlaceID, String newPlaceID);
	
	/**
	 * Move all object to the -1 id place
	 * @param placeId that will by emptied of its core object
	 */
	public void removeAllCoreObject(String placeId);
	
	/**
	 * Rename a place on the smart space
	 * @param placeId the identifier of the place to rename
	 * @param newName the new name of the place
	 * @return true if the place name has been updated, false otherwise
	 */
	public boolean renamePlace(String placeId, String newName);
	
	/**
	 * Get the root place of the hierarchy
	 * @return the root place reference
	 */
	public SymbolicPlace getRootPlace();
	
	/**
	 * Get the symbolic place object from its identifier
	 * @param placeId the place identifier
	 * @return the SymbolicPlace instance
	 */
	public SymbolicPlace getSymbolicPlace(String placeId);
	
	/**
	 * Get all the places
	 * @return places as an ArrayList<SymbolicPlace>
	 */
	public ArrayList<SymbolicPlace> getPlaces();
	
	/**
	 * Get a JSON formatted representation of the smart space.
	 * @return a JSON array that describe each place.
	 */
	public JSONArray getJSONPlaces();
	
	/**
	 * Get all the places the have the name in parameter
	 * @param name the name of the places to get
	 * @return places as an ArrayList<SymbolicPlace>
	 */
	public ArrayList<SymbolicPlace> getPlacesWithName(String name);
	
	/**
	 * Get all the places that are tagged with the tags list in parameter
	 * @param tags the tags list to check 
	 * @return places as an ArrayList<SymbolicPlace>
	 */
	public ArrayList<SymbolicPlace> getPlacesWithTags(ArrayList<String> tags);
	
	/**
	 * Get all the places that have the properties list set
	 * @param propertiesKey the properties key list to check 
	 * @return places as an ArrayList<SymbolicPlace>
	 */
	public ArrayList<SymbolicPlace> getPlacesWithProperties(ArrayList<String> propertiesKey);
	
	/**
	 * Get all the places that have the properties list set the specific value
	 * @param properties the properties list to check 
	 * @return places as an ArrayList<SymbolicPlace>
	 */
	public ArrayList<SymbolicPlace> getPlacesWithPropertiesValue(HashMap<String, String> properties);
	
	/**
	 * Get the place identifier of a core object
	 * @param objId the core object identifier 
	 * @return the identifier of the place where the core object is placed.
	 */
	public String getCoreObjectPlaceId(String objId);
}
