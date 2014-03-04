package appsgate.lig.manager.propertyhistory.services;

import java.util.Set;

import org.json.JSONArray;

/**
 * Interface for PropertyHistory Persistence Manager.
 * Mostly a getter for property changes (designed to reflect state change of ApAM components).
 * DB results are JSON Arrays serialized as Strings
 * @author Thibaud
 *
 */
public interface PropertyHistoryManager {
	
	/**
	 * Simple getter for property changes on the Database
	 * @param devicesID is an Array of devicesID (String) on which we want to evaluate the property name,
	 * 	if null, we inspect all devices ID
	 * @param propertyName The property name to evaluate
	 * @param time_start is the difference, measured in milliseconds, between the desired STARTING period and midnight, January 1, 1970 UTC,
	 * 	(not that simulated values, as future values can lead to weird results, loss of causality)
	 * @param time_end is the difference, measured in milliseconds, between the desired ENDING period and midnight, January 1, 1970 UTC,
	 * 	maximum value 'should' be the current Time (except for simulation -> which can lead to weird results, loss of causality),
	 * time_end value MUST be greater than time_start 
	 * @return a String that represent DB results as a JSON Array (itself containing Arrays), example :
	 * [
	 * deviceID1 : [{time: time_start, state: v0},{time:t1,state:v1},...,{time:tf,state:vf} ],
	 * deviceID2 : [...],
	 * ...
	 * ]
	 */
	String getDevicesStatesHistoryAsString(Set<String> devicesID, String propertyName, long time_start, long time_end);
	
	/**
	 * @see PropertyHistoryManager#getDevicesStatesHistoryAsString , but ...  
	 * @return Directly the JSON Array (itself containing Arrays), example :
	 * [
	 * deviceID1 : [{time: time_start, state: v0},{time:t1,state:v1},...,{time:tf,state:vf} ],
	 * deviceID2 : [...],
	 * ...
	 * ]
	 */
	JSONArray getDevicesStatesHistoryAsJSON(Set<String> devicesID, String propertyName, long time_start, long time_end);

}
