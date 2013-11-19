package appsgate.lig.context.user.impl.account;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private String service;
	
	/**
	 * Account details for synchronization
	 */
	private JSONObject accountSynchDetails;
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(ServiceAccount.class);

	/**
	 * Default constructor for user synchronized service account
	 * @param login the user login for the service
	 * @param hashPswd the corresponding password
	 * @param service the Appsgate web service
	 * @param accountSynchDetails all requires details for connection as a JSONObject
	 */
	public ServiceAccount(String login, String hashPswd,
			String service, JSONObject accountSynchDetails) {
		super();
		this.login = login;
		this.hashPswd = hashPswd;
		this.service = service;
		this.accountSynchDetails = accountSynchDetails;
		Implementation impl = null;
		try {
			if(service.contentEquals("GoogleAgenda")) {
				impl = CST.apamResolver.findImplByName(null, "GoogleCalendarImpl");
				if(impl != null) {
					HashMap<String,String> properties = new HashMap<String, String>();
					properties.put("account", login);
					properties.put("pswd", hashPswd);	
					properties.put("calendarName", accountSynchDetails.getString("calendarName"));
					impl.createInstance(null, properties);
				} 
			}else if(service.contentEquals("Mail")) {
				impl = CST.apamResolver.findImplByName(null, "GmailImpl");
				if(impl != null) {
					HashMap<String,String> properties = new HashMap<String, String>();
					properties.put("user", login);
					properties.put("password", hashPswd);	
					properties.put("auto-refresh", accountSynchDetails.getString("refreshRate"));
					impl.createInstance(null, properties);
				}
			}
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

	public String getServiceType() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public JSONObject getAccountSynchDetails() {
		return accountSynchDetails;
	}

	public void setAccountSynchDetails(JSONObject accountSynchDetails) {
		this.accountSynchDetails = accountSynchDetails;
		logger.debug("Account detials changed: "+getServiceType()+" / "+accountSynchDetails.toString());
	}
	
	public JSONObject getAccountJSONDescription() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("login", login);
			obj.put("hasPSWD", hashPswd);
			obj.put("service", service);
			obj.put("synchDetails", accountSynchDetails);
		} catch (JSONException e) {e.printStackTrace();}
		
		return obj;
	}


}
