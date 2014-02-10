package appsgate.lig.manager.space.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import appsgate.lig.manager.space.spec.Space.CATEGORY;

/**
 * This ApAM component is used to maintain space information for any object
 * in the smart habitat.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 26, 2013
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
					String name = jsonspace.getString("name");
					String parentId = jsonspace.getString("parent");
					JSONArray tags = jsonspace.getJSONArray("tags");
					JSONArray properties = jsonspace.getJSONArray("properties");
					JSONArray devices = jsonspace.getJSONArray("children");
					
					ArrayList<String> tagsList = new ArrayList<String>();
					HashMap<String, String> propertiesList = new HashMap<String, String>();
					ArrayList<String> coreObjectList = new ArrayList<String>();
					
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
					
					int ideviceArray = 0;
					int deviceArrayLength = devices.length();
					while(ideviceArray < deviceArrayLength) {
						coreObjectList.add(devices.getString(ideviceArray));
						ideviceArray++;
					}
					
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
					
					Space loc = new Space(spaceId, name, tagsList, propertiesList, spaceObjectsMap.get(parentId));

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
			String rootID = addSpace("root", CATEGORY.ROOT.toString(), null);
			addSpace("user", CATEGORY.USER_ROOT.toString(), rootID);
			String habitatID = addSpace("habitat1", CATEGORY.HABITAT_ROOT.toString(), rootID);
			addSpace("place", CATEGORY.PLACE_ROOT.toString(), habitatID);
			addSpace("devices", CATEGORY.DEVICE_ROOT.toString(), habitatID);
			addSpace("services", CATEGORY.SERVICE_ROOT.toString(), habitatID);
			addSpace("pgms", CATEGORY.PROGRAM_ROOT.toString(), habitatID);
			
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
	public synchronized String addSpace(String name, String category,  String parent) {

		String spaceId = String.valueOf(new String(name+new Double(Math.random())).hashCode());
		if (!spaceObjectsMap.containsKey(spaceId)) {
			Space newspace = new Space(spaceId, name, category, spaceObjectsMap.get(parent));
			spaceObjectsMap.put(spaceId, newspace);
			notifyspace(spaceId, name, "newspace");
			
			// Save the new devices name table 
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
			
			Set<String> keys = spaceObjectsMap.keySet();
			for(String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}

			if( contextHistory_push.pushData_add(this.getClass().getSimpleName(), spaceId, name, properties)) {
				return spaceId;
			}
		}
		return null;
	}
	
	@Override
	public String addSpace(String name, String category,
			ArrayList<String> tags, HashMap<String, String> properties,
			String parent) {
		
		String spaceId = String.valueOf(new String(name+new Double(Math.random())).hashCode());
		if (!spaceObjectsMap.containsKey(spaceId)) {
			Space newspace = new Space(spaceId, name, category, tags, properties, spaceObjectsMap.get(parent));
			spaceObjectsMap.put(spaceId, newspace);
			notifyspace(spaceId, name, "newspace");
			
			// Save the new devices name table 
			ArrayList<Map.Entry<String, Object>> propertiesSpace = new ArrayList<Map.Entry<String, Object>>();
			
			Set<String> keys = spaceObjectsMap.keySet();
			for(String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				propertiesSpace.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}

			if( contextHistory_push.pushData_add(this.getClass().getSimpleName(), spaceId, name, propertiesSpace)) {
				return spaceId;
			}
		}
		return null;
	}

	@Override
	public String addSpace(String name, ArrayList<String> tags,
			HashMap<String, String> properties, String parent,
			ArrayList<Space> children) {
		
		String spaceId = String.valueOf(new String(name+new Double(Math.random())).hashCode());
		if (!spaceObjectsMap.containsKey(spaceId)) {
			Space newspace = new Space(spaceId, name, tags, properties, spaceObjectsMap.get(parent), children);
			spaceObjectsMap.put(spaceId, newspace);
			notifyspace(spaceId, name, "newspace");
			
			// Save the new devices name table 
			ArrayList<Map.Entry<String, Object>> propertiesSpace = new ArrayList<Map.Entry<String, Object>>();
			
			Set<String> keys = spaceObjectsMap.keySet();
			for(String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				propertiesSpace.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}

			if( contextHistory_push.pushData_add(this.getClass().getSimpleName(), spaceId, name, propertiesSpace)) {
				return spaceId;
			}
		}
		return null;
	}

	@Override
	public synchronized boolean removeSpace(String spaceId) {
		Space selectedspace = spaceObjectsMap.get(spaceId);
		Space parent = selectedspace.getParent();
		
		if(parent != null) {
			@SuppressWarnings("unchecked")
			ArrayList<Space> children = (ArrayList<Space>)selectedspace.getChildren().clone();
			for(Space child : children) {
				removeSpace(child.getId());
			}
			parent.removeChild(selectedspace);
			spaceObjectsMap.remove(spaceId);
			notifyspace(spaceId, selectedspace.getName(), "removespace");
		
			// save the new devices name table 
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
					
			Set<String> keys = spaceObjectsMap.keySet();
			for(String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}
					
			return contextHistory_push.pushData_remove(this.getClass().getSimpleName(), spaceId, selectedspace.getName(), properties);
		}
		return false;
	}

	@Override
	public synchronized boolean renameSpace(String spaceId, String newName) {
		Space loc = spaceObjectsMap.get(spaceId);
		String oldName = loc.getName();
		loc.setName(newName);
		
		notifyspace(spaceId, newName, "updatespace");
		
		// save the new devices name table 
		ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
							
		Set<String> keys = spaceObjectsMap.keySet();
		for(String e : keys) {
			Space sl = spaceObjectsMap.get(e);
			properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
		}
							
		return contextHistory_push.pushData_change(this.getClass().getSimpleName(), spaceId, oldName, newName, properties);		
	}
	
	@Override
	public synchronized Space getSpace(String placId) {
		return spaceObjectsMap.get(placId);
	}

	@Override
	public synchronized JSONArray getJSONSpaces() {
		Iterator<Space> spaces = spaceObjectsMap.values().iterator();
		JSONArray jsonspaceList = new JSONArray();
		Space loc;

		while (spaces.hasNext()) {
			loc = spaces.next();
			jsonspaceList.put(loc.getDescription());
		}

		return jsonspaceList;
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

	@Override
	public synchronized boolean moveSpace(String spaceId, String newParentId) {
		try {
			Space space = spaceObjectsMap.get(spaceId);
			Space newParent = spaceObjectsMap.get(newParentId);
		
			String oldParent = space.getParent().getId();
			space.setParent(newParent);
			
			notifyspace(spaceId, newParentId, "movespace");
			
			// save in data base
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
								
			Set<String> keys = spaceObjectsMap.keySet();
			for(String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
			}		
			return contextHistory_push.pushData_change(this.getClass().getSimpleName(), spaceId, oldParent, newParentId, properties);	
			
		}catch(Exception e){logger.error(e.getMessage());}
		return false;
	}

	@Override
	public synchronized void setTagsList(String spaceId, ArrayList<String> tags) {
		Space space = spaceObjectsMap.get(spaceId);
		space.setTags(tags);
		
		notifyspace(spaceId, "newTagList", "updatespaceTag");
		
		// save in data base
		ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
							
		Set<String> keys = spaceObjectsMap.keySet();
		for(String e : keys) {
			Space sl = spaceObjectsMap.get(e);
			properties.add(new AbstractMap.SimpleEntry<String,Object>(e, sl.getDescription().toString()));
		}		
		contextHistory_push.pushData_change(this.getClass().getSimpleName(), spaceId, "", "tags", properties);
	}

	@Override
	public synchronized void clearTagsList(String spaceId) {
		Space space = spaceObjectsMap.get(spaceId);
		space.clearTags();
		
		notifyspace(spaceId, "taglistFree", "updatespaceTag");
		
		// save in data base
		ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();

		Set<String> keys = spaceObjectsMap.keySet();
		for (String e : keys) {
			Space sl = spaceObjectsMap.get(e);
			properties.add(new AbstractMap.SimpleEntry<String, Object>(e, sl
					.getDescription().toString()));
		}
		contextHistory_push.pushData_remove(this.getClass().getSimpleName(), spaceId, "tags", properties);
	}

	@Override
	public synchronized boolean addTag(String spaceId, String tag) {
		Space space = spaceObjectsMap.get(spaceId);
		if (space.addTag(tag)) {

			notifyspace(spaceId, tag, "updatespaceTag");
			
			// save in data base
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();

			Set<String> keys = spaceObjectsMap.keySet();
			for (String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				properties.add(new AbstractMap.SimpleEntry<String, Object>(e,
						sl.getDescription().toString()));
			}
			return contextHistory_push.pushData_add(this.getClass().getSimpleName(), spaceId, "tag", tag, properties);
		}
		return false;
	}

	@Override
	public synchronized boolean removeTag(String spaceId, String tag) {
		Space space = spaceObjectsMap.get(spaceId);
		if(space.removeTag(tag)) {
		
			notifyspace(spaceId, tag, "removespaceTag");
			
			// save in data base
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();

			Set<String> keys = spaceObjectsMap.keySet();
			for (String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				properties.add(new AbstractMap.SimpleEntry<String, Object>(e,sl.getDescription().toString()));
			}
			return contextHistory_push.pushData_remove(this.getClass().getSimpleName(), spaceId, "tag", tag, properties);
		}
		return false;
	}

	@Override
	public synchronized void setProperties(String spaceId, HashMap<String, String> properties) {
		Space space = spaceObjectsMap.get(spaceId);
		space.setProperties(properties);
		notifyspace(spaceId, "newPropertiesList", "updatespaceProp");
		// save in data base
		ArrayList<Map.Entry<String, Object>> propertiesDB = new ArrayList<Map.Entry<String, Object>>();

		Set<String> keys = spaceObjectsMap.keySet();
		for (String e : keys) {
			Space sl = spaceObjectsMap.get(e);
			propertiesDB.add(new AbstractMap.SimpleEntry<String, Object>(e,sl.getDescription().toString()));
		}
		contextHistory_push.pushData_change(this.getClass().getSimpleName(), spaceId, "", "properties", propertiesDB);
	}

	@Override
	public synchronized void clearPropertiesList(String spaceId) {
		Space space = spaceObjectsMap.get(spaceId);
		space.clearProperties();
		notifyspace(spaceId, "propertiesListFree", "updatespaceProp");
		// save in data base
		ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();

		Set<String> keys = spaceObjectsMap.keySet();
		for (String e : keys) {
			Space sl = spaceObjectsMap.get(e);
			properties.add(new AbstractMap.SimpleEntry<String, Object>(e,
					sl.getDescription().toString()));
		}
		contextHistory_push.pushData_remove(this.getClass().getSimpleName(), spaceId, "properties", properties);
	}

	@Override
	public synchronized boolean addProperty(String spaceId, String key, String value) {
		Space space = spaceObjectsMap.get(spaceId);
		if( space.addProperty(key, value)) {
			notifyspace(spaceId, key+"-"+value, "updatespaceProp");
			// save in data base
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();

			Set<String> keys = spaceObjectsMap.keySet();
			for (String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				properties.add(new AbstractMap.SimpleEntry<String, Object>(e,sl.getDescription().toString()));
			}
			return contextHistory_push.pushData_add(this.getClass().getSimpleName(), spaceId, "property", key, properties);
		}
		return false;
	}

	@Override
	public synchronized boolean removeProperty(String spaceId, String key) {
		Space space = spaceObjectsMap.get(spaceId);
		if( space.removeProperty(key)) {
			notifyspace(spaceId, key, "removespaceProp");
			// save in data base
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();

			Set<String> keys = spaceObjectsMap.keySet();
			for (String e : keys) {
				Space sl = spaceObjectsMap.get(e);
				properties.add(new AbstractMap.SimpleEntry<String, Object>(e,sl.getDescription().toString()));
			}
			return contextHistory_push.pushData_remove(this.getClass().getSimpleName(), spaceId, "property", key, properties);
		}
		return false;
	}

	@Override
	public synchronized Space getRootSpace() {
		Space rootspace = null;
		for(Space space : spaceObjectsMap.values()) {
			if(space.getParent() == null) {
				rootspace = space;
				break;
			}
		}
		return rootspace;
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
	
	/*************************/
	/** for JUnit mock test **/
	/*************************/
	public void initiateMock(DataBasePullService pull, DataBasePushService push) {
		this.contextHistory_pull = pull;
		this.contextHistory_push = push;
	}
}
