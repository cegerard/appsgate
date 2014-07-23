package appsgate.lig.proxy.PhilipsHUE;


import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
import appsgate.lig.proxy.PhilipsHUE.configuration.listeners.PhilipsHUEBridgeCommandListener;
import appsgate.lig.proxy.PhilipsHUE.dao.LightWrapper;
import appsgate.lig.proxy.PhilipsHUE.importer.PhilipsHueFactoryExecutor;
import appsgate.lig.proxy.PhilipsHUE.interfaces.PhilipsHUEServices;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.model.*;
import com.philips.lighting.model.PHLight.PHLightAlertMode;
import com.philips.lighting.model.PHLight.PHLightColorMode;
import com.philips.lighting.model.PHLight.PHLightEffectMode;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

	public static String ApAMIMPL = "PhilipsHUEImpl";
	public static String CONFIG_TARGET = "PHILIPSHUE";

    private Set<PHBridge> bridges;

    private Set<LightWrapper> lights;

	private SendWebsocketsService sendToClientService;

    private ListenerService listenerService;

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(PhilipsHUEAdapter.class);

    private NewLightListener newlightListener;

	/**
	 * Called by ApAM when all dependencies are available
	 */
	public void newInst() {

       if(listenerService.addCommandListener(new PhilipsHUEBridgeCommandListener(this),CONFIG_TARGET)){
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

        logger.info("Uninstantiating {} ...",this.getClass().getSimpleName());
        logger.info("Removing command listener TARGET {} ...",CONFIG_TARGET);
        listenerService.removeCommandListener(CONFIG_TARGET);
        logger.info("Removing command listener {} removed",CONFIG_TARGET);
        logger.debug("{} uninstantiated.",this.getClass().getSimpleName());
	}

	@Override
	public JSONArray getLightList() {

		JSONArray jsonResponse = new JSONArray();
		try {

				for(LightWrapper light : lights) {
					JSONObject lightObj = new JSONObject();

                    PHLightState lightState = light.getLight().getLastKnownLightState();

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
					JSONLightState.put("reachable", String.valueOf(light.getLight().isReachable()));

					lightObj.put("state", JSONLightState);
					lightObj.put("type", light.getLight().getLightType().name());
					lightObj.put("name", light.getLight().getName());
					lightObj.put("modelid", light.getLight().getModelNumber());
					lightObj.put("swversion", light.getLight().getVersionNumber());
					lightObj.put("lightId", light.getBridge().getResourceCache().getBridgeConfiguration().getMacAddress()+"-"+light.getLight().getIdentifier());
					lightObj.put("bridgeLightId", light.getLight().getIdentifier());
					lightObj.put("bridgeIp", light.getBridge().getResourceCache().getBridgeConfiguration().getIpAddress());


					jsonResponse.put(lightObj);
				}

		} catch (Exception e) {
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

		for (PHBridge bridge : bridges) {
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

        for (LightWrapper light: lights) {
            light.getBridge().findNewLightsWithSerials(serials, newlightListener);
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
				}else if(key.contentEquals("hue")) {
					state.setHue(Integer.valueOf(new Long(JSONAttribute.getLong(key)).toString()));
				}else if(key.contentEquals("bri")) {
					state.setBrightness(Integer.valueOf(new Long(JSONAttribute.getLong(key)).toString()));
				}else if(key.contentEquals("sat")) {
					state.setSaturation(Integer.valueOf(new Long(JSONAttribute.getLong(key)).toString()));
				}else if(key.contentEquals("ct")) {
					state.setCt(Integer.valueOf(new Long(JSONAttribute.getLong(key)).toString()));
				}else if(key.contentEquals("transitionTime")) {
					state.setTransitionTime(Integer.valueOf(new Long(JSONAttribute.getLong(key)).toString()));
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

			for(PHBridge bridge : bridges){
				jsonBridge = new JSONObject();
				PHBridgeConfiguration bc = bridge.getResourceCache().getBridgeConfiguration();
				jsonBridge.put("ip", bc.getIpAddress());
				jsonBridge.put("MAC", bc.getMacAddress());
				jsonBridge.put("status", "OK");
				jsonBridge.put("lights", getLightNumber(bridge));
				
				jsonResponse.put(jsonBridge);
			}

			logger.debug("getBridgeList : "+jsonResponse.toString());

		} catch (JSONException e) {
            e.printStackTrace();
        }

		return jsonResponse;
	}
	

	/**
	 * Method call to notify that a new Philips HUE bridge
	 * has been discovered.
	 * @param bridge Philips HUE bridge instance
	 */
	public void notifyNewBridge(PHBridge bridge) {
		//instanciationService.schedule(new LightsInstanciation(bridge), 15, TimeUnit.SECONDS);
	}
	
	/**
	 * Method call to notify that a bridge is not yet reachable
	 * @param ap Philips hue access point
	 */
	public void notifyOldBridge(PHAccessPoint ap) {
        System.out.println("*************** old main creating instance **********");
		Implementation impl = CST.apamResolver.findImplByName(null, ApAMIMPL);
		Set<Instance> insts = impl.getInsts();
		for(Instance inst : insts) {
			if(inst.getProperty("lightBridgeIP").contentEquals(ap.getIpAddress())) {
				((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(inst.getName());
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
	 * @param bridge the bridge
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

        for(PHBridge bridgeToken : bridges ) {
            if(bridgeToken.getResourceCache().getBridgeConfiguration().getIpAddress().contentEquals(brIp)) {
                bridge = bridgeToken;
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

    /**
     * Start the bridge authentication for the first time connection
     * @param bridgeIP the bridge IP address the authenticate with
     */
    public void startPushLinkAuthentication(String bridgeIP) {
        //PHAccessPoint ap = bridgeFinder.getUnauthorizedAccessPoint(bridgeIP);
        //phHueSDK.startPushlinkAuthentication(ap);
    }
	
	/**
	 * Get the ApAM instance corresponding to a specified hue ID
	 * 
	 * @param uid
	 *            , the id of the hue light
	 * @return an ApAM instance
	 */
	private Instance getSensorInstance(String uid) {

        return CST.componentBroker.getInst(uid);

	}


    public void bridgeOnError(Event event){

        Integer code=(Integer)event.getProperty("code");

        try {
            JSONObject resp = new JSONObject();
            if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
                logger.error("BRIDGE NOT RESPONDING");

            } else if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED) {
                logger.warn("Bridge pushlink button not pressed");
                resp.put("TARGET", PhilipsHUEAdapter.CONFIG_TARGET);
                JSONObject content = new JSONObject();
                content.put("header", "Push link button not pressed");
                content.put("text", "you must to push the link button !");
                resp.put("hueToastAlert", content);
                getCommunicationService().send(resp.toString());

            } else if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
                logger.error("BRIDGE AUTHENTICATION FAILED");
                resp.put("TARGET", PhilipsHUEAdapter.CONFIG_TARGET);
                JSONObject content = new JSONObject();
                content.put("header", "Authentication failed");
                content.put("text", "BRIDGE AUTHENTICATION FAILED");
                resp.put("hueToastAlert", content);
                getCommunicationService().send(resp.toString());

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void bridgeOnUpdate(Event event){

        long flag=0;

        PHBridge bridge=(PHBridge)event.getProperty("bridge");

        PHBridgeConfiguration bc = bridge.getResourceCache()
                .getBridgeConfiguration();

        logger.debug("Cache updated: " + flag + " for " + bc.getIpAddress());

        for (PHLight light : bridge.getResourceCache().getAllLights()) {

            Map<String, String> properties = new HashMap<String, String>();
            String deviceID = bc.getMacAddress() + "-" + light.getIdentifier();
            Instance inst = getSensorInstance(deviceID);
            PHLightState lightState = light.getLastKnownLightState();

            if (inst != null) {
                if (light.isReachable()) {
                    PhilipsHueFactoryExecutor.initiateLightStateProperties(properties,lightState);;
                    inst.setAllProperties(properties);
                }
            }
        }

    }

	private class NewLightListener extends PHLightListener {

		@Override
		public void onError(int arg0, String arg1) {
			logger.debug("error code: " +arg0+ " detail: "+arg1);
		}

		@Override
		public void onStateUpdate(Hashtable<String, String> arg0, List<PHHueError> arg1) {
			logger.debug("Light state udpated:");
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
			logger.debug("light action success");
		}

	}


}
