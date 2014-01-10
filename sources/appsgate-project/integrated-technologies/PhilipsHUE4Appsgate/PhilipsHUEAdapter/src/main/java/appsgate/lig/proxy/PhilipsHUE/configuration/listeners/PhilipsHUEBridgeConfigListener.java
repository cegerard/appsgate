package appsgate.lig.proxy.PhilipsHUE.configuration.listeners;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.subscribe.ConfigListener;
import appsgate.lig.proxy.PhilipsHUE.PhilipsHUEAdapter;

/**
* Listener for all configuration events from clients.
* 
* @author Cédric Gérard
* @since January 7, 2014
* @version 1.0.0
* 
*/
public class PhilipsHUEBridgeConfigListener implements ConfigListener {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(PhilipsHUEBridgeConfigListener.class);
	
	/**
	 * The PhilipsHUE bridge service reference
	 */
	private PhilipsHUEAdapter PhilipsAdapter;
	
	/**
	 * Build a Philips HUE bridge listener for client communication
	 * @param bridgeService
	 */
	public PhilipsHUEBridgeConfigListener(PhilipsHUEAdapter PhilipsAdapter) {
		super();
		this.PhilipsAdapter = PhilipsAdapter;
	}

	@Override
	public void onReceivedCommand(JSONObject obj) {
		logger.error("Command received in the Philisps HUE adapter configuration listener !");
	}

	@Override
	public void onReceivedConfig(String cmd, JSONObject obj) {
		logger.debug("Config or event received: " + cmd);
		logger.debug("with core: " + obj.toString());

		if (cmd.equalsIgnoreCase("getHUEConfDevices")) {
			JSONObject returnResp = new JSONObject();
			JSONArray bridgeList = PhilipsAdapter.getBridgeList();
			JSONArray lightList = PhilipsAdapter.getLightList();
			
			try {
				returnResp.put("bridges", bridgeList);
				returnResp.put("lights", lightList);
				JSONObject resp = new JSONObject();
				resp.put("TARGET", PhilipsHUEAdapter.CONFIG_TARGET);
				resp.put("hueConfDevices", returnResp);
				PhilipsAdapter.getCommunicationService().send(obj.getInt("clientId"), resp.toString());
				
			} catch (JSONException e) {e.printStackTrace();}
			
		}else { //Unknown command
			logger.warn("This command is unknown for the "+PhilipsHUEAdapter.CONFIG_TARGET+" target");
		}
		
	}
	

}
