package appsgate.lig.manager.space.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.manager.space.messages.SpaceManagerNotification;
import appsgate.lig.manager.space.spec.SpaceManagerSpec;
import appsgate.lig.manager.space.spec.Space;
import appsgate.lig.manager.space.spec.Space.TYPE;

/**
 * This ApAM component is used to maintain space information for any object
 * in the smart habitat.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 10, 2014
 * 
 * @see SpaceManagerSpec
 * 
 */
public class SpaceManagerImpl implements SpaceManagerSpec {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(SpaceManagerImpl.class);

	/**
	 * This is the hash map to match the space of devices.
	 */
	private HashMap<String, Space> spaceObjectsMap;
	
	/**
	 * Context history pull service to get past spaces state
	 */
	private DataBasePullService contextHistory_pull;
	
	/**
	 * Context history push service to save the current spaces
	 */
	private DataBasePushService contextHistory_push;

	/**
	 * Called by ApAM when all dependencies are available
	 */
	public void newInst() {
		logger.info("space manager starting...");
		spaceObjectsMap = new HashMap<String, Space>();
		
		//restore spaces from data base
		JSONObject spaceMap = contextHistory_pull.pullLastObjectVersion(this.getClass().getSimpleName());
		if(spaceMap != null){
//			HashMap<String, List<String>> tempChildrenMap = new HashMap<String, List<String>>();
			try {
				JSONArray state = spaceMap.getJSONArray("state");
				int length = state.length();
				int i = 0;
				
				while(i < length) {
					JSONObject obj = state.getJSONObject(i);
					
					String spaceId = (String)obj.keys().next();
					JSONObject jsonspace = new JSONObject(obj.getString(spaceId));
					String type = jsonspace.getString("type");
					String parentId = jsonspace.getString("parent");
					JSONArray tags = jsonspace.getJSONArray("tags");
					JSONArray properties = jsonspace.getJSONArray("properties");
					//JSONArray children = jsonspace.getJSONArray("children");
					
					ArrayList<String> tagsList = new ArrayList<String>();
					HashMap<String, String> propertiesList = new HashMap<String, String>();
					//ArrayList<String> coreObjectList = new ArrayList<String>();
					
					int iTagsArray = 0;
					int tagsArrayLength = tags.length();
					while(iTagsArray < tagsArrayLength) {
						tagsList.add(tags.getString(iTagsArray));
						iTagsArray++;
					}
					
					int ipropArray = 0;
					int propArrayLength = properties.length();
					JSONObject prop;
					while(ipropArray < propArrayLength) {
						prop = properties.getJSONObject(ipropArray);
						propertiesList.put(prop.getString("key"), prop.getString("value"));
						ipropArray++;
					}
					
//					int ideviceArray = 0;
//					int deviceArrayLength = devices.length();
//					while(ideviceArray < deviceArrayLength) {
//						coreObjectList.add(devices.getString(ideviceArray));
//						ideviceArray++;
//					}
					
//					JSONArray children = jsonspace.getJSONArray("children");
//					ArrayList<String> childIdList = new ArrayList<String>();
//					int ichildArray = 0;
//					int childArrayLength = children.length();
//					while(ichildArray < childArrayLength) {
//						childIdList.add(children.getString(ichildArray));
//						ichildArray++;
//					}
//					if(childArrayLength > 0) {
//						tempChildrenMap.put(spaceId, childIdList);
//					}
					
					Space loc = new Space(spaceId, TYPE.valueOf(type), tagsList, propertiesList, spaceObjectsMap.get(parentId));

					spaceObjectsMap.put(spaceId, loc);
					i++;
				}
				
//				//Restore children if any
//				for(String key : tempChildrenMap.keySet()) {
//					Space loc = spaceObjectsMap.get(key);
//					List<String> childrenList = tempChildrenMap.get(key);
//					for(String child : childrenList) {
//						loc.addChild(spaceObjectsMap.get(child));
//					}
//				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			addSpace(TYPE.ROOT, null);
			Space root = getRootSpace();
			root.addProperty("name", "root");
			addSpace(TYPE.USER_ROOT, root);
			addSpace(TYPE.HABITAT_CURRENT, root);
			Space currentHab = getCurrentHabitat();
			currentHab.addProperty("name", "habitat1");
			addSpace(TYPE.SPATIAL_ROOT, currentHab);
			addSpace(TYPE.DEVICE_ROOT, currentHab);
			addSpace(TYPE.SERVICE_ROOT, currentHab);
			addSpace(TYPE.PROGRAM_ROOT, currentHab);
		}
		logger.debug("The space manager has been initialized");
	}

	/**
	 * Called by ApAM when the component is not available
	 */
	public void deleteInst() {
		logger.info("Removing space manager...");
	}

	@Override
	public synchronized String addSpace(TYPE type, Space parent) {

		String spaceId = String.valueOf(new String(type.toString()+new Double(Math.random())).hashCode());
		if (!spaceObjectsMap.containsKey(spaceId)) {
			Space newspace = new Space(spaceId, type, parent);
			spaceObjectsMap.put(spaceId, newspace);
			notifyspace(spaceId, type.toString(), "newspace");
			
			// Save the new devices name table 
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
			
			Set<String> keys = spaceObjectsMap.keySet();
			for(String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}

			if( contextHistory_push.pushData_add(this.getClass().getSimpleName(), spaceId, type.toString(), properties)) {
				return spaceId;
			}
		}
		return null;
	}
	
	@Override
	public String addSpace(TYPE type, HashMap<String, String> properties,
			Space parent) {
		String spaceId = String.valueOf(new String(type.toString()+new Double(Math.random())).hashCode());
		if (!spaceObjectsMap.containsKey(spaceId)) {
			Space newspace = new Space(spaceId, type, properties, parent);
			spaceObjectsMap.put(spaceId, newspace);
			notifyspace(spaceId, type.toString(), "newspace");
			
			// Save the new devices name table 
			ArrayList<Map.Entry<String, Object>> propertiesSpace = new ArrayList<Map.Entry<String, Object>>();
			
			Set<String> keys = spaceObjectsMap.keySet();
			for(String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				propertiesSpace.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}

			if( contextHistory_push.pushData_add(this.getClass().getSimpleName(), spaceId, type.toString(), propertiesSpace)) {
				return spaceId;
			}
		}
		return null;
	}
	
	@Override
	public String  addSpace(TYPE type, ArrayList<String> tags, HashMap<String, String> properties,  Space parent) {
		
		String spaceId = String.valueOf(new String(type.toString()+new Double(Math.random())).hashCode());
		if (!spaceObjectsMap.containsKey(spaceId)) {
			Space newspace = new Space(spaceId, type, tags, properties, parent);
			spaceObjectsMap.put(spaceId, newspace);
			notifyspace(spaceId, type.toString(), "newspace");
			
			// Save the new devices name table 
			ArrayList<Map.Entry<String, Object>> propertiesSpace = new ArrayList<Map.Entry<String, Object>>();
			
			Set<String> keys = spaceObjectsMap.keySet();
			for(String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				propertiesSpace.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}

			if( contextHistory_push.pushData_add(this.getClass().getSimpleName(), spaceId, type.toString(), propertiesSpace)) {
				return spaceId;
			}
		}
		return null;
	}

	@Override
	public String addSpace(TYPE type, ArrayList<String> tags, HashMap<String, String> properties, Space parent, ArrayList<Space> children) {
		
		String spaceId = String.valueOf(new String(type.toString()+new Double(Math.random())).hashCode());
		if (!spaceObjectsMap.containsKey(spaceId)) {
			Space newspace = new Space(spaceId, type, tags, properties, parent, children);
			spaceObjectsMap.put(spaceId, newspace);
			notifyspace(spaceId, type.toString(), "newspace");
			
			// Save the new devices name table 
			ArrayList<Map.Entry<String, Object>> propertiesSpace = new ArrayList<Map.Entry<String, Object>>();
			
			Set<String> keys = spaceObjectsMap.keySet();
			for(String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				propertiesSpace.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}

			if( contextHistory_push.pushData_add(this.getClass().getSimpleName(), spaceId, type.toString(), propertiesSpace)) {
				return spaceId;
			}
		}
		return null;
	}

	@Override
	public synchronized boolean removeSpace(Space space) {
		Space parent = space.getParent();
		
		if(parent != null) {
			@SuppressWarnings("unchecked")
			ArrayList<Space> children = (ArrayList<Space>)space.getChildren().clone();
			for(Space child : children) {
				removeSpace(child);
			}
			parent.removeChild(space);
			spaceObjectsMap.remove(space.getId());
			notifyspace(space.getId(), space.getType().toString(), "removespace");
		
			// save the new devices name table 
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
					
			Set<String> keys = spaceObjectsMap.keySet();
			for(String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}
					
			return contextHistory_push.pushData_remove(this.getClass().getSimpleName(), space.getId(), space.getType().toString(), properties);
		}
		return false;
	}
	
	@Override
	public synchronized Space getSpace(String placId) {
		return spaceObjectsMap.get(placId);
	}
	
	@Override
	public synchronized boolean moveSpace(Space space, Space newParent) {
		try {
		
			Space oldParent = space.getParent();
			space.setParent(newParent);
			
			notifyspace(space.getId(), newParent.getId(), "movespace");
			
			// save in data base
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
								
			Set<String> keys = spaceObjectsMap.keySet();
			for(String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}		
			return contextHistory_push.pushData_change(this.getClass().getSimpleName(), space.getId(), oldParent.getId(), newParent.getId(), properties);	
			
		}catch(Exception e){logger.error(e.getMessage());}
		return false;
	}

	@Override
	public synchronized Space getRootSpace() {
		Space rootspace = null;
		for(Space space : spaceObjectsMap.values()) {
			if(space.getType().equals(TYPE.ROOT)) {
				rootspace = space;
				break;
			}
		}
		return rootspace;
	}
	
	@Override
	public Space getCurrentHabitat() {
		Space habitat = null;
		for(Space space : spaceObjectsMap.values()) {
			if(space.getType().equals(TYPE.HABITAT_CURRENT)) {
				habitat = space;
				break;
			}
		}
		return habitat;
	}
	
	@Override
	public Space getDeviceRoot(Space habitat) {
		Space deviceSpace = null;
		for(Space space : habitat.getChildren()) {
			if(space.getType().equals(TYPE.DEVICE_ROOT)) {
				deviceSpace = space;
				break;
			}
		}
		return deviceSpace;
	}

	@Override
	public Space getServiceRoot(Space habitat) {
		Space serviceSpace = null;
		for(Space space : habitat.getChildren()) {
			if(space.getType().equals(TYPE.SERVICE_ROOT)) {
				serviceSpace = space;
				break;
			}
		}
		return serviceSpace;
	}

	@Override
	public Space getUserRoot() {
		Space userSpace = null;
		for(Space space : spaceObjectsMap.values()) {
			if(space.getType().equals(TYPE.USER_ROOT)) {
				userSpace = space;
				break;
			}
		}
		return userSpace;
	}

	@Override
	public Space getProgramRoot(Space habitat) {
		Space pgmSpace = null;
		for(Space space : habitat.getChildren()) {
			if(space.getType().equals(TYPE.PROGRAM_ROOT)) {
				pgmSpace = space;
				break;
			}
		}
		return pgmSpace;
	}

	@Override
	public Space getSpatialRoot(Space habitat) {
		Space spatialSpace = null;
		for(Space space : habitat.getChildren()) {
			if(space.getType().equals(TYPE.SPATIAL_ROOT)) {
				spatialSpace = space;
				break;
			}
		}
		return spatialSpace;
	}

	@Override
	public synchronized ArrayList<Space> getSpaces() {
		return new ArrayList<Space>(spaceObjectsMap.values());
	}

	@Override
	public synchronized ArrayList<Space> getSpacesWithName(String name) {
		ArrayList<Space> spaceName = new ArrayList<Space>();
		for(Space currentspace : spaceObjectsMap.values()) {
			if(currentspace.getName().contentEquals(name)){
				spaceName.add(currentspace);
			}
		}
		return spaceName;
	}

	@Override
	public synchronized ArrayList<Space> getSpacesWithTags(ArrayList<String> tags) {
		ArrayList<Space> spaceTags = new ArrayList<Space>();
		for(Space currentspace : spaceObjectsMap.values()) {
			if(currentspace.getTags().containsAll(tags)){
				spaceTags.add(currentspace);
			}
		}
		return spaceTags;
	}

	@Override
	public synchronized ArrayList<Space> getSpacesWithProperties(
			ArrayList<String> propertiesKey) {
		ArrayList<Space> spaceprop = new ArrayList<Space>();
		for(Space currentspace : spaceObjectsMap.values()) {
			if(currentspace.getProperties().keySet().containsAll(propertiesKey)){
				spaceprop.add(currentspace);
			}
		}
		return spaceprop;
	}

	@Override
	public synchronized ArrayList<Space> getSpacesWithPropertiesValue(HashMap<String, String> properties) {
		ArrayList<Space> spaceprop = new ArrayList<Space>();
		for(Space currentspace : spaceObjectsMap.values()) {
			HashMap<String, String> currentProperties = currentspace.getProperties();
			boolean equals =  false;
			for(String key : properties.keySet()) {
				if(currentProperties.containsKey(key)){
					if(currentProperties.get(key).contentEquals(properties.get(key))) {
						equals = true;
					}else{
						equals = false;
						break;
					}
				}else {
					equals = false;
					break;
				}
			}
			
			if(equals){
				spaceprop.add(currentspace);
			}
		}
		return spaceprop;
	}

	@Override
	public JSONObject getTreeDescription() {
		Space root = getRootSpace();
		JSONObject tree = root.getDescription();
		
		try {
			for(Space child : root.getChildren()) {
				tree.put(child.getId(), getTreeDescription(child));
			}
		} catch (JSONException e) {e.printStackTrace();}

		return tree;
	}

	@Override
	public JSONObject getTreeDescription(Space root) {
		JSONObject subTree = root.getDescription();
				
		try {
			for(Space child : root.getChildren()) {
				subTree.put(child.getId(), getTreeDescription(child));
			}
		} catch (JSONException e) {e.printStackTrace();}
				
		return subTree;
	}
	
	@Override
	public void updateSpace(JSONObject update) {
		// TODO Auto-generated method stub
		logger.debug("updateSpace method of the SpaceManager not implemented yet");
	}

	/**
	 * Use to notify that new space has been created or has changed
	 * 
	 * @param spaceId the space identifier
	 * @param spaceName the user name of this space
	 * @param type indicate if this notification is a space creation (0) or an update (1)
	 */
	private void notifyspace(String spaceId, String spaceName, String type) {
		notifyChanged(new SpaceManagerNotification(spaceId, spaceName, type));
	}
	
	/**
	 * This method notify ApAM that a new notification message has been produced.
	 * @param notif the notification message to send.
	 * @return nothing it just notify ApAM.
	 */
	public NotificationMsg notifyChanged (NotificationMsg notif) {
		logger.debug("space Notify: "+ notif);
		return notif;
	}
	
	
	
	/*************************/
	/** for JUnit mock test **/
	/*************************/
	public void initiateMock(DataBasePullService pull, DataBasePushService push) {
		this.contextHistory_pull = pull;
		this.contextHistory_push = push;
	}

}
