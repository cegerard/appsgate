package appsgate.validation.simulation.adapter;

import java.util.HashMap;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.simulation.adapter.services.SimulatedObjectManagementService;

/**
 * This class is use to validate the simulated adapter return call
 * 
 * @author Cédric Gérard
 * 
 */
public class SimulatedAdapterTester {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(SimulatedAdapterTester.class);

	private SimulatedObjectManagementService simulatedAdapterService;

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("SimulatedAdapterTester has been initialized");

		// Add a simulate temperature sensor
		HashMap<String, String> prop1 = new HashMap<String, String>();

		prop1.put("deviceName", "MonSimuleSensorAMoi_1");
		prop1.put("deviceId", "PLOPPLUPPLIP_1");
		prop1.put("currentTemperature", "42");
		prop1.put("notifRate", "30000");
		prop1.put("evolutionValue", "1");
		prop1.put("evolutionRate", "60000");

		simulatedAdapterService.addSimulateObject(
				SimulatedObjectManagementService.SIMULATED_TEMPERATURE, prop1);

		// Add a simulate temperature sensor
		HashMap<String, String> prop2 = new HashMap<String, String>();

		prop2.put("deviceName", "MonSimuleSensorAMoi_2");
		prop2.put("deviceId", "PLOPPLUPPLIP_2");
		prop2.put("currentTemperature", "18");
		prop2.put("notifRate", "20000");
		prop2.put("evolutionValue", "-0.5");
		prop2.put("evolutionRate", "50000");

		
		simulatedAdapterService.addSimulateObject(
				SimulatedObjectManagementService.SIMULATED_TEMPERATURE, prop2);
		
		//List simulate device
		JSONArray jsonList = simulatedAdapterService.getSimulateObjectlist();
		logger.debug("Simulated device list "+jsonList.toString());
		
		//Remove simulated device
		simulatedAdapterService.removeSimulatedObject("PLOPPLUPPLIP_1");
		simulatedAdapterService.removeSimulatedObject("PLOPPLUPPLIP_2");
		
		//List simulated device
		jsonList = simulatedAdapterService.getSimulateObjectlist();
		logger.debug("Simulated device list "+jsonList.toString());
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("RouterCallTester has been stopped");
	}
}
