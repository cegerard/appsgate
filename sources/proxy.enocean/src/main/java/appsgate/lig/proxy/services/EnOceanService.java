package appsgate.lig.proxy.services;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public interface EnOceanService {

	public JSONArray getAllItem();
	public JSONObject getItem(String id);
	public JSONArray getItemCapabilities(String id);
	public void validateItem(String sensorID, ArrayList<String> capList, boolean doesCapabilitiesHaveToBeSelected);
}
