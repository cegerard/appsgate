package appsgate.lig.virtual.adapter;

import java.util.HashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.json.JSONException;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.virtual.adapter.services.VirtualObjectManagementService;

@Component(publicFactory=false)
@Instantiate(name="AppsgateSensorVirtualAdapter")
@Provides(specifications= {VirtualObjectManagementService.class})
public class VirtualAdapter implements VirtualObjectManagementService {
	
	/**
	 * Service to communicate with clients
	 */
	@Requires
	private SendWebsocketsService sendToClientService;
	
	private HashMap<String, Instance> objectList = new HashMap<String, Instance>();
	
	@Override
	public boolean addVirtualObject(String type, HashMap<String, String> properties) {
		
		String implName; 
		if(type.contentEquals(VIRTUAL_TEMPERATURE)) {
			implName = "CoreVirtualTemperatureSensorImpl";
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
	public boolean removeVirtualObject(String objectId) {
		Instance inst = objectList.get(objectId);
		ComponentBrokerImpl.disappearedComponent(inst.getName());
		objectList.remove(objectId);
		return true;
	}

	@Override
	public JSONArray getVirtualObjectlist() {
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
		sendToClientService.send(clientId, respType, list);
	}
	
	//TODO add virtual object persitence management

}
