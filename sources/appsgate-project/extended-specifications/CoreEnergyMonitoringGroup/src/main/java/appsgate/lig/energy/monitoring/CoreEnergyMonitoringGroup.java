package appsgate.lig.energy.monitoring;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This Extended Service monitor energy relative to:
 * <li>A group of sensor (might be all sensors) that measure energy consumption</li>
 * <li>A budget that will be decreased as energy is consumed</li>
 * <li>A time period for the budget, at each Time period the budget will be reseted</li>
 * The service should also trigger events whenever:
 * <li>the group of sensor changes</li>
 * <li>the total energy consumed changes</li>
 * <li>the budget is changed or reseted</li>
 * <li>the period is changed</li>
 * @author thibaud
 *
 */
public interface CoreEnergyMonitoringGroup {
	
	/**
	 * Get the user defined name for the group
	 * @return the user defined name for the group
	 */
	public String getName();
	
	/**
	 * Set the user defined name for this group 
	 */
	public void setName(String name);
	
	
	/**
	 * Get the current energy sensors in this group
	 * @return an array of objectID, each object ID (String) is an EnergySensor (for instance a SmartPlug)
	 */
	public JSONArray getEnergySensorsGroup();
	
	/**
	 * Single method to change partially or completly, the sensors in the group.
	 * This does NOT reset the current energy measure
	 * @param sensors an array of ObjectID
	 */
	public void setEnergySensorsGroup(JSONArray sensors);
	
	/**
	 * Add a sinle sensor to the group
	 * This does NOT reset the current energy measure
	 * @param sensorID the objectID of the sensor to add
	 */
	public void addEnergySensor(String sensorID);
	
	/**
	 * Remove a sinle sensor from the group
	 * This does NOT reset the current energy measure
	 * @param sensorID the objectID of the sensor to remove
	 */
	public void removeEnergySensor(String sensorID);
	
	/**
	 * Reset the current Energy used and Budget
	 * (the Total Energy and the Energy during Time periods)
	 * @return
	 */
	public void resetEnergy();
	
	/**
	 * Get the total Energy used in this group since last reset
	 * Expressed according to the BudgetUnit (if budget unit = 1 this is watt/sec)
	 * @return
	 */
	public double getTotalEnergy();
	
	/**
	 * Get the Energy used in this group since last reset, during the defined time period(s)
	 * Expressed according to the BudgetUnit (if budget unit = 1 this is watt/sec)
	 * @return
	 */
	public double geEnergyDuringTimePeriod();
	
	
	/**
	 * Return the remaining budget (budget defined minus energy consumed during Time periods)
	 * Remaining = BudgetTotal - (BudgetUnit x EnergyDuringTimePeriod)
	 * @return the remaining budget or -1 if no budget or time periods defined 
	 */
	public double getRemainingBudget();
	
	/**
	 * Return the budget defined for the Time period
	 * @return the remaining budget or -1 if no budget defined 
	 */
	public double getBudgetTotal();
	
	/**
	 * Return the budget unit defined relative to the cost of one watt/sec
	 * @return the budget unit (1 by default, corresponding to Watt/sec) 
	 */
	public double getBudgetUnit();
	
	/**
	 * The the budget total and the budget unit, general formula:
	 * Remaining = BudgetTotal - (BudgetUnit x EnergyDuringTimePeriod)
	 * Note: Setting the budget restet the remaining budget
	 * @param budgetTotal if -1, no budget defined
	 * @param budgetUnit 1 is a default value (Watt/sec)
	 */
	public void setBudget(double budgetTotal, double budgetUnit);
	
	/**
	 * Get the Monitoring Periods as a JSONArray of eventIDs (String),
	 * each one represents an Event in the scheduler
	 * @return
	 */
	public JSONArray getPeriods();
	
	/**
	 * Adds a basic single period with fixed starting and ending dates
	 * No periodicity by default (it has to be managed using the scheduler or google agenda)
	 * @param startDate the starting date in millisecs from the epoch (01/01/1970) 
	 * @param endDate the ending date in millisecs from the epoch (01/01/1970)
	 * @param resetOnStart if true, the budget remaining will be reseted each Time the period starts
	 * @param resetOnEnd if true, the budget remaining will be reseted each Time the period ends
	 * @return	the Event ID (String) as used in the Scheduler
	 */
	public String addPeriod(long startDate, long endDate, boolean resetOnStart, boolean resetOnEnd);
	
	/**
	 * Remove the specified periodID
	 * @param periodID as referenced in the scheduler
	 */
	public void removePeriodById(String eventID);
	
	/**
	 * Gets informations about the specified periodfriom the scheduler
	 * all these are summed in a JSONObject
	 * @param eventID as referenced in the scheduler
	 */
	public JSONObject getPeriodInfo(String eventID);		
}
