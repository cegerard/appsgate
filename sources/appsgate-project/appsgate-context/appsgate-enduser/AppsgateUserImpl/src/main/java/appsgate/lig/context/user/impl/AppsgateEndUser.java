package appsgate.lig.context.user.impl;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.user.impl.account.ServiceAccount;

/**
 * This class is Appsgate end user implementation.
 * It describe how a end user is represent in the Appsgate context.
 * 
 * @author Cédric Gérard
 * @since July 19, 2013
 * @version 1.0.0
 */
public class AppsgateEndUser {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(AppsgateEndUser.class);

	/**
	 * The end user identifier use in in
	 * all other context components
	 */
	String id;
	
	/**
	 * The password finger print keep for authentication
	 */
	String hashPSWD;
	
	/**
	 * The user last name
	 */
	String lastName;
	
	/**
	 * The user first name
	 */
	String firstName;
	
	/**
	 * The user role in his family.
	 * (father, mother, child, ant, friend, etc.)
	 */
	String role;
	
	/**
	 * List of synchronized services
	 */
	ArrayList<ServiceAccount> serviceAccountList;
	
	/**
	 * List of owned devices
	 */
	ArrayList<String> deviceOwnedList;
	
	/**
	 * Build a new end user instance
	 * 
	 * @param id the end user unique identifier
	 * @param pswd the end user password 
	 * @param lastName the end user last name
	 * @param firstName the end user first name
	 * @param role the end user role in the smart home
	 */
	public AppsgateEndUser(String id, String pswd, String lastName, String firstName, String role) {
		super();
		
		this.id	   	   = id;
		this.hashPSWD  = BCrypt.hashpw(pswd, BCrypt.gensalt(11));
		this.lastName  = lastName;
		this.firstName = firstName;
		this.role 	   = role;
	}
	
	/**
	 * Build a new end user instance from a JSONObject description
	 * @param jsonObject the JSONObject description
	 */
	public AppsgateEndUser(JSONObject jsonObject) {
		try {
			this.id 	   = jsonObject.getString("id");
			this.hashPSWD  = jsonObject.getString("hashPSWD");
			this.lastName  = jsonObject.getString("lastName");
			this.firstName = jsonObject.getString("firstName");
			this.role 	   = jsonObject.getString("role");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New user instanciated");
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("A user instance desapeared");
	}

	/**
	 * Authenticate the end user password 
	 * @param candidatepswd the password to test
	 * @return true if the password correspond to this end user password, false otherwise
	 */
	public boolean authenticate(String candidatepswd) {
		return BCrypt.checkpw(candidatepswd, hashPSWD);
	}
	
	/**
	 * Get the JSON format of this End user object
	 * @return the end user object as a JSONObject
	 */
	public JSONObject JSONize() {
		
		JSONObject obj =  new JSONObject();
		try {
			obj.put("id", id);
			obj.put("hashPswd", hashPSWD);
			obj.put("lastName", lastName);
			obj.put("firstName", firstName);
			obj.put("role", role);
			obj.put("devices", getDeviceListJSONArray());
			obj.put("accounts", getAccountsDetailsJSONArray());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return obj;
	}
	
	/**
	 * Get the JSONArray of synchronized accounts
	 * @return synchronized accounts as a JSONArray
	 */
	private JSONArray getAccountsDetailsJSONArray() {
		JSONArray array = new JSONArray();
		
		for(ServiceAccount sa : serviceAccountList) {
			array.put(sa.getAccountSynchDetails());
		}
		
		return array;
	}

	/**
	 * Get the device list as a JSON array
	 * @return associated device list as a JSONArray
	 */
	private JSONArray getDeviceListJSONArray() {
		return new JSONArray(deviceOwnedList);
	}

	/**
	 * Get the JSON description of an end user
	 * @return User description as a JSONObject
	 */
	public JSONObject getDescription() {
		
		JSONObject obj =  new JSONObject();
		try {
			obj.put("id", id);
			obj.put("lastName", lastName);
			obj.put("firstName", firstName);
			obj.put("role", role);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return obj;
	}
	
	/**
	 * Add a device to the owned devices list
	 * @param deviceId the device to add
	 * @return true if the device has been added, false otherwise
	 */
	public boolean addDevice(String deviceId) {
		return deviceOwnedList.add(deviceId);
	}
	
	/**
	 * Remove a device from the owned devices list
	 * @param deviceId the device to remove
	 * @return true if the device has been removed, false otherwise.
	 */
	public boolean removeDevice(String deviceId) {
		return deviceOwnedList.remove(deviceId);
	}

	/**
	 * Add service account to this user
	 * @param accountDetails the account details as a JSONObject
	 * @return true if the account has been added, false otherwise
	 */
	public boolean addAccount(JSONObject accountDetails) {

		try {
			String saLogin = accountDetails.getString("login");
			String saHashPswd =  BCrypt.hashpw(accountDetails.getString("password"), BCrypt.gensalt(11));
			String saAccountImplementation = accountDetails.getString("implem");
			JSONObject saAccountSynchDetails = accountDetails.getJSONObject("details");
		
			ServiceAccount sa = new ServiceAccount(saLogin, saHashPswd, saAccountImplementation, saAccountSynchDetails); 
	
			return serviceAccountList.add(sa);
			
		} catch (JSONException e) {e.printStackTrace();}
		
		return false;
	}

	/**
	 * Remove service account from this user
	 * @param accountDetails the account details as a JSONObject
	 * @return true if the account has been deleted, false otherwise
	 */
	public boolean removeAccount(JSONObject accountDetails) {
		
		try {
			String saLogin = accountDetails.getString("login");
			JSONObject saAccountSynchDetails = accountDetails.getJSONObject("details");
	
			for(ServiceAccount saTemp : serviceAccountList) {
				if(saTemp.getLogin().contentEquals(saLogin) &&
				saTemp.getAccountSynchDetails().getString("id").contentEquals(saAccountSynchDetails.getString("id"))) {
					return serviceAccountList.remove(saTemp);
				}
			}
			
		} catch (JSONException e) {e.printStackTrace();}
		
		return false;
	}

	public String getId() {
		return id;
	}

	public String getLastName() {
		return lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getRole() {
		return role;
	}
	
}
