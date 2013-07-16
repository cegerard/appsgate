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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.proxy.PhilipsHUE.interfaces.PhilipsHUEServices;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;

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
@Component
@Instantiate
@Provides(specifications = { PhilipsHUEServices.class })
public class PhilipsHUEAdapter implements PhilipsHUEServices {

	private static String ApAMIMPL = "PhilipsHUEImpl";

	private PhilipsBridgeUPnPFinder bridgeFinder;
	private String currentUserName = "AppsGateUJF";
	private ScheduledExecutorService instanciationService = Executors.newScheduledThreadPool(1);

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
		bridgeFinder.start();
		logger.debug("Philips finder started");
		logger.debug("PhilipsHUE IP: " + bridgeFinder.getBridgeIp());

		instanciationService.execute(new LightsInstanciation());
	}

	/**
	 * Called by iPOJO when the bundle become not available
	 */
	@Invalidate
	public void delInst() {
		bridgeFinder.stop();
		instanciationService.shutdown();
		try {
			instanciationService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.debug("PhilipsHUEAdapter instanciationService thread crash at termination");
		}
		logger.debug("PhilipsHUEAdapter stopped");
	}

	@Override
	public JSONArray getLightList() {
		JSONArray jsonResponse = new JSONArray();
		try {
			URL url = new URL("http://" + bridgeFinder.getBridgeIp() + "/api/"+ currentUserName + "/lights/");
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

			JSONObject temp_response = new JSONObject(BridgeResponse);

			@SuppressWarnings("unchecked")
			Iterator<String> it = temp_response.keys();
			while(it.hasNext()) {
				String lightID = it.next();
				JSONObject lightObj = temp_response.getJSONObject(lightID);
				lightObj.put("lightId", lightID);
				jsonResponse.put(lightObj);
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
			URL url = new URL("http://" + bridgeFinder.getBridgeIp() + "/api/"+ currentUserName + "/lights/new");
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
			URL url = new URL("http://" + bridgeFinder.getBridgeIp() + "/api/"+ currentUserName + "/lights/");
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
	public JSONObject getLightState(String id) {
		JSONObject jsonResponse = null;
		try {
			URL url = new URL("http://" + bridgeFinder.getBridgeIp() + "/api/"+ currentUserName + "/lights/"+id);
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
	public boolean setAttribute(String id, String attribute, boolean value) {
		try {
			URL url = new URL("http://" + bridgeFinder.getBridgeIp() + "/api/"+ currentUserName + "/lights/"+id+"/state");
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
	public boolean setAttribute(String id, String attribute, long value) {
		try {
			URL url = new URL("http://" + bridgeFinder.getBridgeIp() + "/api/"+ currentUserName + "/lights/"+id+"/state");
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
	public boolean setAttribute(String id, String attribute, String value) {
		try {
			URL url = new URL("http://" + bridgeFinder.getBridgeIp() + "/api/"+ currentUserName + "/lights/"+id+"/state");
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
	public boolean setAttribute(String id, JSONObject JSONAttribute) {
		try {
			URL url = new URL("http://" + bridgeFinder.getBridgeIp() + "/api/"+ currentUserName + "/lights/"+id+"/state");
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
		
		public LightsInstanciation() {
			super();
		}

		public void run() {
			JSONArray lightsArray = getLightList();
			int size = lightsArray.length();
			int i = 0;
			try {
				while(i < size) {
					JSONObject light = lightsArray.getJSONObject(i);
					Implementation impl = CST.apamResolver.findImplByName(null, ApAMIMPL);
					Map<String, String> properties = new HashMap<String, String>();
					properties.put("deviceName", 	light.getString("name"));
					properties.put("deviceId", 		String.valueOf(bridgeFinder.getBridgeIp()+"-"+light.getString("lightId")));
					properties.put("lightBridgeId", light.getString("lightId"));

					/*Instance createInstance = */impl.createInstance(null, properties);//<- It is possible that the thread stop a this line so some instances are not created.
																						//dynaman ?
					i++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}