package appsgate.lig.proxy.services;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public interface EnOceanService {

	public JSONArray getAllItem();
	public JSONObject getItem(String id);
	public JSONArray getItemCapabilities(String id);
	public void validateItem(String sensorID, ArrayList<String> capList, boolean doesCapabilitiesHaveToBeSelected);
	
	
	/****************************/
	/*****  Actuator part  ******/
	/****************************/
	
	/**
	 * Send the turn on actuator event to ubikit
	 * @param targetID, the targeted device identifier
	 */
	public void turnOnActuator(String targetID);
	
	/**
	 * Send the turn off actuator event to ubikit
	 * @param targetID, the targeted device identifier
	 */
	public void turnOffActuator(String targetID);
}
