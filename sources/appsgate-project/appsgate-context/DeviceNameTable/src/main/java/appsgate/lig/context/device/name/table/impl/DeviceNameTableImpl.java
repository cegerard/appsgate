package appsgate.lig.context.device.name.table.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.device.name.table.messages.TableNameNotificationMsg;
import appsgate.lig.context.device.name.table.spec.DeviceNameTableSpec;
import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.core.object.messages.NotificationMsg;

/**
 * The device name table implementation is an ApAM component to map
 * core devices end user names.
 * 
 * @author Cédric Gérard
 * @since June 7, 2013
 * @version 1.0.0
 *
 *@see DeviceNameTableSpec
 *
 */
public class DeviceNameTableImpl implements DeviceNameTableSpec {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(DeviceNameTableImpl.class);

	/**
	 * Map of user of all name given for an core object or service by an end user
	 */
	HashMap<String, String> userObjectName = new HashMap<String, String>();
	
	/**
	 * Context history pull service to get past table state
	 */
	private DataBasePullService contextHistory_pull;
	
	/**
	 * Context history push service to save the current state
	 */
	private DataBasePushService contextHistory_push;

	@Override
	public void addName(String objectId, String usrId, String newName) {
		userObjectName.put(usrId+"-"+objectId, newName);
		notifyChanges(objectId, usrId, newName);
		
		// save the new devices name table 
		ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
		
		Set<String> keys = userObjectName.keySet();
		for(String key : keys) {
			properties.add(new AbstractMap.SimpleEntry<String,Object>(key, userObjectName.get(key)));
		}
		
		contextHistory_push.pushData_add(this.getClass().getSimpleName(), usrId, objectId, newName, properties);
	}

	@Override
	public void deleteName(String objectId, String usrId) {
		String eventKey = usrId+"-"+objectId;

		Set<String> keys = userObjectName.keySet();
		Iterator<String> keysIt = keys.iterator();
		String removedValue = "";
		
		while (keysIt.hasNext()) {
			String key = keysIt.next();
			if (eventKey.contentEquals(key)) {
				removedValue = userObjectName.get(key);
				userObjectName.remove(key);
				notifyChanges(objectId, usrId, getName(objectId, usrId));
				break;
			}
		}
		
		// save the new devices name table
		ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
		
		for(String key : keys) {
			properties.add(new AbstractMap.SimpleEntry<String,Object>(key, userObjectName.get(key)));
		}
		
		contextHistory_push.pushData_remove(this.getClass().getSimpleName(), usrId, objectId, removedValue, properties);
	}
	
	@Override
	public String getName(String objectId, String usrId) {
		String eventKey = usrId+"-"+objectId;
		
		Set<String> keys = userObjectName.keySet();
		Iterator<String> keysIt = keys.iterator();
		String name="";
		while (keysIt.hasNext()) {
			String key = keysIt.next();
			if (eventKey.contentEquals(key)) {
				name = userObjectName.get(key);
				break;
			}
		}
		
		if(name.contentEquals("") && !usrId.contentEquals("")) {
			return getName(objectId, "");
		}
		
		return name;
	}

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("The device name table has been instanciated");
		
		JSONObject table = contextHistory_pull.pullLastObjectVersion(this.getClass().getSimpleName());
		logger.debug("Restore device name table from database");
		if(table != null){
			try {
				JSONArray state = table.getJSONArray("state");
				int length = state.length();
				int i = 0;
				while(i < length) {
					JSONObject obj = state.getJSONObject(i);
					String key = (String)obj.keys().next();
					userObjectName.put(key, obj.getString(key));
					i++;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		logger.debug("The device name table has been initialized");
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("The device name table has been stopped");
	}
	
	/**
	 * This method uses the ApAM message model. Each call produce a
	 * ContactNotificationMsg object and notifies ApAM that a new message has
	 * been released.
	 * 
	 * @return nothing, it just notifies ApAM that a new message has been
	 *         posted.
	 */
	public NotificationMsg notifyChanges(String objectId, String userId, String name) {
		return new TableNameNotificationMsg(objectId, userId, name);
	}

}
