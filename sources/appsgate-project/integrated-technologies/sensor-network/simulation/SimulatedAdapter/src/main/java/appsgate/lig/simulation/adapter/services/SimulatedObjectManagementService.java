package appsgate.lig.simulation.adapter.services;

import java.util.HashMap;

import org.json.JSONArray;

/**
 * This interface describe service for simulated objects management.
 * 
 * @author Cédric Gérard
 * @since august 8, 2013
 * @version 1.0.0
 *
 */
public interface SimulatedObjectManagementService {
	
	public static final String SIMULATED_TEMPERATURE = "SimulatedTemperatureSensor";
	
	/**
	 * Add a new simulated object
	 * @param type the simulate object type
	 * @param properties the simulate object properties
	 * @return true if the simulate object has been created, false otherwise
	 */
	public boolean addSimulateObject(String type, HashMap<String,String> properties);
	
	/**
	 * Remove a simulated object
	 * @param objectId the identifier of the object to remove
	 * @return true if the object has been removed, false otherwise
	 */
	public boolean removeSimulatedObject(String objectId);
	
	/**
	 * Get all existing simulate object
	 * @return the simulate object list as a JSONArray
	 */
	public JSONArray getSimulateObjectlist();

}
