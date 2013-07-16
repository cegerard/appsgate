package appsgate.lig.main.spec;

/**
 * AppsGate specification that define all method that a client can remote call
 * to interact with the Appsgate system.
 * 
 * @author Cedric Gérard
 * @version 1.0.0
 *
 */
public interface AppsGateSpec {
	
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
	
}
