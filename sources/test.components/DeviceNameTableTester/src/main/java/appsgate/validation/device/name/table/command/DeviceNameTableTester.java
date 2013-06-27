package appsgate.validation.device.name.table.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.device.name.table.spec.DeviceNameTableSpec;
/**
 * This is class is use to validate the device name table
 * @author Cédric Gérard
 *
 */
public class DeviceNameTableTester {
	
	private static String testDeviceId = "194.199.23.136-1";
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(DeviceNameTableTester.class);
	
	private DeviceNameTableSpec deviceNameTable;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("DeviceNameTableTester has been initialized");
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to set the name of a device");
		deviceNameTable.addName(testDeviceId, null, "La lampe HUE 1");
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to get the name of a device");
		String name = deviceNameTable.getName(testDeviceId, null);
		logger.debug("       @@@@@@@@@@@@ Device name get: "+name);
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to delete the device name: ");
		deviceNameTable.deleteName(testDeviceId, null);
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to get the name of a device");
		name = deviceNameTable.getName(testDeviceId, null);
		logger.debug("       @@@@@@@@@@@@ Device name get: "+name);
		
		logger.debug("@@@@@@@@@@@@@@@@@@@ Try to get the name of a device for a non existing user");
		name = deviceNameTable.getName(testDeviceId, "plop");
		logger.debug("       @@@@@@@@@@@@ Device name get: "+name);
		
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("DeviceNameTableTester has been stopped");
	}

}
