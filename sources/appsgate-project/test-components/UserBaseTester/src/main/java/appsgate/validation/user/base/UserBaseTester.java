package appsgate.validation.user.base;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.userbase.spec.UserBaseSpec;

public class UserBaseTester {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(UserBaseTester.class);

	private UserBaseSpec UserBaseSpec;

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("UserBaseTester has been initialized");
		try {
			logger.info("########## Try to add user");
			if (UserBaseSpec.adduser("42", "appsgate", "Gerard", "Cedric",
					"papa")) {
				logger.info("########## user added");
			}

			logger.info("########## Try to get user list");
			JSONArray array = UserBaseSpec.getUsers();
			logger.info("########## list: " + array.toString());

			logger.info("########## Try to get user detail");
			JSONObject obj = UserBaseSpec.getUserDetails("42");
			logger.info("########## details: " + obj.toString());

			logger.info("########## Try to remove user");
			if (UserBaseSpec.removeUser("42", "appsgate")) {
				logger.info("########## user removed");
			}

			logger.info("########## Try to get user list");
			array = UserBaseSpec.getUsers();
			logger.info("########## list: " + array.toString());

			logger.info("########## Try to add user");
			if (UserBaseSpec
					.adduser("12", "appsgate", "Dupont", "Rene", "fils")) {
				logger.info("########## user added");
			}

			logger.info("########## Try to get user detail");
			obj = UserBaseSpec.getUserDetails("12");
			logger.info("########## details: " + obj.toString());

			logger.info("########## Try to add an account");
			JSONObject account = new JSONObject();
			try {
				account.put("login", "smarthome.inria@gmail.com");
				account.put("password", "smarthome2012");
				account.put("implem", "GoogleCalendarImpl");
				account.put("details", new JSONObject().put("calendarName", "Agenda boulot"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			logger.info("########## add the account");
			if (UserBaseSpec.addAccount("12", "appsgate", account)) {
				logger.info("########## account added");
			}
			
			logger.info("########## Try to add an account");
			account = new JSONObject();
			try {
				account.put("login", "smarthome.inria@gmail.com");
				account.put("password", "smarthome2012");
				account.put("implem", "GoogleCalendarImpl");
				account.put("details", new JSONObject().put("calendarName", "Smart home"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			logger.info("########## add the account");
			if (UserBaseSpec.addAccount("12", "appsgate", account)) {
				logger.info("########## account added");
			}

			logger.info("########## Try to get accounts");
			array = UserBaseSpec.getAccountsDetails("12");
			logger.info("########## account list: " + array.toString());

			logger.info("########## Try to add a device");
			if (UserBaseSpec.addDevice("12", "appsgate", "ENO5684302")) {
				logger.info("########## device added");
			}

			logger.info("########## Try to get devices");
			array = UserBaseSpec.getAssociatedDevices("12");
			logger.info("########## devices list: " + array.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("UserBaseTester has been stopped");
	}

}
