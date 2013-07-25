package appsgate.lig.context.user.impl.account;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;

/**
 * This class is a representation of an service account
 * to be associated with a end user.
 * 
 * @author Cédric Gérard
 * @since July 23, 2013
 * @version 1.0.0
 */
public class ServiceAccount {
	
	/**
	 * The login for this account
	 */
	private String login;
	
	/**
	 * The hash of the corresponding password
	 */
	private String hashPswd;
	
	/**
	 * The selected implementation for this account
	 */
	private String accountImplementation;
	
	/**
	 * Account details for synchronization
	 */
	private JSONObject accountSynchDetails;
	

	public ServiceAccount(String login, String hashPswd,
			String accountImplementation, JSONObject accountSynchDetails) {
		super();
		this.login = login;
		this.hashPswd = hashPswd;
		this.accountImplementation = accountImplementation;
		this.accountSynchDetails = accountSynchDetails;
		
		try {
			Implementation impl = CST.apamResolver.findImplByName(null, accountImplementation);
			HashMap<String,String> properties = new HashMap<String, String>();
		
			properties.put("account", login);
			properties.put("pswd", hashPswd);
			properties.put("calendarName", accountSynchDetails.getString("calendarName"));
			impl.createInstance(null, properties);
			
		} catch (JSONException e) {e.printStackTrace();}
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getHashPswd() {
		return hashPswd;
	}

	public void setHashPswd(String hashPswd) {
		this.hashPswd = hashPswd;
	}

	public String getAccountImplementation() {
		return accountImplementation;
	}

	public void setAccountImplementation(String accountImplementation) {
		this.accountImplementation = accountImplementation;
	}

	public JSONObject getAccountSynchDetails() {
		return accountSynchDetails;
	}

	public void setAccountSynchDetails(JSONObject accountSynchDetails) {
		this.accountSynchDetails = accountSynchDetails;
	}
	
	public JSONObject getAccountJSONDescription() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("login", login);
			obj.put("hasPSWD", hashPswd);
			obj.put("implem", accountImplementation);
			obj.put("synchDetails", accountSynchDetails);
		} catch (JSONException e) {e.printStackTrace();}
		
		return obj;
	}


}
