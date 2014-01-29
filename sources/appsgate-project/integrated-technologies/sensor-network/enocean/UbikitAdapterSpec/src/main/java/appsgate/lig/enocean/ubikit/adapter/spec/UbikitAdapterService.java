package appsgate.lig.enocean.ubikit.adapter.spec;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * This class is a service use to manipulate items from Ubikit EnOcean
 * PEM software layer. 
 * 
 * @author Cédric Gérard
 * @since February 7, 2013
 * @version 1.0.0
 *
 */
public interface UbikitAdapterService {

	/**
	 * Get all items ass a JSONArray
	 * @return JSONArray that contain all item
	 */
	public JSONArray getAllItem();
	
	/**
	 * Get the item by its identifier
	 * @param id the item identifier
	 * @return the item as a JSONObject
	 */
	public JSONObject getItem(String id);
	
	/**
	 * Get item capabilities from item identifier
	 * @param id the item identifier
	 * @return capabilities of the item as a JSONArray
	 */
	public JSONArray getItemCapabilities(String id);
	
	/**
	 * Validate an item profile for Ubikit EnOcean PEM
	 * @param sensorID the item identifier
	 * @param capList capabilities for this sensor as an array list
	 * @param doesCapabilitiesHaveToBeSelected boolean that is use to know if you need to select one of those capabilities for this sensor
	 */
	public void validateItem(String sensorID, ArrayList<String> capList, boolean doesCapabilitiesHaveToBeSelected);
	
	
	/****************************/
	/*****  Actuator part  ******/
	/****************************/
	
	/**
	 * Send the turn on actuator event to ubikit
	 * @param targetID the targeted device identifier
	 */
	public void turnOnActuator(String targetID);
	
	/**
	 * Send the turn off actuator event to ubikit
	 * @param targetID the targeted device identifier
	 */
	public void turnOffActuator(String targetID);
	
	/**
	 * Send the actuator udpate event through Ubikit.
	 * @param targetID the targeted device identifier
	 */
	public void sendActuatorUpdateEvent(String targetID);
	
}
