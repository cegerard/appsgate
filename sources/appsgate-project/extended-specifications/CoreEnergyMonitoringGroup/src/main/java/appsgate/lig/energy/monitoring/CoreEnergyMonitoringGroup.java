package appsgate.lig.energy.monitoring;

import org.json.JSONArray;

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
	 * @return an array of objectID, each object ID is an EnergySensor (for instance a SmartPlug)
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
	 * Expressed in watt/seconds
	 * @return
	 */
	public double getTotalEnergy();
	
	/**
	 * Get the Energy used in this group since last reset, during the defined time period(s)
	 * Expressed in watt/seconds
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
	 * @param budgetTotal
	 * @param budgetUnit
	 */
	public void setBudget(double budgetTotal, double budgetUnit);
	
	
	/**
	 * Budget will only be measured between the start and the end of the period, the period can repeat during time
	 * at each interval.
	 * Note that changing the period reset the budget measure.
	 * @param startDate 
	 * @param endDate
	 * @param periodOffset by default 0 (used if relative to reals days, week, month, ...) 
	 * @param periodInterval in ms (for instance 24 x 60 x 60 x 1000 define an interval of a day),
	 * if 0, there will be no intervals
	 */
	public void setPeriod(long startDate, long endDate, long periodOffset, long periodInterval);
	
	
	/**
	 * Return the starting date of the Energy monitoring period for this group
	 * @return the starting date in millisecs from the epoch (01/01/1970) 
	 */
	public long getPeriodStart(); 
	
	/**
	 * Return the ending date of the Energy monitoring period for this group
	 * @return the ending date in millisecs from the epoch (01/01/1970) 
	 */
	public long getPeriodEnd();

	/**
	 * Return the interval offset since the epoch. Each interval will apply
	 * @return the interval in millisecs
	 */
	public long getPeriodOffset();		

	/**
	 * Return the interval defined
	 * @return the interval in millisecs
	 */
	public long getPeriodInterval();			

}
