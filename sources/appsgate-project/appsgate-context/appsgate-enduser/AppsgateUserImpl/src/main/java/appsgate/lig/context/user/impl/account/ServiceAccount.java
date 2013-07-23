package appsgate.lig.context.user.impl.account;

import org.json.JSONObject;

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
			String accountImplementation) {
		super();
		this.login = login;
		this.hashPswd = hashPswd;
		this.accountImplementation = accountImplementation;
	}

	public ServiceAccount(String login, String hashPswd,
			String accountImplementation, JSONObject accountSynchDetails) {
		super();
		this.login = login;
		this.hashPswd = hashPswd;
		this.accountImplementation = accountImplementation;
		this.accountSynchDetails = accountSynchDetails;
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


}
