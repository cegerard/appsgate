package appsgate.lig.proxy.PhilipsHUE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.json.JSONArray;
import org.json.JSONObject;

import appsgate.lig.proxy.PhilipsHUE.interfaces.PhilipsHUEServices;

/**
 * This is the adapter for Philips HUE color light technologies. It allows
 * programmers to send RESt commands to the Philips bridge and control all the
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

	private URL url;
	private HttpURLConnection server;

	/**
	 * Called by iPOJO when all dependencies are available
	 */
	@Validate
	public void newInst() {
		try {
			url = new URL("http://194.199.23.136/api/AppsGateUJF/lights/");
			server = (HttpURLConnection) url.openConnection();
			server.setDoInput(true);
			server.setDoOutput(true);
			server.setRequestMethod("GET");
			server.connect();

			String line;

			BufferedReader s = new BufferedReader(new InputStreamReader(server.getInputStream()));
			line = s.readLine();
			while (line != null) {
				System.out.println(line);
				line = s.readLine();
			}
			s.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public JSONArray getLightList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray getNewLights() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean searchForNewLights() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JSONObject getLightState(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setAttribute(String id, String attribute, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setAttribute(JSONObject attributes) {
		// TODO Auto-generated method stub
		return false;
	}

}
