package appsgate.lig.proxy.PhilipsHUE;


import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLight.PHLightAlertMode;
import com.philips.lighting.model.PHLight.PHLightColorMode;
import com.philips.lighting.model.PHLight.PHLightEffectMode;
import com.philips.lighting.model.PHLightState;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
//import appsgate.lig.light.actuator.philips.HUE.impl.PhilipsHUEFactory;
import appsgate.lig.proxy.PhilipsHUE.configuration.listeners.PhilipsHUEBridgeConfigListener;
import appsgate.lig.proxy.PhilipsHUE.interfaces.PhilipsHUEServices;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;

/**
 * This is the adapter for Philips HUE color light technologies. It allows
 * programmers to send REST commands to the Philips bridge and control all the
 * connected Philips color bulbs.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since May 22, 2013
 * 
 */
public class PhilipsHUEAdapter implements PhilipsHUEServices {

	private static String ApAMIMPL = "PhilipsHUEImpl";
	public static String CONFIG_TARGET = "PHILIPSHUE";
	public static String APP_NAME = "AppsGateUJF";
	
	private PHHueSDK phHueSDK;
	private NewLightListener newlightListener;
	
//	private PhilipsHUEFactory lampFactory;
	
	private PhilipsBridgeUPnPFinder bridgeFinder;
	private ScheduledExecutorService instanciationService = Executors.newScheduledThreadPool(5);
	
	private ListenerService listenerService;
	private SendWebsocketsService sendToClientService;

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(PhilipsHUEAdapter.class);

	/**
	 * Called by ApAM when all dependencies are available
	 */
	public void newInst() {
		
		phHueSDK = PHHueSDK.create();
		phHueSDK.setDeviceName(APP_NAME);
		logger.debug("PhilipsHUEAdapter instanciated");
		
		bridgeFinder = new PhilipsBridgeUPnPFinder(this, phHueSDK);
		bridgeFinder.start();
		logger.debug("Philips finder started");
		
		logger.info("Getting the listeners services...");
		if(listenerService.addConfigListener(CONFIG_TARGET, new PhilipsHUEBridgeConfigListener(this))){
			logger.info("Listeners services dependency resolved.");
		}else{
			logger.warn("Listeners services dependency resolution updated.");
		}
		
		newlightListener = new NewLightListener();
	}

	/**
	 * Called by ApAM when the bundle become not available
	 */
	public void delInst() {
		bridgeFinder.stop();
		phHueSDK.destroySDK();
		instanciationService.shutdown();
		try {
			instanciationService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.debug("PhilipsHUEAdapter instanciationService thread crash at termination");
		}
		
		if(listenerService.removeConfigListener(CONFIG_TARGET)){
			logger.info("HUE configuration listener removed.");
		}else{
			logger.warn("HUE configuration listener remove failed.");
		}
		
		logger.debug("PhilipsHUEAdapter stopped");
	}

	@Override
	public JSONArray getLightList() {
		JSONArray jsonResponse = new JSONArray();
		try {
			
			for(PHBridge bridge : phHueSDK.getAllBridges()) {
				
				PHBridgeConfiguration bc = bridge.getResourceCache().getBridgeConfiguration();
				List<PHLight> lightsList = bridge.getResourceCache().getAllLights();
				
				for(PHLight light : lightsList) {
					JSONObject lightObj = new JSONObject();
					
					JSONObject JSONLightState = new JSONObject();
					PHLightState lightState = light.getLastKnownLightState();
					JSONLightState.put("on", lightState.isOn().toString());
					JSONLightState.put("bri", String.valueOf(lightState.getBrightness()));
					JSONLightState.put("hue", String.valueOf(lightState.getHue()));
					JSONLightState.put("sat", String.valueOf(lightState.getSaturation()));
					JSONLightState.put("x", String.valueOf(lightState.getX()));
					JSONLightState.put("y", String.valueOf(lightState.getY()));
					JSONLightState.put("ct", String.valueOf(lightState.getCt()));
					JSONLightState.put("alert", lightState.getAlertMode().name());
					JSONLightState.put("effect", lightState.getEffectMode().name());
					JSONLightState.put("colorMode", lightState.getColorMode().name());
					JSONLightState.put("transitionTime", String.valueOf(lightState.getTransitionTime()));
					JSONLightState.put("reachable", String.valueOf(light.isReachable()));
					lightObj.put("state", JSONLightState);
					
					lightObj.put("type", light.getLightType().name());
					lightObj.put("name", light.getName());
					lightObj.put("modelid", light.getModelNumber());
					lightObj.put("swversion", light.getVersionNumber());
					lightObj.put("lightId", bc.getMacAddress()+"-"+light.getIdentifier());
					lightObj.put("bridgeLightId", light.getIdentifier());
					lightObj.put("bridgeIp", bc.getIpAddress());
					jsonResponse.put(lightObj);
				}
			}
			logger.debug("getLightList : "+jsonResponse.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponse;
	}
	
	/**
	 * Get the lights list from the specify bridge 
	 * @param bridgeIP the bridge from where get the light list
	 * @return the light list as a JSONArray
	 */
	public JSONArray getLightList(String bridgeIP) {
		JSONArray jsonResponse = new JSONArray();
		try {
			
			PHBridge bridge = getBridgeFromIp(bridgeIP);
				
			PHBridgeConfiguration bc = bridge.getResourceCache().getBridgeConfiguration();
			List<PHLight> lightsList = bridge.getResourceCache().getAllLights();
				
			for(PHLight light : lightsList) {
				JSONObject lightObj = new JSONObject();
					
				JSONObject JSONLightState = new JSONObject();
				PHLightState lightState = light.getLastKnownLightState();
				JSONLightState.put("on", lightState.isOn().toString());
				JSONLightState.put("bri", String.valueOf(lightState.getBrightness()));
				JSONLightState.put("hue", String.valueOf(lightState.getHue()));
				JSONLightState.put("sat", String.valueOf(lightState.getSaturation()));
				JSONLightState.put("x", String.valueOf(lightState.getX()));
				JSONLightState.put("y", String.valueOf(lightState.getY()));
				JSONLightState.put("ct", String.valueOf(lightState.getCt()));
				JSONLightState.put("alert", lightState.getAlertMode().name());
				JSONLightState.put("effect", lightState.getEffectMode().name());
				JSONLightState.put("colorMode", lightState.getColorMode().name());
				JSONLightState.put("transitionTime", String.valueOf(lightState.getTransitionTime()));
				JSONLightState.put("reachable", String.valueOf(light.isReachable()));
				lightObj.put("state", JSONLightState);
					
				lightObj.put("type", light.getLightType().name());
				lightObj.put("name", light.getName());
				lightObj.put("modelid", light.getModelNumber());
				lightObj.put("swversion", light.getVersionNumber());
				lightObj.put("lightId", bc.getMacAddress()+"-"+light.getIdentifier());
				lightObj.put("bridgeIp", bc.getIpAddress());
				lightObj.put("bridgeLightId", light.getIdentifier());
				jsonResponse.put(lightObj);
			}
			logger.debug("getLightList : "+jsonResponse.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponse;
	}

	@Override
	public boolean searchForNewLights() {
		
		for (PHBridge bridge : phHueSDK.getAllBridges()) {
			bridge.findNewLights(newlightListener);
		}
		return true;
	}
	
	@Override
	public boolean searchForNewLights(String bridgeIP) {
		PHBridge bridge = getBridgeFromIp(bridgeIP);
		bridge.findNewLights(newlightListener);
		return true;
	}
	
	/**
	 * search for new light in the HUE network that have specific serial id
	 * @param serials the list of serial id as a list of String
	 */
	public void searchForNewLightsWithSerials(List<String> serials) {
		for (PHBridge bridge : phHueSDK.getAllBridges()) {
			bridge.findNewLightsWithSerials(serials, newlightListener);
		}
	}
	
	/**
	 * search for new light in the HUE network that have specific serial id
	 * @param serials the list of serial id as a list of String
	 */
	public void searchForNewLightsWithSerials(List<String> serials, String bridgeIP) {
		PHBridge bridge = getBridgeFromIp(bridgeIP);
		bridge.findNewLightsWithSerials(serials, newlightListener);
	}
	
	/**
	 * Update the firmware of the specify bridge
	 * @param bridgeIP the targeted bridge
	 */
	public void updateFirmWare(String bridgeIP) {
		PHBridge bridge = getBridgeFromIp(bridgeIP);
		bridge.updateSoftware(null);
	}


	@Override
	public JSONObject getLightState(String bridgeIP, String id) {
		
		PHBridge bridge = getBridgeFromIp(bridgeIP);
		PHLight light = getLightFromId(bridge, id);
		JSONObject jsonResponse = getLightState(light);
		try {
			jsonResponse.put("lightId", bridge.getResourceCache().getBridgeConfiguration().getMacAddress()+"-"+light.getIdentifier());
			jsonResponse.put("bridgeIp", bridgeIP);
		} catch (JSONException e) {e.printStackTrace();}
		return jsonResponse;
	}
	
	/**
	 * Get the light state from the HUE API
	 * @param light the hue light instance
	 * @return the light state as JSONObject
	 */
	public JSONObject getLightState(PHLight light) {
		JSONObject jsonResponse = null;
		try {
			PHLightState lightState = light.getLastKnownLightState();
		
			jsonResponse = new JSONObject();
		
			JSONObject JSONLightState = new JSONObject();
			JSONLightState.put("on", lightState.isOn().toString());
			JSONLightState.put("bri", String.valueOf(lightState.getBrightness()));
			JSONLightState.put("hue", String.valueOf(lightState.getHue()));
			JSONLightState.put("sat", String.valueOf(lightState.getSaturation()));
			JSONLightState.put("x", String.valueOf(lightState.getX()));
			JSONLightState.put("y", String.valueOf(lightState.getY()));
			JSONLightState.put("ct", String.valueOf(lightState.getCt()));
			JSONLightState.put("alert", lightState.getAlertMode().name());
			JSONLightState.put("effect", lightState.getEffectMode().name());
			JSONLightState.put("colorMode", lightState.getColorMode().name());
			JSONLightState.put("transitionTime", String.valueOf(lightState.getTransitionTime()));
			JSONLightState.put("reachable", String.valueOf(light.isReachable()));
			jsonResponse.put("state", JSONLightState);
		
			jsonResponse.put("type", light.getLightType().name());
			jsonResponse.put("name", light.getName());
			jsonResponse.put("modelid", light.getModelNumber());
			jsonResponse.put("swversion", light.getVersionNumber());
			jsonResponse.put("bridgeLightId", light.getIdentifier());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponse;
	}

	@Override
	public boolean setAttribute(String bridgeIP, String id, String attribute, boolean value) {
		
		PHBridge bridge = getBridgeFromIp(bridgeIP);
		PHLight light = getLightFromId(bridge, id);
		
		PHLightState state = new PHLightState();
		state.setOn(value);
		
		bridge.updateLightState(light, state);
		
		return true;
	}

	@Override
	public boolean setAttribute(String bridgeIP, String id, String attribute, long value) {
		
		PHBridge bridge = getBridgeFromIp(bridgeIP);
		PHLight light = getLightFromId(bridge, id);
		
		PHLightState state = new PHLightState();
		int convertedValue = Integer.valueOf(new Long(value).toString()); 
		if(attribute.contentEquals("hue")) {
			state.setHue(convertedValue);
		} else if(attribute.contentEquals("bri")) {
			state.setBrightness(convertedValue);
		} else if(attribute.contentEquals("sat")) {
			state.setSaturation(convertedValue);
		} else if(attribute.contentEquals("ct")) {
			state.setCt(convertedValue);
		} else if(attribute.contentEquals("transitionTime")) {
			state.setTransitionTime(convertedValue);
		} else {
			return false;
		}
		
		bridge.updateLightState(light, state);
		return true;
	}

	@Override
	public boolean setAttribute(String bridgeIP, String id, String attribute, String value) {
		
		PHBridge bridge = getBridgeFromIp(bridgeIP);
		PHLight light = getLightFromId(bridge, id);
		
		PHLightState state = new PHLightState();

		if(attribute.contentEquals("alert")) {
			if(value.contentEquals("select")) {
				state.setAlertMode(PHLightAlertMode.ALERT_SELECT);
			}else if(value.contentEquals("lselect")) {
				state.setAlertMode(PHLightAlertMode.ALERT_LSELECT);
			}else if(value.contentEquals("none")) {
				state.setAlertMode(PHLightAlertMode.ALERT_NONE);
			}else {
				state.setAlertMode(PHLightAlertMode.ALERT_UNKNOWN);
				return false;
			}
		}else if(attribute.contentEquals("effect")) {
			if(value.contentEquals("colorloop")) {
				state.setEffectMode(PHLightEffectMode.EFFECT_COLORLOOP);
			}else if(value.contentEquals("none")) {
				state.setEffectMode(PHLightEffectMode.EFFECT_NONE);
			}else {
				state.setEffectMode(PHLightEffectMode.EFFECT_UNKNOWN);
				return false;
			}
		}else if(attribute.contentEquals("colormode")) {
			if(value.contentEquals("hs")) {
				state.setColorMode(PHLightColorMode.COLORMODE_HUE_SATURATION);
			}else if(value.contentEquals("ct")) {
				state.setColorMode(PHLightColorMode.COLORMODE_CT);
			}else if(value.contentEquals("none")) {
				state.setColorMode(PHLightColorMode.COLORMODE_NONE);
			}else if(value.contentEquals("xy")) {
				state.setColorMode(PHLightColorMode.COLORMODE_XY);
			}else if(value.contentEquals("unknown")) {
				state.setColorMode(PHLightColorMode.COLORMODE_UNKNOWN);
			}else {
				return false;
			}
		}else{
			return false;
		}
		
		bridge.updateLightState(light, state);
		return true;
	}

	@Override
	public boolean setAttribute(String bridgeIP, String id, JSONObject JSONAttribute) {
		
		PHBridge bridge = getBridgeFromIp(bridgeIP);
		PHLight light = getLightFromId(bridge, id);
		
		PHLightState state = new PHLightState();
		
		@SuppressWarnings("unchecked")
		Iterator<String> it = JSONAttribute.keys();
		while(it.hasNext()) {
			String key = it.next();

			try {
				if(key.contentEquals("on")) {
					state.setOn(JSONAttribute.getBoolean(key));
				}else if(key.contentEquals("hue") || key.contentEquals("bri")
						|| key.contentEquals("sat") || key.contentEquals("ct")
						|| key.contentEquals("transitionTime") ) {
					state.setHue(Integer.valueOf(new Long(JSONAttribute.getLong(key)).toString()));
				
				}else if(key.contentEquals("alert")) {
					String alert = JSONAttribute.getString(key);
					if(alert.contentEquals("select")) {
						state.setAlertMode(PHLightAlertMode.ALERT_SELECT);
					}else if(alert.contentEquals("lselect")) {
						state.setAlertMode(PHLightAlertMode.ALERT_LSELECT);
					}else if(alert.contentEquals("none")) {
						state.setAlertMode(PHLightAlertMode.ALERT_NONE);
					}
				}else if(key.contentEquals("effect")) {
					String effect = JSONAttribute.getString(key);
					if(effect.contentEquals("colorloop")) {
						state.setEffectMode(PHLightEffectMode.EFFECT_COLORLOOP);
					}else if(effect.contentEquals("none")) {
						state.setEffectMode(PHLightEffectMode.EFFECT_NONE);
					}
				}else if(key.contentEquals("colormode")) {
					String colorMode = JSONAttribute.getString(key);
					if(colorMode.contentEquals("hs")) {
						state.setColorMode(PHLightColorMode.COLORMODE_HUE_SATURATION);
						}else if(colorMode.contentEquals("ct")) {
						state.setColorMode(PHLightColorMode.COLORMODE_CT);
					}else if(colorMode.contentEquals("none")) {
						state.setColorMode(PHLightColorMode.COLORMODE_NONE);
					}else if(colorMode.contentEquals("xy")) {
						state.setColorMode(PHLightColorMode.COLORMODE_XY);
					}else if(colorMode.contentEquals("unknown")) {
						state.setColorMode(PHLightColorMode.COLORMODE_UNKNOWN);
					}
				}else{
					return false;
				}
			}catch(JSONException ex) {
				ex.printStackTrace();
				return false;
			}
		}
			
		bridge.updateLightState(light, state);
		return true;
	}
	
	/**Set the xy parameter for HUE lights
	 * @param bridgeIP the bridge that manage the light
	 * @param id the light id on the bridge
	 * @param attribute the attribute as a string
	 * @param x the x value
	 * @param y the y value
	 * @return true if the parameters are set, false otherwise
	 */
	public boolean setXY(String bridgeIP, String id, String attribute, float x, float y) {
		
		PHBridge bridge = getBridgeFromIp(bridgeIP);
		PHLight light = getLightFromId(bridge, id);
		
		PHLightState state = new PHLightState();

		if(attribute.contentEquals("xy")) {
			state.setX(x);
			state.setY(y);
		}else{
			return false;
		}
		
		bridge.updateLightState(light, state);
		return true;
	}
	
	@Override
	public JSONArray getBridgeList() {
		JSONArray jsonResponse = new JSONArray();
		try {
			JSONObject jsonBridge;
			
			for(PHBridge bridge : phHueSDK.getAllBridges()){
				jsonBridge = new JSONObject();
				PHBridgeConfiguration bc = bridge.getResourceCache().getBridgeConfiguration();
				jsonBridge.put("ip", bc.getIpAddress());
				jsonBridge.put("MAC", bc.getMacAddress());
				jsonBridge.put("status", "OK");
				jsonBridge.put("lights", getLightNumber(bridge));
				
				jsonResponse.put(jsonBridge);
			}
				
			for(PHAccessPoint accessPoint : bridgeFinder.getUnauthorizedAccessPoints()) {
				jsonBridge = new JSONObject();
				jsonBridge.put("ip", accessPoint.getIpAddress());
				jsonBridge.put("MAC", accessPoint.getMacAddress());
				jsonBridge.put("status", "not associated");
				jsonBridge.put("lights", "N.A");
	
				jsonResponse.put(jsonBridge);
			}
			
			logger.debug("getBridgeList : "+jsonResponse.toString());

		} catch (JSONException e) {e.printStackTrace();}
		
		return jsonResponse;
	}
	
	/**
	 * Start the bridge authentication for the first time connection
	 * @param bridgeIP the bridge IP address the authenticate with
	 */
	public void startPushLinkAuthentication(String bridgeIP) {
		PHAccessPoint ap = bridgeFinder.getUnauthorizedAccessPoint(bridgeIP);
		phHueSDK.startPushlinkAuthentication(ap);
	}
	
	/**
	 * Inner class for Philips HUE lights instanciation thread
	 * @author Cédric Gérard
	 * @since June 26, 2013
	 * @version 1.0.0
	 */
	private class LightsInstanciation implements Runnable {
		
		private PHBridge bridge;
		
		public LightsInstanciation(PHBridge bridge) {
			super();
			this.bridge = bridge;
		}

		public void run() {
			
			PHBridgeConfiguration bc  = bridge.getResourceCache().getBridgeConfiguration();
			int nbTry = 0;
			
			try {
				for(PHLight light : bridge.getResourceCache().getAllLights()){
				
					if(light.isReachable()) {
					
						Implementation impl = CST.apamResolver.findImplByName(null, ApAMIMPL);
						Map<String, String> properties = new HashMap<String, String>();
						properties.put("deviceName", 	light.getName());
						properties.put("deviceId", 		bc.getMacAddress()+"-"+light.getIdentifier());
						properties.put("lightBridgeId", light.getIdentifier());
						properties.put("lightBridgeIP", bc.getIpAddress());
						properties.put("reachable", "true");

						if(impl != null) {
							impl.createInstance(null, properties);
							nbTry = 0;
						}else {
							synchronized(this){wait(3000);}
							nbTry++;
							if(nbTry == 5){
								logger.error("No "+ApAMIMPL+" found !");
								logger.error("Stop the HUE light instanciation thread !");
								break;
							}
						}
					}else {
						logger.warn("Don't instanciate not reachable HUE light: "+bc.getMacAddress()+"-"+light.getIdentifier());
					}
				}
			}catch(InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		
	}

	/**
	 * Method call to notify that a new Philips HUE bridge
	 * has been discovered.
	 * @param bridgethe Philips HUE bridge instance
	 */
	public void notifyNewBridge(PHBridge bridge) {
		instanciationService.schedule(new LightsInstanciation(bridge), 15, TimeUnit.SECONDS);
	}
	
	/**
	 * Method call to notify that a bridge is not yet reachable
	 * @param ap Philips hue access point
	 */
	public void notifyOldBridge(PHAccessPoint ap) {
		Implementation impl = CST.apamResolver.findImplByName(null, ApAMIMPL);
		Set<Instance> insts = impl.getInsts();
		for(Instance inst : insts) {
			if(inst.getProperty("lightBridgeIP").contentEquals(ap.getIpAddress())) {
				ComponentBrokerImpl.disappearedComponent(inst.getName());
			}
		}
	}
	
	/**
	 * Get the communication service instance
	 * @return the Websocket communication service instance
	 */
	public SendWebsocketsService getCommunicationService(){
		return sendToClientService;
	}
	
	/**
	 * Get the number of associated light to the specify bridge
	 * @param brdieg the bridge
	 * @return the number of light as a String
	 */
	private String getLightNumber(PHBridge bridge) {
		return String.valueOf(bridge.getResourceCache().getAllLights().size());
	}
	
	/**
	 * Get the bridge instance from it's IP address
	 * @param brIp the bridge IP
	 * @return the bridge instance references by to the bridge IP
	 */
	private PHBridge getBridgeFromIp(String brIp) {
		PHBridge bridge= null;
		for(PHBridge br : phHueSDK.getAllBridges()) {
			if(br.getResourceCache().getBridgeConfiguration().getIpAddress().contentEquals(brIp)) {
				bridge = br;
				break;
			}
		}
		return bridge;
	}
	
	/**
	 * Get all bridge information
	 * @param ip the targeted bridge ip adress
	 * @return all birdge information as a JSONObject
	 */
	public JSONObject getBridgeInfo(String ip) {
		PHBridge bridge = getBridgeFromIp(ip);
		JSONObject returnedConf = new JSONObject();
		try {
			if(bridge != null) {
				PHBridgeConfiguration conf = bridge.getResourceCache().getBridgeConfiguration();
				
				returnedConf.put("hueident", conf.getBridgeID());
				returnedConf.put("name", conf.getName());
				returnedConf.put("swversion", conf.getSoftwareVersion());
				returnedConf.put("dhcpenabled", conf.getDhcpEnabled());
				returnedConf.put("gateway", conf.getGateway());
				returnedConf.put("ip", conf.getIpAddress());
				returnedConf.put("mac", conf.getMacAddress());
				returnedConf.put("netmask", conf.getNetmask());
				returnedConf.put("proxy", conf.getProxy());
				returnedConf.put("proxyport", conf.getProxyPort());
				returnedConf.put("proxyenabled", conf.getPortalServicesEnabled());
				returnedConf.put("time", conf.getTime());
				returnedConf.put("timezone", conf.getTimeZone());
				returnedConf.put("localtime", conf.getLocalTime());
				
			}else { //bridge not associated
				PHAccessPoint ap = bridgeFinder.getUnauthorizedAccessPoint(ip);
				returnedConf.put("ip", ap.getIpAddress());
				returnedConf.put("mac", ap.getMacAddress());
				returnedConf.put("error", "has to be associated");
			}
			
		} catch (JSONException e) {e.printStackTrace();}
	
		return returnedConf;
	}
	
	/**
	 * Get the light instance from it's identifier on the specify bridge
	 * @param bridge the targeted bridge
	 * @param lightId the light identifier on the bridge
	 * @return the light instance
	 */
	private PHLight getLightFromId(PHBridge bridge, String lightId) {
		PHLight returnLight= null;
		for(PHLight light : bridge.getResourceCache().getAllLights()) {
			if(light.getIdentifier().contentEquals(lightId)) {
				returnLight = light;
				break;
			}
		}
		return returnLight;
	}
	
	/***********************************************/
	/** 		    INNER CLASS					  **/
	/***********************************************/
	private class NewLightListener extends PHLightListener {

		@Override
		public void onError(int arg0, String arg1) {
			logger.debug("error code: " +arg0+ " detail: "+arg1);
		}

		@Override
		public void onStateUpdate(Hashtable<String, String> arg0, List<PHHueError> arg1) {
			logger.debug("Light state udpate:");
			for(String key : arg0.keySet()) {
				String value = arg0.get(key);
				logger.debug("---- : "+key+" / "+value);
			}
			logger.debug("Light state error:");
			for(PHHueError error : arg1) {
				logger.debug(" ##### : "+error.getCode()+" / "+error.getMessage());
			}
		}

		@Override
		public void onSuccess() {
			// TODO instantiate new light
			logger.debug("LIGHT ACTION SUCCESS");
		}
		
	}

}