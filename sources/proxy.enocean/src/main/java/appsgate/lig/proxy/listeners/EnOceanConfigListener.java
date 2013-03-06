package appsgate.lig.proxy.listeners;

import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
	private static Logger logger = LoggerFactory.getLogger(EnOceanConfigListener.class);
	
	/**
	 * The parent proxy of this listener
	 */
	private EnOceanProxy enoceanProxy;
	
	/**
	 * Constructor to initialize the listener with its parent
	 * 
	 * @param enoceanProxy, the parent of this listener
	 */
	public EnOceanConfigListener(EnOceanProxy enoceanProxy) {
		this.enoceanProxy = enoceanProxy;
	}

	/**
	 * Call when new command or event is received
	 * @param cmd, the command or event
	 * @param obj, the core of the cmd parameter
	 */
	@SuppressWarnings("unchecked")
	public void onReceivedConfig(String cmd, JSONObject obj) {
		logger.debug("Config or event received: "+cmd);
		logger.debug("with core: "+obj.toJSONString());
			
		if (cmd.equalsIgnoreCase("setPairingMode")) {
			String mode = (String)obj.get("pairingMode");
			enoceanProxy.setPairingMode(Boolean.valueOf(mode));
			
		} else if (cmd.equalsIgnoreCase("sensorValidation")) {
			String id = (String)obj.get("id");
			JSONArray SelectedCapas = (JSONArray)obj.get("capabilities");
			enoceanProxy.validateItem(id, SelectedCapas, true);
		}
	}

	/**
	 * Like onReceiveConfig but more generic
	 * 
	 * @param cmd, the command or event
	 * @param obj, the core of the cmd parameter
	 * 
	 * @see onReceivedConfig
	 */
	public void onReceivedCommand(JSONObject obj) {
		Set<?> keys = obj.keySet();
		String command = keys.iterator().next().toString();
		JSONObject value = (JSONObject)obj.get(command);
		onReceivedConfig(command, value);
	}

}
