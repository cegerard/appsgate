package appsgate.lig.enocean.ubikit.adapter.listeners;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsgate.lig.enocean.ubikit.adapter.UbikitAdapter;

/**
 * Listener for all configuration events from clients.
 * 
 * @author Cédric Gérard
 * @since February 10, 2013
 * @version 1.0.0
 * 
 */
public class EnOceanCommandListener implements CommandListener {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(EnOceanCommandListener.class);

	/**
	 * The parent proxy of this listener
	 */
	private UbikitAdapter enoceanProxy;

	/**
	 * Constructor to initialize the listener with its parent
	 * 
	 * @param enoceanProxy the parent of this listener
	 */
	public EnOceanCommandListener(UbikitAdapter enoceanProxy) {
		this.enoceanProxy = enoceanProxy;
	}

	/**
	 * Call when new command or event is received
	 * 
	 * @param cmd the command or event
	 * @param obj the core of the cmd parameter
	 */
	public void onReceivedConfig(String cmd, JSONObject obj) {
		logger.debug("Config or event received: " + cmd);
		logger.debug("with core: " + obj.toString());

		try {

			if (cmd.equalsIgnoreCase("setPairingMode")) {
				String mode = obj.getString("pairingMode");
				enoceanProxy.setPairingMode(Boolean.valueOf(mode));

			} else if (cmd.equalsIgnoreCase("sensorValidation")) {
				String id = obj.getString("id");
				JSONArray selectedCapas = obj.getJSONArray("capabilities");
				ArrayList<String> arrayCapa = new ArrayList<String>();

				int l = selectedCapas.length();
				int i = 0;

				while (i < l) {
					arrayCapa.add(selectedCapas.getString(i));
					i++;
				}

				enoceanProxy.validateItem(id, arrayCapa, true);

			} else if (cmd.equalsIgnoreCase("getConfDevices")) {
				enoceanProxy.getConfDevices(obj.getInt("clientId"));

			} else if (cmd.equalsIgnoreCase("actuatorAction")) {
				String id = obj.getString("id");
				String action = obj.getString("action");

				if (action.equalsIgnoreCase("on")) {
					enoceanProxy.turnOnActuator(id);
				} else if (action.equalsIgnoreCase("off")) {
					enoceanProxy.turnOffActuator(id);
				}
			} else if (cmd.equalsIgnoreCase("unpairDevice")) {
				String id = obj.getString("uid");
				enoceanProxy.unpairDevice(id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Like onReceiveConfig but more generic
	 * 
	 * @param obj the core of the cmd parameter
	 */
	@Override
	public void onReceivedCommand(JSONObject obj) {
		try {
			String command = obj.getString("CONFIGURATION");
			JSONObject value = obj.getJSONObject(command);
			value.put("clientId", obj.getInt("clientId"));
			onReceivedConfig(command, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
