package appsgate.lig.manager.space.spec;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;

import appsgate.lig.manager.space.spec.Space.CATEGORY;

/**
 * Specification of services offer by a space manager.
 * 
 * @author Cédric Gérard
 * @since February 26, 2013
 * @version 1.0.0
 *
 */
public interface SpaceManagerSpec {
	
	/**
	 * Add a new space to the hash map.
	 * @param name the space name
	 * @param parent the parent id of this space, or null if this space is a root
	 * @return the id of the new space null otherwise.
	 */
	public String addSpace(String name, String category,  String parent);
	
	/**
	 * Add a new space with tags and properties
	 * @param name the space name
	 * @param category the category of this space
	 * @param tags the tags list to associated to this space
	 * @param properties the properties list to associated to this space
	 * @param parent the parent space for this space, or null if it is a root
	 * @return the id of the new space null otherwise.
	 */
	public String addSpace(String name, String category, ArrayList<String> tags, HashMap<String, String> properties,  String parent);
	
	/**
	 * Add a complete space
	 * @param name the space name
	 * @param tags the tags list to associated to this space
	 * @param properties the properties list to associated to this space
	 * @param parent the parent space for this space, or null if it is a root
	 * @param children the sub-spaces list
	 * @return the id of the new space null otherwise.
	 */
	public String addSpace(String name, ArrayList<String> tags, HashMap<String, String> properties,  String parent, ArrayList<Space> children);
	
	
	/**
	 * Remove a space.
	 * @param spaceId the identifier of the space
	 * @return true if the space has been removed, false otherwise
	 */
	public boolean removeSpace(String spaceId);
	
	/**
	 * Move this space under a new parent space
	 * @param spaceId the space id of the space that move
	 * @param newParent the identifier of the  new parent space
	 * @return true if the space has been moved, false otherwise
	 */
	public boolean moveSpace(String spaceId, String newParentId);
	
	/**
	 * Set the tags list of this space
	 * @param spaceId the space identifier
	 * @param tags the new tags list
	 */
	public void setTagsList(String spaceId, ArrayList<String> tags);
	
	/**
	 * Empty the list of tag of this space
	 * @param spaceId the space identifier
	 */
	public void clearTagsList(String spaceId);
	
	/**
	 * Add a new tag to this space
	 * @param spaceId the space identifier
	 * @param tag the new tag to add
	 * @return true if the tag has been add, false otherwise
	 */
	public boolean addTag(String spaceId, String tag);
	
	/**
	 * remove a tag from this space
	 * @param spaceId the space identifier
	 * @param tag the tag to remove
	 * @return true if the tag has been removed, false otherwise
	 */
	public boolean removeTag(String spaceId, String tag);
	
	/**
	 * Set the properties of this space
	 * @param spaceId the space identifier
	 * @param properties the new properties list
	 */
	public void setProperties(String spaceId, HashMap<String, String> properties);
	
	/**
	 * Empty the properties list of this space
	 * @param spaceId the space identifier
	 */
	public void clearPropertiesList(String spaceId);
	
	/**
	 * Add a new property to the list or update an existing one
	 * @param spaceId the space identifier 
	 * @param key the key of the property
	 * @param value the value to set
	 * @return true if key is a newly added false if the key exist and the value has been changed
	 */
	public boolean addProperty(String spaceId, String key, String value);
	
	/**
	 * Remove an existing property
	 * @param spaceId the space identifier
	 * @param key the key of the property to remove
	 * @return true if the property has been removed, false otherwise
	 */
	public boolean removeProperty(String spaceId, String key);
	
	/**
	 * Rename a space on the smart space
	 * @param spaceId the identifier of the space to rename
	 * @param newName the new name of the space
	 * @return true if the space name has been updated, false otherwise
	 */
	public boolean renameSpace(String spaceId, String newName);
	
	/**
	 * Get the root space of the hierarchy
	 * @return the root space reference
	 */
	public Space getRootSpace();
	
	/**
	 * Get the symbolic space object from its identifier
	 * @param spaceId the space identifier
	 * @return the Space instance
	 */
	public Space getSpace(String spaceId);
	
	/**
	 * Get all the spaces
	 * @return spaces as an ArrayList<Space>
	 */
	public ArrayList<Space> getSpaces();
	
	/**
	 * Get a JSON formatted representation of the smart space.
	 * @return a JSON array that describe each space.
	 */
	public JSONArray getJSONSpaces();
	
	/**
	 * Get all the spaces the have the name in parameter
	 * @param name the name of the spaces to get
	 * @return spaces as an ArrayList<Symbolicspace>
	 */
	public ArrayList<Space> getSpacesWithName(String name);
	
	/**
	 * Get all the spaces that are tagged with the tags list in parameter
	 * @param tags the tags list to check 
	 * @return spaces as an ArrayList<Symbolicspace>
	 */
	public ArrayList<Space> getSpacesWithTags(ArrayList<String> tags);
	
	/**
	 * Get all the spaces that have the properties list set
	 * @param propertiesKey the properties key list to check 
	 * @return spaces as an ArrayList<Symbolicspace>
	 */
	public ArrayList<Space> getSpacesWithProperties(ArrayList<String> propertiesKey);
	
	/**
	 * Get all the spaces that have the properties list set the specific value
	 * @param properties the properties list to check 
	 * @return spaces as an ArrayList<Symbolicspace>
	 */
	public ArrayList<Space> getSpacesWithPropertiesValue(HashMap<String, String> properties);
	
}
