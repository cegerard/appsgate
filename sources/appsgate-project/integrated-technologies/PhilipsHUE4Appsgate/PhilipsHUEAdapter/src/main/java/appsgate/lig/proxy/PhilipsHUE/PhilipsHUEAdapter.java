package appsgate.lig.proxy.PhilipsHUE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
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
@Component(publicFactory=false)
@Instantiate(name="AppsgatePhilipsHUEAdapter")
@Provides(specifications = { PhilipsHUEServices.class })
public class PhilipsHUEAdapter implements PhilipsHUEServices {

	private static String ApAMIMPL = "PhilipsHUEImpl";
	public static String CONFIG_TARGET = "PHILIPSHUE";
	public static String APP_NAME = "AppsGateUJF";
	
	private PHHueSDK phHueSDK;
	
	private PhilipsBridgeUPnPFinder bridgeFinder;
	private ScheduledExecutorService instanciationService = Executors.newScheduledThreadPool(5);
	
	private ListenerService listenerService;
	private SendWebsocketsService sendToClientService;

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(PhilipsHUEAdapter.class);

	/**
	 * Called by iPOJO when all dependencies are available
	 */
	@Validate
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
	}

	/**
	 * Called by iPOJO when the bundle become not available
	 */
	@Invalidate
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
					jsonResponse.put(lightObj);
				}
			}
			logger.debug("getLightList : "+jsonResponse.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponse;
	}

	@Override
	public JSONArray getNewLights() {
		JSONArray jsonResponse = new JSONArray();
		try {
			
			for(PHBridge bridge : phHueSDK.getAllBridges()) {
				
//				TODO use the findNewLights method instead of direct REST API
//				with this call the next method become useless. Make it fuse with
//				this one.
//				bridge.findNewLights(arg0);
//				bridge.findNewLightsWithSerials(arg0, arg1);
				
				String bridgeIP = bridge.getResourceCache().getBridgeConfiguration().getIpAddress();
				URL url = new URL("http://" + bridgeIP + "/api/"+ APP_NAME + "/lights/new");
				HttpURLConnection server = (HttpURLConnection) url.openConnection();
				server.setDoInput(true);
				server.setRequestMethod("GET");
				server.connect();

				BufferedReader response = new BufferedReader(new InputStreamReader(server.getInputStream()));
				String line = response.readLine();
				String BridgeResponse = "";
				while (line != null) {
					BridgeResponse += line;
					line = response.readLine();
				}
				response.close();
				server.disconnect();

				JSONObject temp_response = new JSONObject(BridgeResponse);
				@SuppressWarnings("unchecked")
				Iterator<String> it = temp_response.keys();
				while(it.hasNext()) {
					String lightID = it.next();
					Object lightObj = temp_response.get(lightID);
					if(lightObj instanceof JSONObject) {
						((JSONObject)lightObj).put("bridgeLightId", lightID);
					}
					jsonResponse.put(lightObj);
				}
			}
			logger.debug(jsonResponse.toString());

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponse;
	}

	@Override
	public boolean searchForNewLights() {
		try {
//			TODO this method fuse with the previous one using findLight method from the new API		
			boolean successState = true;
			
			for(PHBridge bridge : phHueSDK.getAllBridges()) {
				String bridgeIP = bridge.getResourceCache().getBridgeConfiguration().getIpAddress();
				URL url = new URL("http://" + bridgeIP + "/api/"+ APP_NAME + "/lights/");
				HttpURLConnection server = (HttpURLConnection) url.openConnection();
				server.setDoInput(true);
				server.setRequestMethod("POST");
				server.connect();

				BufferedReader response = new BufferedReader(new InputStreamReader(server.getInputStream()));
				String line = response.readLine();
				String BridgeResponse = "";
				while (line != null) {
					BridgeResponse += line;
					line = response.readLine();
				}
				response.close();
				server.disconnect();
				successState &= isSuccess(BridgeResponse);
			}

			return successState;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return false;
	}


	@Override
	public JSONObject getLightState(String bridgeIP, String id) {
		
		PHBridge bridge = getBridgeFromIp(bridgeIP);
		PHLight light = getLightFromId(bridge, id);
		return getLightState(light);
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
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponse;
	}

	@Override
	public boolean setAttribute(String bridgeIP, String id, String attribute, boolean value) {
		//TODO update all setAttribute method with the updateLIghtState with listener method
		try {
			URL url = new URL("http://" + bridgeIP + "/api/"+ APP_NAME + "/lights/"+id+"/state");
			HttpURLConnection server = (HttpURLConnection) url.openConnection();
			server.setDoInput(true);
			server.setDoOutput(true);
			server.setRequestMethod("PUT");
			server.connect();

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
			JSONObject JSONAttribute = new JSONObject();
			JSONAttribute.put(attribute, value);
			logger.debug(JSONAttribute.toString());
			bw.write(JSONAttribute.toString(), 0, JSONAttribute.toString().length());
			bw.flush();
			bw.close();

			BufferedReader response = new BufferedReader(new InputStreamReader(server.getInputStream()));
			String line = response.readLine();
			String BridgeResponse = "";
			while (line != null) {
				BridgeResponse += line;
				line = response.readLine();
			}
			response.close();

			return isSuccess(BridgeResponse);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean setAttribute(String bridgeIP, String id, String attribute, long value) {
		try {
			URL url = new URL("http://" + bridgeIP + "/api/"+ APP_NAME + "/lights/"+id+"/state");
			HttpURLConnection server = (HttpURLConnection) url.openConnection();
			server.setDoInput(true);
			server.setDoOutput(true);
			server.setRequestMethod("PUT");
			server.connect();

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
			JSONObject JSONAttribute = new JSONObject();
			JSONAttribute.put(attribute, value);
			logger.debug(JSONAttribute.toString());
			bw.write(JSONAttribute.toString(), 0, JSONAttribute.toString().length());
			bw.flush();
			bw.close();

			BufferedReader response = new BufferedReader(new InputStreamReader(server.getInputStream()));
			String line = response.readLine();
			String BridgeResponse = "";
			while (line != null) {
				BridgeResponse += line;
				line = response.readLine();
			}
			response.close();

			return isSuccess(BridgeResponse);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean setAttribute(String bridgeIP, String id, String attribute, String value) {
		try {
			URL url = new URL("http://" + bridgeIP + "/api/"+ APP_NAME + "/lights/"+id+"/state");
			HttpURLConnection server = (HttpURLConnection) url.openConnection();
			server.setDoInput(true);
			server.setDoOutput(true);
			server.setRequestMethod("PUT");
			server.connect();

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
			JSONObject JSONAttribute = new JSONObject();
			JSONAttribute.put(attribute, value);
			logger.debug(JSONAttribute.toString());
			bw.write(JSONAttribute.toString(), 0, JSONAttribute.toString().length());
			bw.flush();
			bw.close();

			BufferedReader response = new BufferedReader(new InputStreamReader(server.getInputStream()));
			String line = response.readLine();
			String BridgeResponse = "";
			while (line != null) {
				BridgeResponse += line;
				line = response.readLine();
			}
			response.close();

			return isSuccess(BridgeResponse);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean setAttribute(String bridgeIP, String id, JSONObject JSONAttribute) {
		try {
			URL url = new URL("http://" + bridgeIP + "/api/"+ APP_NAME + "/lights/"+id+"/state");
			HttpURLConnection server = (HttpURLConnection) url.openConnection();
			server.setDoInput(true);
			server.setDoOutput(true);
			server.setRequestMethod("PUT");
			server.connect();

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
			logger.debug(JSONAttribute.toString());
			bw.write(JSONAttribute.toString(), 0, JSONAttribute.toString().length());
			bw.flush();
			bw.close();

			BufferedReader response = new BufferedReader(new InputStreamReader(server.getInputStream()));
			String line = response.readLine();
			String BridgeResponse = "";
			while (line != null) {
				BridgeResponse += line;
				line = response.readLine();
			}
			response.close();

			return isSuccess(BridgeResponse);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
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
	 * Get the Philips HUE bridge Mac address or empty string is doesn't work
	 * @return the Philips hue bridge Mac address as a String
	 */
	public String getBridgeMacAddress(String bridgeIP) {
		String macAddr = "";
		try {
			URL url = new URL("http://" + bridgeIP + "/api/"+ APP_NAME + "/config");
			HttpURLConnection server = (HttpURLConnection) url.openConnection();
			server.setDoInput(true);
			server.setRequestMethod("GET");
			server.connect();

			BufferedReader response = new BufferedReader(new InputStreamReader(server.getInputStream()));
			String line = response.readLine();
			String BridgeResponse = "";
			while (line != null) {
				BridgeResponse += line;
				line = response.readLine();
			}
			response.close();

			JSONObject jsonResponse = new JSONObject(BridgeResponse);
			if(jsonResponse.has("mac")) {
				macAddr = jsonResponse.getString("mac");
			}else {
				macAddr = jsonResponse.toString();
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return macAddr;
	}

	/**
	 * Indicate if the request is a success or not.
	 * @param BridgeResponse the request to check
	 * @return true if the HTTP response indicate a success action
	 * @throws JSONException
	 */
	private boolean isSuccess(String BridgeResponse) throws JSONException {
		JSONArray temp_response = new JSONArray(BridgeResponse);
		logger.debug(temp_response.toString());

		if(temp_response.length() > 0) {
			String success = temp_response.getJSONObject(0).getString("success");
			return (success != null);
		}

		return false;
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
	 * Get the subscribe service form OSGi/iPOJO. This service is optional.
	 * 
	 * @param listenerService
	 *            , the subscription service
	 */
	@Bind(optional = false)
	public void bindSubscriptionService(ListenerService listenerService) {
		this.listenerService = listenerService;
		logger.debug("Communication subscription service dependency resolved");
	}

	/**
	 * Call when the EnOcean proxy release the optional subscription service.
	 * 
	 * @param listenerService
	 *            , the released subscription service
	 */
	@Unbind(optional = false)
	public void unbindSubscriptionService(ListenerService listenerService) {
		this.listenerService = null;
		logger.debug("Subscription service dependency not available");
	}

	/**
	 * Get the communication service from OSGi/iPojo. This service is optional.
	 * 
	 * @param sendToClientService
	 *            , the communication service
	 */
	@Bind(optional = false)
	public void bindCommunicationService(SendWebsocketsService sendToClientService) {
		this.sendToClientService = sendToClientService;
		logger.debug("Communication service dependency resolved");
	}
	
	/**
	 * Call when the EnOcean proxy release the communication service.
	 * 
	 * @param sendToClientService
	 *            , the communication service
	 */
	@Unbind(optional = false)
	public void unbindCommunicationService(SendWebsocketsService sendToClientService) {
		this.sendToClientService = null;
		logger.debug("Communication service dependency not available");
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

}