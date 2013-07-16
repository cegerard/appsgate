package appsgate.validation.main.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.main.spec.AppsGateSpec;


/**
 * Class use to test Appgate command
 * @author Cédric Gérard
 *
 */
public class AppsGateMainCommandTester {
	
	private static String testDeviceId = "194.199.23.136-1";
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(AppsGateMainCommandTester.class);
	
	private AppsGateSpec appsgate;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("AppsGateMainCommandTester has been initialized");
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to set the name of a device");
		appsgate.setUserObjectName(testDeviceId, null, "La lampe HUE 1");
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to get the name of a device");
		String name = appsgate.getUserObjectName(testDeviceId, null);
		logger.debug("       @@@@@@@@@@@@ Device name get: "+name);
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to delete the device name: ");
		appsgate.deleteUserObjectName(testDeviceId, null);
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to get the name of a device");
		name = appsgate.getUserObjectName(testDeviceId, null);
		logger.debug("       @@@@@@@@@@@@ Device name get: "+name);
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to get the name of a device for a non existing user");
		name = appsgate.getUserObjectName(testDeviceId, "plop");
		logger.debug("       @@@@@@@@@@@@ Device name get: "+name);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("AppsGateMainCommandTester has been stopped");
	}

}
