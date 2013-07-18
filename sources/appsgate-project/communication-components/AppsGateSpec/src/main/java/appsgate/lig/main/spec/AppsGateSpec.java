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
	/**  Place management   */
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
	
	
}
