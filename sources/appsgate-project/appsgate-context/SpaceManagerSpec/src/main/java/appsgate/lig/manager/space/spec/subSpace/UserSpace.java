package appsgate.lig.manager.space.spec.subSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.manager.space.spec.Space;


/**
 * This class is AppsGate end user implementation.
 * It describe how a end user is represent in the AppsGate context.
 * 
 * @author Cédric Gérard
 * @since February 14, 2014
 * @version 1.0.0
 */
public class UserSpace extends Space {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(UserSpace.class);
	
	/**
	 * Executor use to manage user service account instantiation
	 */
	private ScheduledExecutorService instanciationService;


	/**
	 * The password finger print keep for authentication
	 */
	String hashPSWD;
	
	/**
	 * List of synchronized services
	 */
	ArrayList<ServiceAccount> serviceAccountList = new ArrayList<ServiceAccount>();
	
	/**
	 * User space constructor
	 * @param id space identifier
	 * @param tags the tags list
	 * @param properties the properties map
	 * @param parent the parent of this space
	 * @param children the children list of this space
	 * @param pswd the password of this user
	 */
	public UserSpace(String id, ArrayList<String> tags, HashMap<String, String> properties, Space parent, ArrayList<Space> children, String pswd) {
		super(id, TYPE.USER, tags, properties, parent, children);
		this.instanciationService = Executors.newScheduledThreadPool(1);
		this.hashPSWD = BCrypt.hashpw(pswd, BCrypt.gensalt(11));
	}

	/**
	 * The user space constructor
	 * @param id the space identifier
	 * @param properties the properties list
	 * @param parent the parent space
	 * @param pswd the user password
	 */
	public UserSpace(String id, HashMap<String, String> properties, Space parent, String pswd) {
		super(id, TYPE.USER, properties, parent);
		this.instanciationService = Executors.newScheduledThreadPool(1);
		this.hashPSWD = BCrypt.hashpw(pswd, BCrypt.gensalt(11));
	}

	/**
	 * The default constructor
	 * @param id the space identifier
	 * @param parent the parent space
	 * @param pswd the user password
	 */
	public UserSpace(String id, Space parent, String pswd) {
		super(id, TYPE.USER, parent);
		this.instanciationService = Executors.newScheduledThreadPool(1);
		this.hashPSWD = BCrypt.hashpw(pswd, BCrypt.gensalt(11));
	}
	
	/**
	 * Rebuild a User space instance from existing data.
	 * The password is the encrypted value.
	 * @param id the space identifier
	 * @param tags the tags list
	 * @param properties the properties list
	 * @param parent the parent space
	 * @param hashPwd the encrypted value of the user password
	 */
	public UserSpace(String id, ArrayList<String> tags, HashMap<String, String> properties, Space parent, String hashPwd) {
		super(id, TYPE.USER, tags, properties, parent);
		this.instanciationService = Executors.newScheduledThreadPool(1);
		this.hashPSWD = hashPwd;
		
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
	public JSONObject getDescription() {
		JSONObject description = super.getDescription();
		try {
			description.put("hashPSWD", hashPSWD);
			description.put("accounts", getAccountsDetailsJSONArray());
		}catch(JSONException jsonex) {
			jsonex.printStackTrace();
		}
		
		return description;
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
