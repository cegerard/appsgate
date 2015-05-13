package appsgate.lig.fairylights.adapter;

import org.json.JSONArray;

public interface FairyLightsAdapterSpec {
	
	/**
	 * Create a group of lights (create an instance of the CoreFairyLightsSpec service)
	 * with a set of selected lights using their absolute id (from '0' to '24').
	 * <br>The creation of the group will fail if:
	 * <li> The real fairy light device is not available at the moment</li> 
	 * <li> the selectedLights contains lights that already belongs to other groups (except the group "FairyLights-All")</li> 
	 * <li> the selectedLights contains lights that does not exists (or id unknown, out of bound, ...)</li>
	 * Note that a special group "FairyLights-All" is created when the Device is discovered,
	 * it contains all the lights of the real Device
	 * @param selectedLights is a JSONArray of absolute lights ids (from '0' to '24').
	 */
	public void createFreeformLightsGroup(JSONArray selectedLights);
	
	/**
	 * Create a contiguous group of lights containing all light between the light with id = startingIndex
	 * and the light with id = endingIndex
	 * @see FairyLightsAdapterSpec#createFreeformLightsGroup(JSONArray selectedLights)
	 * @param startingIndex
	 * @param endingIndex
	 */
	public void createContiguousLightsGroup(int startingIndex, int endingIndex);
	
	/**
	 * Update an existing group of lights
	 * replace the set of selected lights using their absolute id (from '0' to '24').
	 * <br>The update of the group will fail if:
	 * <li> The groupId does not exist or is not available at the moment</li> 
	 * <li> the selectedLights contains lights that already belongs to other groups (except the group "FairyLights-All")</li> 
	 * <li> the selectedLights contains lights that does not exists (or id unknown, out of bound, ...)</li>
	 * Note that the special group "FairyLights-All" cannot be updated
	 * @param selectedLights is a JSONArray of absolute lights ids (from '0' to '24').
	 */
	public void updateLightsGroup(String groupId, JSONArray selectedLights);
	
	/**
	 * Release one single light fom its affected group
	 * @param lightIndex is an absolute lights id (from '0' to '24').
	 */
	public void releaseLight(int lightIndex);	
	
	/**
	 * Remove an existing group of lights upon its objectId
	 * Note that the group "FairyLights-All" cannot be removed
	 * @param groupId
	 */
	public void removeLightsGroup(String groupId);
	
	/**
	 * Get an Array of all the lights that does not belong to a group (except "FairyLights-All")
	 * @return
	 */
	public JSONArray getAvailableLights();
	

}
