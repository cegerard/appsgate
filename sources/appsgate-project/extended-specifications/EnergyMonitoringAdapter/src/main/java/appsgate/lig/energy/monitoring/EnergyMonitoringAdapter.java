package appsgate.lig.energy.monitoring;

import org.json.JSONArray;

/**
 * Adapter responsible for creation of EnergyMonitoring groups
 * @author thibaud
 */
public interface EnergyMonitoringAdapter {
	
	/**
	 * create a group along with its initial properties
	 * @param groupName user defined name for the group
	 * @param sensors initial array of objectID, each object ID is an EnergySensor (for instance a SmartPlug)
	 * @param budgetTotal the total budget, relative to the unit defined
	 * @param budgetUnit defined relative to the cost of one watt/sec
	 * @param periods the Monitoring Periods as a JSONArray of JSONObject, each one represents a Period
	 * @see  appsgate.lig.energy.monitoring.utils.Period
	 * @return the groupID if the service was successfully created
	 */
	public String createGroup(String groupName,
			JSONArray sensors,
			double budgetTotal, double budgetUnit,
			JSONArray periods);
	
	/**
	 * Create a Group with no sensors, no budget, no period, and all default values
	 * @param groupName user defined name for the group
	 * @return the groupID if the service was successfully created
	 */
	public String createEmptyGroup(String groupName);	
	
	public void removeGroup(String groupID);

}
