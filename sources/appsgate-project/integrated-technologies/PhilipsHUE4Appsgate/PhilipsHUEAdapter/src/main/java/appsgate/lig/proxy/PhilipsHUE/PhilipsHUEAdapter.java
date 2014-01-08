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

	private PhilipsBridgeUPnPFinder bridgeFinder;
	private String currentUserName = "AppsGateUJF";
	private ScheduledExecutorService instanciationService = Executors.newScheduledThreadPool(1);
	
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
		logger.debug("PhilipsHUEAdapter instanciated");
		bridgeFinder = new PhilipsBridgeUPnPFinder();
		bridgeFinder.registrer(this);
		bridgeFinder.start();
		
		logger.info("Getting the listeners services...");
		if(listenerService.addConfigListener(CONFIG_TARGET, new PhilipsHUEBridgeConfigListener(this))){
			logger.info("Listeners services dependency resolved.");
		}else{
			logger.warn("Listeners services dependency resolution updated.");
		}
		
		logger.debug("Philips finder started");
	}

	/**
	 * Called by iPOJO when the bundle become not available
	 */
	@Invalidate
	public void delInst() {
		bridgeFinder.stop();
		bridgeFinder.unregistrer(this);
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
			
			for(String bridgeIP : bridgeFinder.getAvailableBridgesIp()) {
			
				URL url = new URL("http://" + bridgeIP + "/api/"+ currentUserName + "/lights/");
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
				
				logger.debug("Bridge response (before jsonize) : "+BridgeResponse);

				JSONObject temp_response = new JSONObject(BridgeResponse);

				@SuppressWarnings("unchecked")
				Iterator<String> it = temp_response.keys();
				while(it.hasNext()) {
					String lightID = it.next();
					JSONObject lightObj = temp_response.getJSONObject(lightID);
					lightObj.put("lightId", lightID);
					jsonResponse.put(lightObj);
				}
			}
			logger.debug("getLightList : "+jsonResponse.toString());

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
	public JSONArray getNewLights() {
		JSONArray jsonResponse = new JSONArray();
		try {
			
			for(String bridgeIP : bridgeFinder.getAvailableBridgesIp()) {
			
				URL url = new URL("http://" + bridgeIP + "/api/"+ currentUserName + "/lights/new");
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
						((JSONObject)lightObj).put("lightId", lightID);
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
			
			boolean successState = true;
			
			for(String bridgeIP : bridgeFinder.getAvailableBridgesIp()) {
			
				URL url = new URL("http://" + bridgeIP + "/api/"+ currentUserName + "/lights/");
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
		JSONObject jsonResponse = null;
		
		try {
			URL url = new URL("http://" + bridgeIP + "/api/"+ currentUserName + "/lights/"+id);
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

			jsonResponse = new JSONObject(BridgeResponse);
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
	public boolean setAttribute(String bridgeIP, String id, String attribute, boolean value) {
		try {
			URL url = new URL("http://" + bridgeIP + "/api/"+ currentUserName + "/lights/"+id+"/state");
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
			URL url = new URL("http://" + bridgeIP + "/api/"+ currentUserName + "/lights/"+id+"/state");
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
			URL url = new URL("http://" + bridgeIP + "/api/"+ currentUserName + "/lights/"+id+"/state");
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
			URL url = new URL("http://" + bridgeIP + "/api/"+ currentUserName + "/lights/"+id+"/state");
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
			
			for(String bridgeIP : bridgeFinder.getBridgesIp()) {
				JSONObject jsonBridge = new JSONObject();
				jsonBridge.put("ip", bridgeIP);
				
				if(!bridgeFinder.isStatusError(bridgeIP)) {
					jsonBridge.put("status", "OK");
					jsonBridge.put("MAC", getBridgeMacAddress(bridgeIP));
					jsonBridge.put("lights", getLightNumber(bridgeIP));
				}else {
					JSONObject error = bridgeFinder.getErrorDetails(bridgeIP);
					if( error.getString("description").equalsIgnoreCase("unauthorized user")) {
						jsonBridge.put("status", "not associated");
					}else {
						jsonBridge.put("status", "critical error");
					}
					jsonBridge.put("MAC", "N.A");
					jsonBridge.put("lights", "N.A");
				}
	
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
			URL url = new URL("http://" + bridgeIP + "/api/"+ currentUserName + "/config");
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
	 * ask the bridge for user name availability
	 * @param ipAddr the bridge IP address
	 * @param error the error tab out JSONObject
	 * @return true if the bridge is already associated, false otherwise
	 */
	public boolean isAssociated(String ipAddr, JSONObject[] error) {
		try {
			URL url = new URL("http://" + ipAddr + "/api/"+ currentUserName+"/");
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

			if(BridgeResponse.startsWith("[")) { //If the returned value is the JSONArray description of the failure
				JSONArray jsonResponse = new JSONArray(BridgeResponse);
				error[0] = jsonResponse.getJSONObject(0);
				return false;
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			try {
				JSONObject errorDetail = new JSONObject();
				errorDetail.put("Exception", "Malformed URL Exception");
				errorDetail.put("source", "PhilispsHUEAdapter");
				errorDetail.put("method", "isAssociated");
				error[0].put("error", errorDetail);
			} catch (JSONException e1) {e1.printStackTrace();}
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			try {
				JSONObject errorDetail = new JSONObject();
				errorDetail.put("Exception", "Input/Output exception with bridge");
				errorDetail.put("source", "PhilispsHUEAdapter");
				errorDetail.put("method", "isAssociated");
				error[0].put("error", errorDetail);
			} catch (JSONException e1) {e1.printStackTrace();}
			return false;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Get the number of lights associated with a specify HUE bridge
	 * @param bridgeIP the HUE bridge address
	 * @return the number of lights connected to the bridge
	 */
	private int getLightNumber(String bridgeIP) {
		int associatedLightsNumber = 0;
		try {
			
			URL url = new URL("http://" + bridgeIP + "/api/"+ currentUserName + "/lights/");
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
				associatedLightsNumber++;
			}
			logger.debug("number of lights with "+bridgeIP+" = "+associatedLightsNumber);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return associatedLightsNumber;
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
		
		private String bridgeMac;
		private String bridgeIp;
		
		public LightsInstanciation(String bridgeMac, String bridgeIp) {
			super();
			this.bridgeMac = bridgeMac;
			this.bridgeIp = bridgeIp;
		}

		public void run() {
			JSONArray lightsArray = getLightList();
			int size = lightsArray.length();
			int i = 0;
			int nbTry = 0;
			
			try {
				while(i < size) {
					JSONObject light = lightsArray.getJSONObject(i);
					String lightId = light.getString("lightId");
					JSONObject lightState = getLightState(bridgeIp, lightId);
					
					if(lightState.getJSONObject("state").getBoolean("reachable")) {
					
						Implementation impl = CST.apamResolver.findImplByName(null, ApAMIMPL);
						Map<String, String> properties = new HashMap<String, String>();
						properties.put("deviceName", 	light.getString("name"));
						properties.put("deviceId", 		bridgeMac+"-"+lightId);
						properties.put("lightBridgeId", lightId);
						properties.put("lightBridgeIP", bridgeIp);
						properties.put("reachable", "true");

						if(impl != null) {
							/*Instance createInstance = */impl.createInstance(null, properties);
							i++;
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
					} else {
						i++;
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		
	}

	/**
	 * Method call to notify that a new Philips HUE bridge
	 * has been discovered.
	 * @param bridgeMac the Philips HUE bridge mac address
	 * @param bridgeIP the Philips HUE bridge IP address
	 */
	public void notifyNewBridge(String bridgeMac, String bridgeIp) {
		instanciationService.schedule(new LightsInstanciation(bridgeMac, bridgeIp), 15, TimeUnit.SECONDS);
	}
	
	/**
	 * Method call to notify that a bridge is not yet reachable
	 * @param bridgeIP the former bridge IP address
	 */
	public void notifyOldBridge(String bridgeIP) {
		Implementation impl = CST.apamResolver.findImplByName(null, ApAMIMPL);
		Set<Instance> insts = impl.getInsts();
		for(Instance inst : insts) {
			if(inst.getProperty("lightBridgeIP").contentEquals(bridgeIP)) {
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

}