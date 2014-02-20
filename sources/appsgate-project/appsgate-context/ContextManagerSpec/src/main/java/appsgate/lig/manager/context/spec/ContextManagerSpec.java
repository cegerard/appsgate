package appsgate.lig.manager.context.spec;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;

import appsgate.lig.manager.space.spec.subSpace.Space;
import appsgate.lig.manager.space.spec.subSpace.Space.TYPE;
/**
 * Specification of services offer by a space manager.
 * 
 * @author Cédric Gérard
 * @since February 26, 2013
 * @version 1.0.0
 *
 */
public interface ContextManagerSpec {
	
	/**
	 * Add a new space to the hash map.
	 * @param type the space type
	 * @param parent the parent id of this space, or null if this space is a root
	 * @return the id of the new space null otherwise.
	 */
	public String addSpace(TYPE type, Space parent);
	
	/**
	 * Add a new space to the hash map.
	 * @param type the space type
	 * @param properties the properties list to associated to this space
	 * @param parent the parent id of this space, or null if this space is a root
	 * @return the id of the new space null otherwise.
	 */
	public String addSpace(TYPE type, HashMap<String, String> properties, Space parent);
	
	/**
	 * Add a new space with tags and properties
	 * @param type the space type
	 * @param tags the tags list to associated to this space
	 * @param properties the properties list to associated to this space
	 * @param parent the parent space for this space, or null if it is a root
	 * @return the id of the new space null otherwise.
	 */
	public String addSpace(TYPE type, ArrayList<String> tags, HashMap<String, String> properties,  Space parent);
	
	/**
	 * Add a complete space
	 * @param type the space type
	 * @param tags the tags list to associated to this space
	 * @param properties the properties list to associated to this space
	 * @param parent the parent space for this space, or null if it is a root
	 * @param children the sub-spaces list
	 * @return the id of the new space null otherwise.
	 */
	public String addSpace(TYPE type, ArrayList<String> tags, HashMap<String, String> properties, Space parent, ArrayList<Space> children);
	
	/***********************************/
	/**	BEGIN User Space dedicated API */
	/***********************************/
	/**
	 * Add a new user space to the hash map.
	 * @param parent the parent id of this space, or null if this space is a root
	 * @param pwd the user password
	 * @return the id of the new space null otherwise.
	 */
	public String addUserSpace(Space parent, String pwsd);
	
	/**
	 * Add a new space to the hash map.
	 * @param properties the properties list to associated to this space
	 * @param parent the parent id of this space, or null if this space is a root
	 * @param pwd the user password
	 * @return the id of the new space null otherwise.
	 */
	public String addUserSpace(HashMap<String, String> properties, Space parent, String pwd);
	
	/**
	 * Add a complete space
	 * @param tags the tags list to associated to this space
	 * @param properties the properties list to associated to this space
	 * @param parent the parent space for this space, or null if it is a root
	 * @param children the sub-spaces list
	 * @param the password of the user
	 * @return the id of the new space null otherwise.
	 */
	public String addUserSpace(ArrayList<String> tags, HashMap<String, String> properties, Space parent, ArrayList<Space> children, String pwd);
	
	/*********************************/
	/**	END User Space dedicated API */
	/*********************************/
	
	/**
	 * Remove a space and attached its children to its parent
	 * @param space the space to remove
	 * @return true if the space has been removed, false otherwise
	 */
	public boolean removeSpace(Space space);
	
	/**
	 * Remove a space and all its children
	 * @param space the root of the tree to remove
	 * @return true if the tree has been removed completely, false otherwise
	 */
	public boolean removeTree(Space space);
	
	/**
	 * Remove a space and all its children if they can be removed, otherwise children are
	 * attached to its parent
	 * @param space the space to remove
	 * @return true if the space has been removed, false otherwise
	 */
	public boolean removeSpaceAndUserChildren(Space space);
	
	/**
	 * Move this space under a new parent space
	 * @param space the space that move
	 * @param newParent the new parent space
	 * @return true if the space has been moved, false otherwise
	 */
	public boolean moveSpace(Space space, Space newParent);
	
	/**
	 * Get the root space of the hierarchy
	 * @return the root space reference
	 */
	public Space getRootSpace();
	
	/**
	 * Get the current habitat space
	 * @return the space that correspond to the AppsGate box location
	 */
	public Space getCurrentHabitat();
	
	/**
	 * Get the device root node from a specific habitat
	 * precondition: the space need to be a habitat 
	 * @param habitat the habitat
	 * @return the device space
	 */
	public Space getDeviceRoot(Space habitat);
	
	/**
	 * Get the service root node
	 * @param habitat the habitat
	 * @return the device space
	 */
	public Space getServiceRoot(Space habitat);
	
	/**
	 * Get the programs root node from a specific habitat
	 * precondition: the space need to be a habitat 
	 * @param habitat the habitat
	 * @return the program space
	 */
	public Space getProgramRoot(Space habitat);
	
	/**
	 * Get the spatial root node from a specific habitat
	 * precondition: the space need to be a habitat 
	 * @param habitat the habitat
	 * @return the spatial space
	 */
	public Space getSpatialRoot(Space habitat);
	
	/**
	 * Get the users root node
	 * @return the user space
	 */
	public Space getUserRoot();

	/**
	 * Get the symbolic space object from its identifier
	 * @param spaceId the space identifier
	 * @return the Space instance
	 */
	public Space getSpace(String spaceId);
	
	/**
	 * Get all the spaces in planar view
	 * @return spaces as an ArrayList<Space>
	 */
	public ArrayList<Space> getSpaces();
	
	/**
	 * Get all space that match the type in parameter
	 * @param type the space type
	 * @return An ArrayList of space
	 */
	public ArrayList<Space> getSpacesWithType(TYPE type);
	
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
	
	/**
	 * Get the tree representation of all spaces
	 * @return the tree representation as a JSONObject
	 */
	public JSONObject getTreeDescription();
	
	/**
	 * Get the sub-tree of all spaces from
	 * the space give in parameter
	 * @param root the root of the sub tree
	 * @return the tree as a JSONObject
	 */
	public JSONObject getTreeDescription(Space root);
	
	/**
	 * Notify on update of the space model that trigger ApAM 
	 * notification message.
	 * @param update the update as a JSONObject
	 */
	public void spaceUpdated(JSONObject update);
	
}
