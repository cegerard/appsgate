package appsgate.lig.proxy.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import appsGate.lig.manager.communication.service.subscribe.ConfigListener;
import appsgate.lig.proxy.enocean.EnOceanProxy;

/**
 * Listener for all configuration events from clients.
 * 
 * @author Cédric Gérard
 * @since February 10, 2013
 * @version 1.0.0
 * 
 */
public class EnOceanConfigListener implements ConfigListener {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(EnOceanConfigListener.class);

	/**
	 * The parent proxy of this listener
	 */
	private EnOceanProxy enoceanProxy;

	/**
	 * Constructor to initialize the listener with its parent
	 * 
	 * @param enoceanProxy
	 *            , the parent of this listener
	 */
	public EnOceanConfigListener(EnOceanProxy enoceanProxy) {
		this.enoceanProxy = enoceanProxy;
	}

	/**
	 * Call when new command or event is received
	 * 
	 * @param cmd
	 *            , the command or event
	 * @param obj
	 *            , the core of the cmd parameter
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
				enoceanProxy.getActuator();

			} else if (cmd.equalsIgnoreCase("createActuator")) {
				String profile = obj.getString("profile");
				String name = obj.getString("name");
				String place = obj.getString("place");
				enoceanProxy.createActuator(profile, name, place);

			} else if (cmd.equalsIgnoreCase("actuatorAction")) {
				String id = obj.getString("id");
				String action = obj.getString("action");

				if (action.equalsIgnoreCase("on")) {
					enoceanProxy.turnOnActuator(id);
				} else if (action.equalsIgnoreCase("off")) {
					enoceanProxy.turnOffActuator(id);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Like onReceiveConfig but more generic
	 * 
	 * @param obj
	 *            , the core of the cmd parameter
	 */
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

}
