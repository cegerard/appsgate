package appsgate.lig.proxy.PhilipsHUE.configuration.listeners;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsgate.lig.proxy.PhilipsHUE.PhilipsHUEAdapter;

/**
* Listener for all configuration events from clients.
* 
* @author Cédric Gérard
* @since January 7, 2014
* @version 1.0.0
* 
*/
public class PhilipsHUEBridgeCommandListener implements CommandListener {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(PhilipsHUEBridgeCommandListener.class);
	
	/**
	 * The PhilipsHUE bridge service reference
	 */
	private PhilipsHUEAdapter PhilipsAdapter;
	
	/**
	 * Build a Philips HUE bridge listener for client communication
	 * @param bridgeService
	 */
	public PhilipsHUEBridgeCommandListener(PhilipsHUEAdapter PhilipsAdapter) {
		super();
		this.PhilipsAdapter = PhilipsAdapter;
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
		try {
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
			
		}else if(cmd.equalsIgnoreCase("getBridgeInfo")) {
			JSONObject returnResp = new JSONObject();
			JSONObject bridgeInfo = PhilipsAdapter.getBridgeInfo(obj.getString("ip"));
			returnResp.put("TARGET", PhilipsHUEAdapter.CONFIG_TARGET);
			returnResp.put("bridgeInfo", bridgeInfo);
			PhilipsAdapter.getCommunicationService().send(obj.getInt("clientId"), returnResp.toString());
			
		}else if(cmd.equalsIgnoreCase("getBridgeLights")) {
			JSONArray lightList = PhilipsAdapter.getLightList(obj.getString("ip"));
			try {
				JSONObject resp = new JSONObject();
				resp.put("TARGET", PhilipsHUEAdapter.CONFIG_TARGET);
				resp.put("bridgeLights", lightList);
				resp.put("bridgeIp", obj.getString("ip"));
				PhilipsAdapter.getCommunicationService().send(obj.getInt("clientId"), resp.toString());
				
			} catch (JSONException e) {e.printStackTrace();}
			
		}else if(cmd.equalsIgnoreCase("getLightClickedState")) {
			JSONObject lightState = PhilipsAdapter.getLightState(obj.getString("bridge"), obj.getString("id"));
			try {
				JSONObject resp = new JSONObject();
				resp.put("TARGET", PhilipsHUEAdapter.CONFIG_TARGET);
				resp.put("lightClickedState", lightState);
				PhilipsAdapter.getCommunicationService().send(obj.getInt("clientId"), resp.toString());
				
			} catch (JSONException e) {e.printStackTrace();}
			
		}else if (cmd.equalsIgnoreCase("setHUEAttribute")) {
			String attribute = obj.getString("attribute");
			String bridgeIp = obj.getString("bridgeIp");
			String objectId = obj.getString("objectId");
			String lightId = objectId.split("-")[1];
			if(attribute.contentEquals("ct")) {
				long value = Long.valueOf(obj.getString("ct"));
				PhilipsAdapter.setAttribute(bridgeIp, lightId, attribute, value);
			} else if (attribute.contentEquals("colormode")) {
				String value = obj.getString("colormode");
				PhilipsAdapter.setAttribute(bridgeIp, lightId, attribute, value);
			} else if (attribute.contentEquals("xy")) {
				float x = Float.parseFloat(Double.toString(obj.getDouble("x")));
				float y = Float.parseFloat(Double.toString(obj.getDouble("y")));
				PhilipsAdapter.setXY(bridgeIp, lightId, attribute, x, y);
			}
			
		}else if (cmd.equalsIgnoreCase("pushlinkSync")) {
			PhilipsAdapter.startPushLinkAuthentication(obj.getString("ip"));
		}else if (cmd.equalsIgnoreCase("findNewLights")) {
			PhilipsAdapter.searchForNewLights(obj.getString("ip"));
		}else if (cmd.equalsIgnoreCase("findNewLightSerial")) {
			ArrayList<String> serials = new ArrayList<String>();
			serials.add(obj.getString("serial"));
			PhilipsAdapter.searchForNewLightsWithSerials(serials, obj.getString("ip"));
		}else if (cmd.equalsIgnoreCase("updatefirmware")) {
			PhilipsAdapter.updateFirmWare(obj.getString("ip"));
		}
		else { //Unknown command
			logger.warn("This command is unknown for the "+PhilipsHUEAdapter.CONFIG_TARGET+" target");
		}
		} catch (JSONException e) {e.printStackTrace();}
		
	}
	

}
