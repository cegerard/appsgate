package appsgate.lig.energy.monitoring.group;

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
	
	
	public final static String NAME_KEY = "name";
	public final static String SENSORS_KEY = "sensors";
	public final static String ENERGY_KEY="energy";	
	public final static String BUDGETTOTAL_KEY="budgetTotal";
	public final static String BUDGETUNIT_KEY="budgetUnit";
	public final static String BUDGETREMAINING_KEY="budgetRemaining";
	public final static String BUDGETRESETED_KEY="budgetReset";
	public final static String PERIODS_KEY="periods";	
	public final static String ISMONITORING_KEY = "isMonitoring";
	public final static String LASTRESET_KEY = "lastReset";
	public final static String ANNOTATIONS_KEY = "annotations";
	public final static String HISTORY_KEY = "history";
	public final static String AUTOMATION_KEY = "automation";
	
	public final static String PERIOD_ID = "id";
	public final static String PERIOD_NAME = "name";
	public final static String PERIOD_START = "startDate";
	public final static String PERIOD_STOP = "stopDate";
	public final static String PERIOD_RECURRENCE = "recurrence";
	
	
	public final static String RAW_ENERGY_KEY="rawEnergy";	
	public final static String RAW_ENERGYDURINGPERIOD_KEY="rawEnergyDuringPeriod";		
	
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
	 * Allow the user to add a text to the group
	 * (explaining unusual energy consumption for instance)
	 * The annotation will be timestamped by the service ('when' this method is called)
	 * Warning: there could be only one single annotation for a particular timestamp 
	 * @param annotation
	 */
	public void addAnnotation(String annotation);
	
	/**
	 * get all the annotations attached to the group as a JSONArray of JSONObject
	 * (the key being the timestamp and the value being the text)
	 * [{"time1","annotation 1"},{"time2", "annotation2"},..., {{"timeN", "annotationN"}}]
	 * @param annotation
	 */
	public JSONArray getAnnotations();
	
	/**
	 * Allow the user to delete an annotation using its timestamp
	 * Warning: there could be only one single annotation for a particular timestamp 
	 * @param timestamp if the timestamp is not found, nothing will be done

	 */
	public void deleteAnnotation(String timestamp);	
	
	/**
	 * Allow the user to add/update an annotaiton upon its timestamp
	 * (explaining unusual energy consumption for instance)
	 * The annotation explicitly timestamped as parameter (if no annotation is found the annotation is added)
	 * Warning: there could be only one single annotation for a particular timestamp 
	 * @param annotation
	 */
	public void updateAnnotation(String timestamp, String annotation);
	
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
	 * Reset the current Energy used and remaining Budget 
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
	public double getEnergyDuringTimePeriod();
	
	
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
	public double getBudget();
	
	/**
	 * Return the budget unit defined relative to the cost of one watt/sec
	 * @return the budget unit (1 by default, corresponding to Watt/sec) 
	 */
	public double getBudgetUnit();
	
	/**
	 * The the budget the budget unit, general formula:
	 * Remaining = Budget - (BudgetUnit x EnergyDuringTimePeriod)
	 * @param budgetUnit 1 is a default value (Watt/sec)
	 */
	public void setBudgetUnit(double budgetUnit);	
	
	/**
	 * The the budget total and the budget unit, general formula:
	 * Remaining = Budget - (BudgetUnit x EnergyDuringTimePeriod)
	 * Note: Setting the budget restet the remaining budget
	 * @param budget if -1, no budget defined
	 */
	public void setBudget(double budget);

	 /**
	 * Configure the starting of the monitoring, might be with a fixed date and/or a recurrence pattern
	 * Note: that each start implies a reset
	 * @param startDate the starting date in millisecs from the epoch (01/01/1970), if 0 or -1 is provided, assume to use the recurrence from 01/01/1970 
	 * @param recurrence as a basic reccurence rule to the event (supported values : NONE, EACH_DAY, EACH_WEEK, EACH_MONDAY... EACH_MONTH, EACH_YEAR,
	 */
	public void configureStart(long startDate, String recurrence);
	
	 /**
	 * Configure the stopping of the monitoring, might be with a fixed date and/or a recurrence pattern
	 * @param startDate the starting date in millisecs from the epoch (01/01/1970), if 0 or -1 is provided, assume to use the recurrence from 01/01/1970 
	 * @param recurrence as a basic reccurence rule to the event (supported values : NONE, EACH_DAY, EACH_WEEK, EACH_MONDAY... EACH_MONTH, EACH_YEAR,
	 */
	public void configureStop(long stopDate, String recurrence);
	
	 /**
	 * Configure the reset of the monitoring, might be with a fixed date and/or a recurrence pattern
	 * @param startDate the starting date in millisecs from the epoch (01/01/1970), if 0 or -1 is provided, assume to use the recurrence from 01/01/1970 
	 * @param recurrence as a basic reccurence rule to the event (supported values : NONE, EACH_DAY, EACH_WEEK, EACH_MONDAY... EACH_MONTH, EACH_YEAR,
	 */
	public void configureReset(long resetDate, String recurrence);
	
	/**
	 * returns true if the group is currently monitoring Energy consumption (and decreasing budget)
	 * @return
	 */
	public boolean isMonitoring();
	
	/**
	 * Used to force the monitoring even if we are not on a monitoring period
	 * Won't stop until a call to stopMonitoring or if a monitoring period ends
	 */
	public void startMonitoring();
	
	/**
	 * Used to force the stop of monitoring even if we are inside a monitoring period
	 * Won't start again until a call to startMonitoring or if a monitoring period begin
	 */
	public void stopMonitoring();
	
	/**
	 * get the timestamp of the last reset, the budget remaining and total consumption
	 * are since this timestamp
	 * (the timestamp is build again the CoreClock, in may not be the real system time)
	 * @return a timestamp as a long value in millisecs from the epoch (01/01/1970)  
	 */
	public long getLastResetTimestamp();
	public long getLastStartTimestamp();
	public long getLastStopTimestamp();
	
	/**
	 * retrieve the history of measured budget and energy consumption at each reset
	 * @return an array of JSONObject, each one contains the starting and stop of measure, time of last reset (equal or after last stop)
	 * the remaining budget at the end and the total consumption at the end
	 */
	public JSONArray getEnergyHistory();
}
