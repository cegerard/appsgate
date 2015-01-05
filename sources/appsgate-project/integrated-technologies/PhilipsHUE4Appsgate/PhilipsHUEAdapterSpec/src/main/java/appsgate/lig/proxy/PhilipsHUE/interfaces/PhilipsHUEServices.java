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
	 * Starts a search for new lights.
	 * The bridge will search for 1 minute and will add a maximum of 15 new lights.
	 * To add further lights, the command needs to be sent again after the search has completed.
	 * If a search is already active, it will be aborted and a new search will start.
	 * When the search has finished, new lights will be available using the get new lights command.
	 * In addition, the new lights will now be available by calling get all lights.
	 * @param the bridge Ip from where get new lights
	 * @return true when the search begin.
	 */
	public boolean searchForNewLights(String bridgeIP);
	
	/**
	 * Get the current light state. That method call return all the attributes corresponding
	 * to the remote light.
	 * @param bridgeIP the Philips HUE IP address
	 * @param id the light bulb id on the bridge
	 * @return all the light bulb attributes as an JSON object
	 */
	public JSONObject getLightState(String bridgeIP, String id);
	
	/**
	 * Set the attribute value for the specified light bulb identifier
	 * @param bridgeIP the Philips HUE IP address
	 * @param id the light bulb id on the bridge
	 * @param attribute the attribute to set
	 * @param value the new value for the attribute
	 * @return true if the value is set correctly, false otherwise
	 */
	public boolean setAttribute(String bridgeIP, String id, String attribute, boolean value);
	/**
	 * Set the attribute value for the specified light bulb identifier
	 * @param bridgeIP the Philips HUE IP address
	 * @param id the light bulb id on the bridge
	 * @param attribute the attribute to set
	 * @param value the new value for the attribute
	 * @param transition time to change the state
	 * @return true if the value is set correctly, false otherwise
	 */
	public boolean setAttribute(String bridgeIP, String id, String attribute, boolean value,Integer transitionTime);
	/**
	 * Set the attribute value for the specified light bulb identifier
	 * @param bridgeIP the Philips HUE IP address
	 * @param id the light bulb id on the bridge
	 * @param attribute the attribute to set
	 * @param value the new value for the attribute
	 * @return true if the value is set correctly, false otherwise
	 */
	public boolean setAttribute(String bridgeIP, String id, String attribute, long value);
	
	/**
	 * Set the attribute value for the specified light bulb identifier
	 * @param bridgeIP the Philips HUE IP address
	 * @param id the light bulb id on the bridge
	 * @param attribute the attribute to set
	 * @param value the new value for the attribute
	 * @return true if the value is set correctly, false otherwise
	 */
	public boolean setAttribute(String bridgeIP, String id, String attribute, String value);
	
	/**
	 * Set a group of attribute (Batch mode) 
	 * @param bridgeIP the Philips HUE IP address
	 * @param id the light bulb id on the bridge
	 * @param attributes  all attributes to set, with name, value and the light
	 * bulb identifier on the bridge 
	 * @return true if all the attribute are set and false otherwise
	 */
	public boolean setAttribute(String bridgeIP, String id, JSONObject attributes);
	
	/**
	 * Get the list of discovered Philips HUE bridge
	 * @return the list of bridge as a JSONArray
	 */
	public JSONArray getBridgeList();
	
}
