package appsgate.validation.router.call;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.router.spec.RouterApAMSpec;

/**
 * This class is use to validate the router return call
 * @author Cédric Gérard
 *
 */
public class RouterCallTester {
	
	private static String testDeviceId = "194.199.23.136-1";
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(RouterCallTester.class);
	
	private RouterApAMSpec router;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("RouterCallTester has been initialized");
		
		JSONArray args  = new JSONArray();
		JSONObject id   = new JSONObject();
		JSONObject usr  = new JSONObject();
		JSONObject name = new JSONObject();
		
		try {
			id.put("value", testDeviceId);
			id.put("type", "String");
			args.put(id);
			
			usr.put("value", "");
			usr.put("type", "String");
			args.put(usr);
			
			name.put("value", "Ma lampe de test");
			name.put("type", "String");
			args.put(name);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to set the name of a device");
		router.executeCommand("main", "setUserObjectName", args).run();
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to get the name of a device");
		//router.executeCommand("main", "getUserObjectName", args).run();
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to delete the device name");
		//router.executeCommand("main", "deleteUserObjectName", args).run();
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to get the name of a device for a non existing user");
		
		JSONArray args2  = new JSONArray();
		try {
			args2.put(id);
			
			usr.put("value", "plop");
			args2.put(usr);
			
			args2.put(name);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		router.executeCommand("main", "setUserObjectName", args2).run();
		
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("RouterCallTester has been stopped");
	}
}
