package appsgate.lig.context.userbase.spec;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author Cedric Gerard
 * @since  July 23, 2013
 * @version 1.0.0
 *
 */
public interface UserBaseSpec {
	
	/**
	 * Add a user to the Appsgate user base
	 * @param id the login for this user (must be unique)
	 * @param password the password to be authenticate
	 * @param LastName the user last name
	 * @param firstName the user first name
	 * @param role the user role in this home
	 * @return true if the user is correctly added, false otherwise
	 */
	public boolean adduser(String id, String password, String LastName, String firstName, String role);
	
	/**
	 * Remove a user from the Appsgate user base.
	 * @param id the identifier of the user to remove
	 * @param password the password of the user to remove
	 * @return true if the user is removed, false otherwise
	 */
	public boolean removeUser(String id, String password);
	
	/**
	 * Get the user list as JSON description
	 * @return the user list as JSONArray
	 */
	public JSONArray getUsers();
	
	/**
	 * Get a user details
	 * @param id the user identifier
	 * @return the user details as JSONObject
	 */
	public JSONObject getUserDetails(String id);
	
	/**
	 * Check if the id is already use or not
	 * @param id the candidate identifier
	 * @return true if the identifier is not use, false otherwise
	 */
	public boolean checkIfIdIsFree(String id);
	
	/**
	 * Add a service account to this user account
	 * @param id the user identifier
	 * @param password the user password
	 * @param accountDetails all account detail as JSONObject
	 * @return true is the account is correctly added.
	 */
	public boolean addAccount(String id, String password, JSONObject accountDetails);
	
	/**
	 * Remove  a service account from this user account
	 * @param id the user identifier
	 * @param password the user password
	 * @param accountDetails the accountDetial for removal as JSONObject
	 * @return true if the account is correctly removed
	 */
	public boolean removeAccount(String id, String password, JSONObject accountDetails);
	
	/**
	 * Get all account details
	 * @param id the end user identifier
	 * @return accounts details as a JSONArray
	 */
	public JSONArray getAccountsDetails(String id);
	
	/**
	 * Associate a device to the user
	 * @param id the user identifier
	 * @param password the user password
	 * @param deviceId the device identifier
	 * @return true if the device is correctly added
	 */
	public boolean addDevice(String id, String password, String deviceId);
	
	/**
	 * Dissociate a device from the user
	 * @param id the user identifier
	 * @param password the user password
	 * @param deviceId the device identifier
	 * @return true if the device is correctly removed
	 */
	public boolean removeDevice(String id, String password, String deviceId);
	
	/**
	 * Get all associated device id
	 * @param id the end user identifier
	 * @return the device id as a JSONArray
	 */
	public JSONArray getAssociatedDevices(String id);

        /**
         * 
         * the object is as this {'type':'role','value':'nameOfRole','children':[roleObjects]}
         * 
         * @return the hierarchy of the users as a tree in a JSONObject
         */
        public JSONObject getHierarchy();
        
        /**
         * Set the hierarchy
         * 
         * @param hierarchy the hierarchy entered as a tree
         */
        public void setHierarchy(JSONObject hierarchy);
}
