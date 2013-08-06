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
	 */
	public JSONArray getDevices();
	
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
	 * @return a JSON array that describe each place.
	 */
	public JSONArray getPlaces();
	
	/**
	 * Add a new place and move object in it.
	 * @param place the new place description and the list of object to move in
	 */
	public void newPlace(JSONObject place);
	
	/**
	 * Update a place on the smart space
	 * @param place the new place description
	 */
	public void updatePlace(JSONObject place);
	
	/**
	 * Move a device in a specified place
	 * @param objId the object to move
	 * @param srcPlaceId the previous place of this object
	 * @param destPlaceId the destination of this object
	 */
	public void moveDevice(String objId, String srcPlaceId, String destPlaceId);
	
	/**
	 * Get the place identifier of a core object
	 * @param objId the core object identifier 
	 * @return the identifier of the place where the core object is placed.
	 */
	public String getCoreObjectPlaceId(String objId);
	
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
	
}
