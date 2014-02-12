package appsgate.lig.main.spec;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * AppsGate specification that define all method that a client can remote call
 * to interact with the Appsgate system.
 * 
 * @author Cedric Gérard
 * @since June 19, 2013
 * @version 1.0.0
 *
 */
public interface AppsGateSpec {
	
	/***************************/
	/**   Device management    */
	/***************************/
	
	/**
	 * Get all the devices description
         * @return 
	 */
	public JSONArray getDevices();
	
	/**
	 * Get device details
	 * @param deviceId the targeted device identifier
	 * @return the device description as a JSONObject
	 */
	public JSONObject getDevice(String deviceId);
	
	/**
	 * Get all the devices of a specify user type
	 * @param type the type to filter devices
	 * @return the device list as a JSONArray
	 */
	public JSONArray getDevices(String type);
        
	/**
	 * Return the devices of a list of type presents in the places
	 * 
	 * @param typeList
	 *            the list of types to look for (if empty, return all objects)
	 * @param places
	 *            the places where to find the objects (if empty return all
	 *            places)
	 * @return a list of objects contained in these places
	 */
	public JSONArray getDevicesInSpaces(JSONArray typeList, JSONArray places);

	/**
	 * Return a list of types descending from another types
	 * 
	 * @param typeList
	 *            the list of types to look for (if empty, return all subtypes)
	 * @return an empty array if nothing is found or the array of types
	 */
	public JSONArray getSubtypes(JSONArray typeList);

	
	/***************************/
	/** Device name management */
	/***************************/
	
	/**
	 * Call AppsGate to add a user object name 
	 * @param objectId the object
	 * @param user the user that name this object
	 * @param name the new name of this object
	 */
	public void setUserObjectName(String objectId, String user, String name);
	
	/**
	 * Get the name of an object for a specific user
	 * @param objectId the object
	 * @param user the user who ask
	 * @return the name of the object named by user
	 */
	public String getUserObjectName(String objectId, String user);
	
	/**
	 * Delete an name for an object set by a user
	 * @param objectId the object
	 * @param user the user who give the name to this object
	 */
	public void deleteUserObjectName(String objectId, String user);
	
	/***************************/
	/**    Place management    */
	/***************************/
	
	/**
	 * Call AppsGate to get all existing place definition.
	 * @param habitatID the identifier of the habitat
	 * @return a JSON array that describe each place.
	 */
	public JSONObject getPlaces(String habitatID);
	
	/**
	 * Call AppsGate to get information about a place
	 * @param habitatID the identifier of the habitat
	 * @param placeId the place to get information about
	 * @return place details as a JSONObject
	 */
	public JSONObject getPlaceInfo(String habitatID, String placeId);
	
	
	/***************************/
	/**    Space management    */
	/***************************/
	
	/**
	 * Call AppsGate to get all existing space definition.
	 * @return a JSON array that describe each space.
	 */
	public JSONArray getJSONSpaces();
	
	/**
	 * Call AppsGate to get information about a specific space
	 * @param spaceId the space to get information about
	 * @return space details as a JSONObject
	 */
	public JSONObject getSpaceInfo(String spaceId);
	
	/**
	 * Call AppsGate to get all the spaces that match a specific name
	 * @param name the name to match
	 * @return the spaces with the name <name> as a JSONArray
	 */
	public JSONArray getSpacesByName(String name);
	
	/**
	 * Get spaces that have been tagged with all tags
	 * give in parameter.
	 * @param tags the tags list that spaces have to match
	 * @return spaces as a JSONArray
	 */
	public JSONArray getSpacesWithTags(JSONArray tags);
	
	/**
	 * Get spaces that contains the properties keys in parameters
	 * @param keys all properties that spaces have to be set
	 * @return spaces list as a JSONArray
	 */
	public JSONArray getSpacesWithProperties(JSONArray keys);
	
	/**
	 * Get spaces that contains the properties keys in parameters
	 * and with the corresponding values
	 * @param properties all properties that spaces have to be set with
	 * the corresponding value
	 * @return spaces list as a JSONArray
	 */
	public JSONArray getSpacesWithPropertiesValue(JSONArray properties);
	
	/**
	 * Get the root space description
	 * @return the root space as a JSONObject
	 */
	public JSONObject getRootSpace();
	
	/**
	 * Add a new space and move object in it.
	 * @param space the new space description and the list of object to move in
	 * @return the new space identifier
	 */
	public String newSpace(JSONObject space);
	
	/**
	 * Update a space on the smart space
	 * @param space the new space description
	 * @param true if the space has been updated, false otherwise
	 */
	public boolean updateSpace(JSONObject space);
	
	/**
	 * Remove a space from the smart space
	 * @param id the space identifier
	 * @return true if the space has been removed, false otherwise
	 */
	public boolean removeSpace(String id);
	
	/**
	 * Add a tag to the tag of list of the specified space
	 * @param spaceId the space where to add the tag
	 * @param tag the tag to add
	 * @return true if the tag has been added, false otherwise
	 */
	public boolean addTag(String spaceId, String tag);
	
	/**
	 * Remove a tag from a space
	 * @param spaceId the space from where to remove the tag
	 * @param tag the tag to remove
	 * @return true if the tag has been removed, false otherwise
	 */
	public boolean removeTag(String spaceId, String tag);
	
	/**
	 * Add a property to a specified space
	 * @param spaceId the space where to add the property
	 * @param key the key of the property to add
	 * @param value the value of the property to add
	 * @return true f the property has been added, false otherwise
	 */
	public boolean addProperty(String spaceId, String key, String value);
	
	/**
	 * Remove a property from a specified space
	 * @param spaceId the space from where to remove the property
	 * @param key the key of the property that have to be removed
	 * @return true if the property is removed, false otherwise
	 */
	public boolean removeProperty(String spaceId, String key);
	
	
	/***************************/
	/**  End User management   */
	/***************************/
	
	/**
	 * Get the end user list
	 * @return the user list as a JSONArray
	 */
	public JSONArray getUsers();
	
	/**
	 * Create a new end user
	 * @param id the user identifier
	 * @param password the user password
	 * @param lastName the user last name
	 * @param firstName the user first name
	 * @param role the user role
	 * @return true if the user is created, false otherwise
	 */
	public boolean createUser(String id, String password, String lastName, String firstName, String role);
	
	/**
	 * Delete an existing end user
	 * @param id the identifier of the user to be deleted
	 * @param password the corresponding password
	 * @return true if the user has been deleted, false otherwise
	 */
	public boolean deleteUser(String id, String password);
	
	/**
	 * Get details on a specify user
	 * @param id the identifier of the user
	 * @return user details as a JSONObject
	 */
	public JSONObject getUserDetails(String id);
	
	/**
	 * Get all information on a specify user
	 * @param id the identifier of the user
	 * @return user information as a JSONObject
	 */
	public JSONObject getUserFullDetails(String id);
	
	/**
	 * Check if the wanted identifier already existing.
	 * @param id the identifier to check
	 * @return true if the identifier is not use, false otherwise
	 */
	public boolean checkIfIdIsFree(String id);
	
	/**
	 * Synchronize a web service account with an end user profile
	 * @param id the end user identifier
	 * @param password the end user password
	 * @param accountDetails all service account needed to be connected
	 * @return true if the service account has been synchronized, false otherwise
	 */
	public boolean synchronizeAccount(String id, String password, JSONObject accountDetails);
	
	/**
	 * delete service account synchronization
	 * @param id the end user identifier
	 * @param password the end user password
	 * @param accountDetails all information needed to removed connection
	 * @return true it the synchronization has been canceled, false otherwise.
	 */
	public boolean desynchronizedAccount(String id, String password, JSONObject accountDetails);
	
	/**
	 * Associate a device to an end user
	 * @param id the end user identifier
	 * @param password the end user password
	 * @param deviceId the device identifier
	 * @return true if the association has been completed, false otherwise
	 */
	public boolean associateDevice(String id, String password, String deviceId);
	
	/**
	 * Remove end user and device association
	 * @param id the end user identifier
	 * @param password the end user password
	 * @param deviceId the device identifier
	 * @return true if the association has been deleted, false otherwise
	 */
	public boolean separateDevice(String id, String password, String deviceId);
	
	/************************************/
	/**   End User programs management  */
	/************************************/
	
	/**
	 * Deploy a new end user program in Appsgate system
	 * @param jsonProgram the JSONtree of the program
	 * @return true if the program has been deployed, false otherwise
	 */
	public boolean addProgram(JSONObject jsonProgram);
	
	/**
	 * Remove a currently deployed program.
	 * Stop it, if it is running.
	 * @param programId the identifier of the program to remove.
	 * @return true if the program has been removed, false otherwise.
	 */
	public boolean removeProgram(String programId);
	
	/**
	 * Update an existing program
	 * @param jsonProgram the JSONtree of the program
	 * @return true if the program has been updated, false otherwise
	 */
	public boolean updateProgram(JSONObject jsonProgram);
	
	/**
	 * Run a deployed end user program 
	 * @param programId the identifier of the program to run
	 * @return true if the program has been launched, false otherwise
	 */
	public boolean callProgram(String programId);
	
	/**
	 * Stop a deployed program execution
	 * @param programId identifier of the program
	 * @return true if the program has been stopped, false otherwise
	 */
	public boolean stopProgram(String programId);
	
	/**
	 * Stop the program but keep its current state
	 * @param programId the identifier of the program
	 * @return true if the program has been paused, false otherwise
	 */
	public boolean pauseProgram(String programId);
	
	/**
	 * Get the list of current deployed programs
	 * @return the programs list as a JSONArray
	 */
	public JSONArray getPrograms();
	
	/**
	 * Check if a program is active or not
	 * 
	 * @param programId the identifier of the program
	 * @return true if the program is active (STARTED), false otherwise
	 */
	public boolean isProgramActive(String programId);
	
	/************************************/
	/**    General Appsgate commands    */
	/************************************/
	
	/**
	 * Shutdown the Appsgate system
	 * (Shutdown the OSGi distribution)
	 */
	public void shutdown();
	
	/**
	 * restart the Appsgate system
	 * (Restart the system bundle from OSGi)
	 */
	public void restart();
	
}
