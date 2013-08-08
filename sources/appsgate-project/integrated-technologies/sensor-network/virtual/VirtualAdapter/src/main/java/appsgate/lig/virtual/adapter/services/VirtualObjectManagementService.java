package appsgate.lig.virtual.adapter.services;

import java.util.HashMap;

import org.json.JSONArray;

/**
 * This interface describe service for virtual objects management.
 * 
 * @author Cédric Gérard
 * @since august 8, 2013
 * @version 1.0.0
 *
 */
public interface VirtualObjectManagementService {
	
	public static final String VIRTUAL_TEMPERATURE = "VirtualTemperatureSensor";
	
	/**
	 * Add a new virtual object
	 * @param type the virtual object type
	 * @param properties the virtual object properties
	 * @return true if the virtual object has been created, false otherwise
	 */
	public boolean addVirtualObject(String type, HashMap<String,String> properties);
	
	/**
	 * Remove a virtual object
	 * @param objectId the identifier of the object to remove
	 * @return true if the object has been removed, false otherwise
	 */
	public boolean removeVirtualObject(String objectId);
	
	/**
	 * Get all existing virtual object
	 * @return the virtual object list as a JSONArray
	 */
	public JSONArray getVirtualObjectlist();

}
