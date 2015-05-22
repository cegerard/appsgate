package appsgate.lig.fairylights.adapter;

import org.json.JSONArray;

/**
 * This adapter create/modify/remove fairylights groups
 * It is responsible to manage the reservation policy of each light (if they can be shared by multiple groups or not) 
 * @author thibaud
 *
 */
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
	public void createFreeformLightsGroup(String name, JSONArray selectedLights);
	
	/**
	 * Create a contiguous group of lights containing all light between the light with id = startingIndex (included)
	 * and the light with id = endingIndex (included)
	 * @see FairyLightsAdapterSpec#createFreeformLightsGroup(JSONArray selectedLights)
	 * @param startingIndex
	 * @param endingIndex
	 */
	public void createContiguousLightsGroup(String name, int startingIndex, int endingIndex);
	
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
	
	/**
	 * The light reservation policy should be unique for one instance of Adapter, should not be changed at runtime
	 * @author thibaud
	 *
	 */
	public enum LightReservationPolicy {
		SHARED, // One light can be shared by multiple groups  (the group: "FairyLights-All" will allways contains all the fairy lights)
		ASSIGNED, // If a light is assigned to one group, canot be reused for another group (except the group: "FairyLights-All" that will allways contains all the fairy lights)
		EXCLUSIVE; // If a light is assigned to one group, canot be reused for another group (even the group: "FairyLights-All" that will contains the remaining availables fairy lights)
	}
}
