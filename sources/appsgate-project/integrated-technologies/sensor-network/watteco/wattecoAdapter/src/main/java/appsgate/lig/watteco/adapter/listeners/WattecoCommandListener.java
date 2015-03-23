package appsgate.lig.watteco.adapter.listeners;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsgate.lig.watteco.adapter.WattecoAdapter;

/**
 * Listener for all configuration event from clients
 * @author Cédric Gérard
 * @since September 30, 2013
 * @version 1.0.0
 *
 */
public class WattecoCommandListener implements CommandListener {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(WattecoCommandListener.class);
	
	/**
	 * Adapter for Watteco sensor network technology
	 */
	private WattecoAdapter wattecoAdapter;
	
	/**
	 * Build an Watteco listener for client configuration messages
	 * @param wattecoAdapter the Watteco adapter bundle
	 */
	public WattecoCommandListener(WattecoAdapter  wattecoAdapter) {
		this.wattecoAdapter = wattecoAdapter;
	}

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

	public void onReceivedConfig(String cmd, JSONObject obj) {
		logger.debug("Config or event received: " + cmd);
		logger.debug("with core: " + obj.toString());
		
		if (cmd.equalsIgnoreCase("discover")) {
			try {
				String routerIP = obj.getString("borderRouterIP");
				wattecoAdapter.discover(routerIP);
			} catch (JSONException e) {
				wattecoAdapter.discover();
			}
		}
	}

}
