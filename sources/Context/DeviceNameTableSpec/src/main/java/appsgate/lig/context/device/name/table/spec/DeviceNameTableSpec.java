package appsgate.lig.context.device.name.table.spec;

/**
 * Table that allow users to rename devices and services as they want.
 * The name can be whatever and is use for display.
 * 
 * @author Cedric Gérard
 * @version 1.0.0
 *
 */
public interface DeviceNameTableSpec {

	/**
	 * Add a new name to the hash map
	 * @param objectIds the targeted object
	 * @param usrId which user the name stand for
	 * @param newName the new name for this device
	 */
	public void addName(String objectId, String usrId, String newName);
	
	/**
	 * Delete an object name in the ash map
	 * @param objectId the object to change
	 * @param usrId the user which the name  stand for
	 */
	public void deleteName(String objectId, String usrId);
	
	/**
	 * Get the name give to a device by a specified user
	 * @param objectId the device
	 * @param usrId the user who give the name
	 * @return the user object name of the device
	 */
	public String getName(String objectId, String usrId);
}
