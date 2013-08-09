package appsgate.validation.virtual.adapter;

import java.util.HashMap;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.virtual.adapter.services.VirtualObjectManagementService;

/**
 * This class is use to validate the virtual adapter return call
 * 
 * @author Cédric Gérard
 * 
 */
public class VirtualAdapterTester {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(VirtualAdapterTester.class);

	private VirtualObjectManagementService virtualAdapterService;

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("VirtualAdapterTester has been initialized");

		// Add a virtual temperature sensor
		HashMap<String, String> prop1 = new HashMap<String, String>();

		prop1.put("deviceName", "MonVirtualSensorAMoi_1");
		prop1.put("deviceId", "PLOPPLUPPLIP_1");
		prop1.put("currentTemperature", "42");
		prop1.put("notifRate", "30000");
		prop1.put("evolutionValue", "1");
		prop1.put("evolutionRate", "60000");

		virtualAdapterService.addVirtualObject(
				VirtualObjectManagementService.VIRTUAL_TEMPERATURE, prop1);

		// Add a virtual temperature sensor
		HashMap<String, String> prop2 = new HashMap<String, String>();

		prop2.put("deviceName", "MonVirtualSensorAMoi_2");
		prop2.put("deviceId", "PLOPPLUPPLIP_2");
		prop2.put("currentTemperature", "18");
		prop2.put("notifRate", "20000");
		prop2.put("evolutionValue", "-0.5");
		prop2.put("evolutionRate", "50000");

		
		virtualAdapterService.addVirtualObject(
				VirtualObjectManagementService.VIRTUAL_TEMPERATURE, prop2);
		
		//List virtual device
		JSONArray jsonList = virtualAdapterService.getVirtualObjectlist();
		logger.debug("Virtual device list "+jsonList.toString());
		
		//Remove virtual device
		virtualAdapterService.removeVirtualObject("PLOPPLUPPLIP_1");
		virtualAdapterService.removeVirtualObject("PLOPPLUPPLIP_2");
		
		//List virtual device
		jsonList = virtualAdapterService.getVirtualObjectlist();
		logger.debug("Virtual device list "+jsonList.toString());
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("RouterCallTester has been stopped");
	}
}
