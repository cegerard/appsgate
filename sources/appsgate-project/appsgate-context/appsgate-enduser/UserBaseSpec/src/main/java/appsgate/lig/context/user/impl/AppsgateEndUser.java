package appsgate.lig.context.user.impl;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
	 * Executor use to manage user service account instanciation
	 */
	private ScheduledExecutorService instanciationService;

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
	ArrayList<ServiceAccount> serviceAccountList = new ArrayList<ServiceAccount>();
	
	/**
	 * List of owned devices
	 */
	ArrayList<String> deviceOwnedList = new ArrayList<String>();
	
	/**
	 * default constructor it
	 * just initialize the password hash with empty string hash
	 */
	public AppsgateEndUser() {
		this.hashPSWD = BCrypt.hashpw("", BCrypt.gensalt(11));
		this.instanciationService = Executors.newScheduledThreadPool(1);
	}
	
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
		
		this.instanciationService = Executors.newScheduledThreadPool(1);
		
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
		
		this.instanciationService = Executors.newScheduledThreadPool(1);
		
		try {
			this.id 	   = jsonObject.getString("id");
			this.hashPSWD  = jsonObject.getString("hashPSWD");
			this.lastName  = jsonObject.getString("lastName");
			this.firstName = jsonObject.getString("firstName");
			this.role 	   = jsonObject.getString("role");
			
			JSONArray devices = jsonObject.getJSONArray("devices");
			int size = devices.length();
			int i = 0;
			while (i<size){
				deviceOwnedList.add(devices.getString(i));
				i++;
			}
			
			//Create thread that take the account list in parameter and instanciate all account
			//TODO Schedule instanciation to avoid dependency collision
			instanciationService.schedule(new accountInstanciation(jsonObject.getJSONArray("accounts")), 15, TimeUnit.SECONDS);

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
		
		instanciationService.shutdown();
		try {
			instanciationService.awaitTermination(5, TimeUnit.SECONDS);
			logger.debug("instanciation acocunt service terminate.");
		} catch (InterruptedException e) {
			// instanciationService has probably terminated, but some problem
			// happened.
			logger.debug("Account instanciation service thread crash at termination");
		}
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
			obj.put("hashPSWD", hashPSWD);
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
	public JSONArray getAccountsDetailsJSONArray() {
		JSONArray array = new JSONArray();
		try{
			for(ServiceAccount sa : serviceAccountList) {
				array.put(sa.getAccountJSONDescription());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return array;
	}

	/**
	 * Get the device list as a JSON array
	 * @return associated device list as a JSONArray
	 */
	public JSONArray getDeviceListJSONArray() {
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
			String saPswd =  accountDetails.getString("password");
			String saService = accountDetails.getString("service");
			JSONObject saAccountSynchDetails = accountDetails.getJSONObject("details");
		
			ServiceAccount sa = new ServiceAccount(saLogin, saPswd, saService, saAccountSynchDetails); 
	
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

	/**
	 * Get the user identifier 
	 * @return the user identifier as a string
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the user last name
	 * @return the user last name as a string
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Get the user first name
	 * @return the user first name as a string
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Get the user role
	 * @return the user role as a string
	 */
	public String getRole() {
		return role;
	}
	
	/**
	 * Change the current password method
	 * @param oldPass the former user password
	 * @param newPass the new user password
	 * @return true if the password has change, false otherwise
	 */
	public boolean setPassword(String oldPass, String newPass) {
		if(authenticate(oldPass)) {
			this.hashPSWD = BCrypt.hashpw(newPass, BCrypt.gensalt(11));
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * Inner class for user account instanciation thread
	 * @author Cédric Gérard
	 * @since Septembre 10, 2013
	 * @version 1.0.0
	 */
	private class accountInstanciation implements Runnable {

		JSONArray accounts;
		
		public accountInstanciation(JSONArray accounts) {
			super();
			this.accounts = accounts;
			logger.info("user account instanciation service ready.");
		}

		public void run() {
			JSONObject acc;
			ServiceAccount sa;
			int size = accounts.length();
			int i=0;
			while (i<size){
				try {
					acc = accounts.getJSONObject(i);

					String login = acc.getString("login");
					String hashPswd = acc.getString("hasPSWD");
					String service = acc.getString("service");
					JSONObject accountSynchDetails = acc.getJSONObject("synchDetails");
				
					sa = new ServiceAccount(login, hashPswd, service, accountSynchDetails);
					serviceAccountList.add(sa);
					i++;
					
				} catch (JSONException e) {logger.error(e.getMessage());}
			}
		}
		
		
	}
}
