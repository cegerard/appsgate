package appsgate.lig.main.spec;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * AppsGate specification that define all method that a client can remote call
 * to interact with the Appsgate system.
 * 
 * @author Cedric Gérard
 * @version 1.0.0
 *
 */
public interface AppsGateSpec {
	
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
	/**  Location management   */
	/***************************/
	
	/**
	 * Call AppsGate to get all existing location definition.
	 * @return a JSON array that describe each place.
	 */
	public JSONArray getLocations();
	
	/**
	 * Add a new location and move object in it.
	 * @param location the new location description and the list of object to move in
	 */
	public void newLocation(JSONObject location);
	
	/**
	 * Update a location on the smart space
	 * @param location the new location description
	 */
	public void updateLocation(JSONObject location);
	
	/**
	 * Move a device in a specified location
	 * @param objId the object to move
	 * @param srcLocationId the previous location of this object
	 * @param destLocationId the destination of this object
	 */
	public void moveDevice(String objId, String srcLocationId, String destLocationId);
	
	/**
	 * Get the location identifier of a core object
	 * @param objId the core object identifier 
	 * @return the identifier of the location where the core object is placed.
	 */
	public String getCoreObjectLocationId(String objId);
	
	
}
