package appsgate.lig.virtual.simulation.listener;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.subscribe.ConfigListener;
import appsgate.lig.simulation.adapter.SimulationAdapter;

public class SimulationConfigListener implements ConfigListener {
	
	private SimulationAdapter adapter;
	
	
	public SimulationConfigListener(SimulationAdapter adapter) {
		super();
		this.adapter = adapter;
	}

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(SimulationConfigListener.class);

	@Override
	public void onReceivedCommand(JSONObject obj) {
		@SuppressWarnings("rawtypes")
		Iterator keys = obj.keys();
		String command = keys.next().toString();
		JSONObject value;
		try {
			value = obj.getJSONObject(command);
			onReceivedConfig(command, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onReceivedConfig(String cmd, JSONObject obj) {
		logger.debug("Config or event received: " + cmd);
		logger.debug("with core: " + obj.toString());

		try {

			if (cmd.equalsIgnoreCase("addVirtualObject")) {
				HashMap<String,String> properties =  new HashMap<String, String>();
				
				properties.put("deviceName", obj.getString("deviceName"));
				properties.put("deviceId", obj.getString("deviceId"));
				properties.put("currentTemperature", obj.getString("currentTemperature"));
				properties.put("notifRate", obj.getString("notifRate"));
				properties.put("evolutionValue", obj.getString("evolutionValue"));
				properties.put("evolutionRate", obj.getString("evolutionRate"));
				
				adapter.addSimulateObject(obj.getString("type"), properties);

			} else if (cmd.equalsIgnoreCase("removeSimulatedObject")) {
				adapter.removeSimulatedObject(obj.getString("objectId"));

			} else if (cmd.equalsIgnoreCase("getSimulateObjectList")) {
				JSONArray list = adapter.getSimulateObjectlist();
				adapter.sendResponse(obj.getInt("clientId"), "simulateObjectList", list);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
