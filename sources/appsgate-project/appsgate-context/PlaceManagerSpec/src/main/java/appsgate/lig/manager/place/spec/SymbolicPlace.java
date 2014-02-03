package appsgate.lig.manager.place.spec;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an abstract representation of
 * a place
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 26, 2013
 *
 */
public class SymbolicPlace {
	
	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(SymbolicPlace.class);

	/**
	 * The place unique identifier
	 */
	private String id;
	
	/**
	 * The place name
	 */
	private String name;
	
	/**
	 * tags list
	 */
	private ArrayList<String> tags;
	
	/**
	 * Place properties list
	 */
	private HashMap<String, String> properties;
	
	/**
	 * The parent place in the hierarchy
	 */
	private SymbolicPlace parent;
	
	/**
	 * Children places of the current place
	 */
	private ArrayList<SymbolicPlace> children;
	
	/**
	 * The abstract device list
	 */
	private ArrayList<String> abstractsObjects;
	
	/**
	 * Build a new symbolic place instance from the less information possible
	 * 
	 * @param id the place identifier
	 * @param name the place user name
	 * @param parent the place parent
	 */
	public SymbolicPlace(String id, String name, SymbolicPlace parent) {
		super();
		this.id = id;
		this.name = name;
		this.parent = parent;
		tags = new ArrayList<String>();
		properties = new HashMap<String, String>();
		children = new ArrayList<SymbolicPlace>();
		abstractsObjects = new ArrayList<String>();
	}
	
	/**
	 * Build a symbolic place with tags and properties
	 * 
	 * @param id the place identifier
	 * @param name the place name
	 * @param tags some tags for this place
	 * @param properties some properties for this place
	 * @param parent the place parent
	 */
	public SymbolicPlace(String id, String name, ArrayList<String> tags,
			HashMap<String, String> properties, SymbolicPlace parent) {
		super();
		this.id = id;
		this.name = name;
		this.tags = tags;
		this.properties = properties;
		this.parent = parent;
		this.children = new ArrayList<SymbolicPlace>();
		this.abstractsObjects = new ArrayList<String>();
	}
	
	/**
	 * Build a symbolic place with tags, properties and devices
	 * 
	 * @param id the place identifier
	 * @param name the place name
	 * @param tags some tags for this place
	 * @param properties some properties for this place
	 * @param parent the place parent
	 * @param abstractsObjects devices of services associate to this places
	 */
	public SymbolicPlace(String id, String name, ArrayList<String> tags,
			HashMap<String, String> properties, SymbolicPlace parent,
			ArrayList<String> abstractsObjects) {
		super();
		this.id = id;
		this.name = name;
		this.tags = tags;
		this.properties = properties;
		this.parent = parent;
		this.children = new ArrayList<SymbolicPlace>();
		this.abstractsObjects = abstractsObjects;
	}
	
	/**
	 * The complete Symbolic place constructor
	 * 
	 * @param id the place identifier
	 * @param name the place name
	 * @param tags some tags for this place
	 * @param properties some properties for this place
	 * @param parent the place parent
	 * @param childrens the place sub-places
	 * @param abstractsObjects devices of services associate to this places
	 */
	public SymbolicPlace(String id, String name, ArrayList<String> tags,
			HashMap<String, String> properties, SymbolicPlace parent,
			ArrayList<SymbolicPlace> childrens,
			ArrayList<String> abstractsObjects) {
		super();
		this.id = id;
		this.name = name;
		this.tags = tags;
		this.properties = properties;
		this.parent = parent;
		this.children = childrens;
		this.abstractsObjects = abstractsObjects;
	}


	/**
	 * Get the places identifier
	 * @return place identifier as a String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the place name
	 * @return the current place name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Change the name of this place
	 * @param name the new name of this place
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the parent of this place in the hierarchy
	 * @return the current parent of this place
	 */
	public SymbolicPlace getParent() {
		return parent;
	}

	/**
	 * Move the place in the hierarchy
	 * @param parent the new parent of this place
	 */
	public void setParent(SymbolicPlace parent) {
		this.parent = parent;
	}

	/**
	 * Get the tags list of this place
	 * @return the tags list as an ArrayList<String>
	 */
	public ArrayList<String> getTags() {
		return tags;
	}
	
	/**
	 * Add a new tag to the current tag list
	 * @param newTag the new tag to add
	 */
	public void addTag(String newTag) {
		tags.add(newTag);
	}

	/**
	 * Get the current properties map
	 * @return properties as a HashMap<String, String>
	 */
	public HashMap<String, String> getProperties() {
		return properties;
	}

	/**
	 * Add a new property or update an existing one
	 * @param key the property key
	 * @param value the property value
	 */
	public void addProperty(String key, String value) {
		properties.put(key, value);
	}
	
	/**
	 * Get all children of this place
	 * @return the children list as an ArrayList<SymbolicPlace>
	 */
	public ArrayList<SymbolicPlace> getChildren() {
		return children;
	}

	/**
	 * Add a new child to the children list
	 * @param child the child SymbolicPlace
	 */
	public void addChild(SymbolicPlace child) {
		children.add(child);
	}
	
	/**
	 * Get associate core object identifier list
	 * @return the core object identifier as an ArrayList<String>
	 */
	public ArrayList<String> getAbstractsObjects() {
		return abstractsObjects;
	}

	/**
	 * Check if a core object is located in this place
	 * @param objId the core object identifier
	 * @return true if the core object is associate to this location, false otherwise
	 */
	public boolean isHere(String objId) {
		return abstractsObjects.contains(objId);
	}
	
	/**
	 * Add on object to this place
	 * @param obj the new abstract object identifier
	 */
	public void addObject(String objId) {
		if(abstractsObjects.add(objId)){
			logger.debug("Core device "+objId+ " added to "+name+ "/ "+id);
		} else {
			logger.error("Error adding "+objId+" to "+id);
		}
	}
	
	/**
	 * Remove this object from this place
	 * 
	 * @param obj the object to remove
	 */
	public void removeObject(String objId) {
		if(abstractsObjects.remove(objId)) {
			logger.debug("Core device "+objId+ " remove from "+name+ "/ "+id);
		} else {
			logger.error("Error removing "+objId+" to "+id);
		}
	}

	/**
	 * Remove all the objects from this place 
	 */
	public void removeAll() {
		abstractsObjects.clear();
	}

	/**
	 * Get the JSONdescription of this place
	 * @return the JSONObject
	 */
	public JSONObject getDescription() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", id);
			obj.put("name", name);
			if(parent != null) {
				obj.put("parent", parent.getId());
			}else {
				obj.put("parent", "null");
			}
			
			JSONArray childArray = new JSONArray();
			for(SymbolicPlace child :  children) {
				childArray.put(child.getId());
			}
			obj.put("children", childArray);
			
			JSONArray tagArray = new JSONArray();
			for(String tag :  tags) {
				tagArray.put(tag);
			}
			obj.put("tags", tagArray);
			
			JSONArray propertiesArray = new JSONArray();
			JSONObject property;
			for(String key :  properties.keySet()) {
				property = new JSONObject();
				property.put("key", key);
				property.put("value", properties.get(key));
				propertiesArray.put(property);
			}
			obj.put("properties", propertiesArray);
			
			JSONArray coreObjectArray = new JSONArray();
			for(String id : abstractsObjects) {
				coreObjectArray.put(id);
				
			}
			obj.put("devices", coreObjectArray);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return obj;
	}

}
