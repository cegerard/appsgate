package appsgate.lig.simulation.adapter;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.simulation.adapter.spec.SimulatedObjectManagementService;


public class SimulationAdapter implements SimulatedObjectManagementService {
	
	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(SimulationAdapter.class);
	
	/**
	 * Service to be notified when clients send commands
	 */
	private ListenerService listenerService;
	
	/**
	 * Service to communicate with clients
	 */
	private SendWebsocketsService sendToClientService;
	
	/**
	 * HTTP service dependency resolve by iPojo. Allow to register HTML
	 * resources to the Felix HTTP server
	 */
	private HttpService httpService;
	
	private HashMap<String, Instance> objectList = new HashMap<String, Instance>();
	
	/**
	 * Method call by ApAM when a new instance of Watteco adapter is created
	 */
	public void newInst() {
		logger.debug("Simulation adapter instantiated.");
	}
	

	/**
	 * method call by ApAM when a instance of Watteco adapter disappeared
	 */
	public void delInst() {
		logger.debug("Simulation adapter instance desapear.");
	}
	
	@Override
	public boolean addSimulateObject(String type, HashMap<String, String> properties) {
		
		String implName; 
		if(type.contentEquals(SIMULATED_TEMPERATURE)) {
			implName = "SimulatedTemperatureSensorImpl";
		}else {
			return false;
		}
		
		Implementation impl = CST.apamResolver.findImplByName(null, implName);
		Instance inst = impl.createInstance(null, properties);

		if(objectList.put(properties.get("deviceId"), inst) != null) {
			return true;
		}
		
		return false;
		
	}

	@Override
	public boolean removeSimulatedObject(String objectId) {
		Instance inst = objectList.get(objectId);
		((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(inst.getName());
		objectList.remove(objectId);
		return true;
	}

	@Override
	public JSONArray getSimulateObjectlist() {
		JSONArray list = new JSONArray();
		Instance inst;
		for(String key : objectList.keySet()) {
			inst = objectList.get(key);
			try {
				list.put(((CoreObjectSpec)inst.getServiceObject()).getDescription());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return list;
	}

	public void sendResponse(int clientId, String respType, JSONArray list) {
		if(sendToClientService != null)
			sendToClientService.send(clientId, respType, list);
		else
			logger.error("No client communication service found.");
	}
	
	//TODO add simulate object persistence management

}
