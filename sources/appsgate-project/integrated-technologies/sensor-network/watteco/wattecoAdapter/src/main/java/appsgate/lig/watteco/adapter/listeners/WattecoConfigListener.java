package appsgate.lig.watteco.adapter.listeners;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.subscribe.ConfigListener;
import appsgate.lig.watteco.adapter.WattecoAdapter;

/**
 * Listener for all configuration event from clients
 * @author Cédric Gérard
 * @since September 30, 2013
 * @version 1.0.0
 *
 */
public class WattecoConfigListener implements ConfigListener {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(WattecoConfigListener.class);
	
	/**
	 * Adapter for Watteco sensor network technology
	 */
	private WattecoAdapter wattecoAdapter;
	
	/**
	 * Build an Watteco listener for client configuration messages
	 * @param wattecoAdapter the Watteco adapter bundle
	 */
	public WattecoConfigListener(WattecoAdapter  wattecoAdapter) {
		this.wattecoAdapter = wattecoAdapter;
	}

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
