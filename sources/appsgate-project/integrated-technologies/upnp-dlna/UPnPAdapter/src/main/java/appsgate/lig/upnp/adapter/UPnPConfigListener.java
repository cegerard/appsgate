package appsgate.lig.upnp.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.upnp.UPnPDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.upnp.media.player.MediaPlayer;

/**
 * Listener for all configuration events from clients.
 * 
 * @author Thibaud
 * 
 */
public class UPnPConfigListener implements CommandListener {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(UPnPConfigListener.class);
	
	public static String CONFIG_TARGET = "UPNP";

	private SendWebsocketsService sendToClientService;

	public UPnPConfigListener(SendWebsocketsService sendToClientService) {
		this.sendToClientService = sendToClientService;

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

	private JSONArray getBrowsers() throws JSONException {
		JSONArray browserList = new JSONArray();

		Implementation implementation = CST.apamResolver.findImplByName(null,
				"MediaBrowser");

		for (Instance browserInstance : implementation.getInsts()) {
			CoreObjectSpec browser = (CoreObjectSpec) browserInstance
					.getServiceObject();

			JSONObject obj = new JSONObject();
			obj.put("objectId", browser.getAbstractObjectId());
			obj.put("friendlyName",
					browserInstance.getProperty(UPnPDevice.FRIENDLY_NAME));
			browserList.put(obj);
		}
		logger.debug("getBrowsers() : " + browserList);
		return browserList;
	}

	private JSONArray getPlayers() throws JSONException {
		JSONArray playersList = new JSONArray();

		Implementation implementation = CST.apamResolver.findImplByName(null,
				"MediaPlayer");

		for (Instance browserInstance : implementation.getInsts()) {
			CoreObjectSpec objectSpec = (CoreObjectSpec) browserInstance
					.getServiceObject();
			MediaPlayer player = (MediaPlayer) browserInstance
					.getServiceObject();

			JSONObject obj = new JSONObject();
			obj.put("objectId", objectSpec.getAbstractObjectId());
			obj.put("friendlyName",
					browserInstance.getProperty(UPnPDevice.FRIENDLY_NAME));
			obj.put("volume", player.getVolume());
			// TODO : add play/pause/stop status ?
			playersList.put(obj);
		}
		logger.debug("getPlayers() : " + playersList);
		return playersList;
	}

	public void onReceivedConfig(String cmd, JSONObject obj) {
		logger.debug("Config or event received: " + cmd);
		logger.debug("with core: " + obj.toString());
		try {
			if (cmd.equalsIgnoreCase("getMediaServices")) {
				JSONObject returnResp = new JSONObject();
				JSONArray playersList = getPlayers();
				JSONArray browsersList = getBrowsers();

				try {
					returnResp.put("players", playersList);
					returnResp.put("browsers", browsersList);
					JSONObject resp = new JSONObject();
					resp.put("TARGET", CONFIG_TARGET);
					resp.put("mediaServices", returnResp);
					sendToClientService.send(obj.getInt("clientId"),
							resp.toString());

				} catch (JSONException e) {
					e.printStackTrace();
				}

			} else if (cmd.equalsIgnoreCase("getPlayers")) {
				JSONObject returnResp = new JSONObject();
				JSONArray playersList = getPlayers();

				try {
					returnResp.put("TARGET", CONFIG_TARGET);
					returnResp.put("players", playersList);
					sendToClientService.send(obj.getInt("clientId"),
							returnResp.toString());

				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			} else if (cmd.equalsIgnoreCase("getBrowsers")) {
				JSONObject returnResp = new JSONObject();
				JSONArray browsersList = getBrowsers();

				try {
					returnResp.put("browsers", browsersList);
					returnResp.put("TARGET", CONFIG_TARGET);
					sendToClientService.send(obj.getInt("clientId"),
							returnResp.toString());

				} catch (JSONException e) {
					e.printStackTrace();
				}

			} else if (cmd.equalsIgnoreCase("setVolume")) {
				String id = obj.getString("id");
				String volume = obj.getString("volume");
				logger.debug("Setting volume "+volume+" for Device "+id);
				Implementation implementation = CST.apamResolver.findImplByName(null,
						"MediaPlayer");
				MediaPlayer player= (MediaPlayer) implementation.getInst(id).getServiceObject();
				player.setVolume(Integer.valueOf(volume));

			} else { // Unknown command
				logger.warn("This command is unknown for the " + CONFIG_TARGET
						+ " target");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

}
