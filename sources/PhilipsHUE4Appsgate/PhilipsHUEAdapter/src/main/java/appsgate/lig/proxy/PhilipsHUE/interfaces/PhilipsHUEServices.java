package appsgate.lig.proxy.PhilipsHUE.interfaces;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This is the OSGi service interface for Philips HUE adapter.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since May 22, 2013
 * 
 */
public interface PhilipsHUEServices {
	
	/**
	 * Gets a list of all lights that have been discovered by the bridge.
	 * @return the list of light as a JSON array
	 */
	public JSONArray getLightList();
	
	/**
	 * Gets a list of lights that were discovered the last time a search for
	 * new lights was performed. The list of new lights is always deleted
	 * when a new search is started.
	 * @return the list of new lights as a JSON array
	 */
	public JSONArray getNewLights();
	
	
	/**
	 * Starts a search for new lights.
	 * The bridge will search for 1 minute and will add a maximum of 15 new lights.
	 * To add further lights, the command needs to be sent again after the search has completed.
	 * If a search is already active, it will be aborted and a new search will start.
	 * When the search has finished, new lights will be available using the get new lights command.
	 * In addition, the new lights will now be available by calling get all lights.
	 * @return true when the search begin.
	 */
	public boolean searchForNewLights();
	
	/**
	 * Get the current light state. That method call return all the attributes corresponding
	 * to the remote light.
	 * @param id the light bulb id on the bridge
	 * @return all the light bulb attributes as an JSON object
	 */
	public JSONObject getLightState(String id);
	
	/**
	 * Set the attribute value for the specified light bulb identifier
	 * @param id the light bulb id on the bridge
	 * @param attribute the attribute to set
	 * @param value the new value for the attribute
	 * @return true if the value is set correctly, false otherwise
	 */
	public boolean setAttribute(String id, String attribute, String value);
	
	/**
	 * Set a group of attribute (Batch mode) 
	 * @param attributes  all attributes to set, with name, valu and the light
	 * bulb identifier on the bridge 
	 * @return true if all the attribute are set and false otherwise
	 */
	public boolean setAttribute(JSONObject attributes);
	
	
}
