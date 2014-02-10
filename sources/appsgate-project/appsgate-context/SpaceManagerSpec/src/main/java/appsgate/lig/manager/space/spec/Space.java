package appsgate.lig.manager.space.spec;

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
public class Space {
	
	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(Space.class);

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
	private Space parent;
	
	/**
	 * Children places of the current place
	 */
	private ArrayList<Space> children;
	
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
	public Space(String id, String name, Space parent) {
		super();
		this.id = id;
		this.name = name;
		setParent(parent);
		tags = new ArrayList<String>();
		properties = new HashMap<String, String>();
		children = new ArrayList<Space>();
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
	public Space(String id, String name, ArrayList<String> tags,
			HashMap<String, String> properties, Space parent) {
		super();
		this.id = id;
		this.name = name;
		this.tags = tags;
		this.properties = properties;
		setParent(parent);
		this.children = new ArrayList<Space>();
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
	public Space(String id, String name, ArrayList<String> tags,
			HashMap<String, String> properties, Space parent,
			ArrayList<String> abstractsObjects) {
		super();
		this.id = id;
		this.name = name;
		this.tags = tags;
		this.properties = properties;
		setParent(parent);
		this.children = new ArrayList<Space>();
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
	public Space(String id, String name, ArrayList<String> tags,
			HashMap<String, String> properties, Space parent,
			ArrayList<Space> childrens,
			ArrayList<String> abstractsObjects) {
		super();
		this.id = id;
		this.name = name;
		this.tags = tags;
		this.properties = properties;
		setParent(parent);
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
	public Space getParent() {
		return parent;
	}

	/**
	 * Move the place in the hierarchy
	 * @param parent the new parent of this place
	 */
	public void setParent(Space parent) {
		Space oldParent = this.parent;
		this.parent = parent;
		
		if(parent != null) {
			parent.addChild(this);
		}
		
		if(oldParent != null) {
			oldParent.removeChild(this);
		}
	}

	/**
	 * Get the tags list of this place
	 * @return the tags list as an ArrayList<String>
	 */
	public ArrayList<String> getTags() {
		return tags;
	}
	
	/**
	 * Set tags list
	 * @param tags the new tags list
	 */
	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}
	
	/**
	 * Remove all tags of this place
	 */
	public void clearTags() {
		tags.clear();
	}
	
	/**
	 * Add a new tag to the current tag list
	 * @param newTag the new tag to add
	 */
	public boolean addTag(String newTag) {
		return tags.add(newTag);
	}
	
	/**
	 * Remove a tag from the current tag list
	 * @param newTag the new tag to add
	 */
	public boolean removeTag(String tag) {
		return tags.remove(tag);
	}
	
	/**
	 * Test if a tag is associated to this place
	 * @param tag the tag to test
	 * @return true if the tag is associated, false otherwise
	 */
	public boolean isTagged(String tag) {
		return tags.contains(tag);
	}

	/**
	 * Get the current properties map
	 * @return properties as a HashMap<String, String>
	 */
	public HashMap<String, String> getProperties() {
		return properties;
	}

	/**
	 * Set the properties map
	 * @param properties the new properties map
	 */
	public void setProperties(HashMap<String, String> properties) {
		this.properties = properties;
	}
	
	/**
	 * Remove all properties associated to this place
	 */
	public void clearProperties() {
		properties.clear();
	}
	
	/**
	 * Add a new property or update an existing one
	 * @param key the property key
	 * @param value the property value
	 * @return true if key is a new key false if the key exist and the value has been replaced
	 */
	public boolean addProperty(String key, String value) {
		return (properties.put(key, value) == null);
	}
	
	/**
	 * Remove a property from the properties map
	 * @param key the property key
	 * @return true if the property has been removed
	 */
	public boolean removeProperty(String key) {
		return (properties.remove(key) != null);
	}
	
	/**
	 * Get the property value associated to the key parameter
	 * @param key the property key
	 * @return the value associated to the key as a String
	 */
	public String getPropertyValue(String key) {
		return properties.get(key);
	}
	
	/**
	 * Test if the property is associated to this place 
	 * @param key the key of the property
	 * @return true if the property key is found, false otherwise
	 */
	public boolean hasProperty(String key) {
		return properties.containsKey(key);
	}
	
	/**
	 * Get all children of this place
	 * @return the children list as an ArrayList<SymbolicPlace>
	 */
	public ArrayList<Space> getChildren() {
		return children;
	}
	
	/**
	 * Set children of this place
	 * @param children the new children list
	 */
	public void setChildren( ArrayList<Space> children) {
		this.children = children;
		for(Space child : children) {
			child.setParent(parent);
		}
	}

	/**
	 * Add a new child to the children list
	 * @param child the child SymbolicPlace
	 */
	public boolean addChild(Space child) {
		return children.add(child);
	}
	
	/**
	 * Remove a child
	 * @param child the child to remove
	 * @return true if the child is removed, false otherwise
	 */
	public boolean removeChild(Space child) {
		return children.remove(child);
	}
	
	/**
	 * Test if the place contain the place in parameter has a child
	 * @param child the place to test
	 * @return true if child is a child of this place, false otherwise
	 */
	public boolean hasChild(Space child) {
		return children.contains(child);
	}
	
	/**
	 * Get associate core object identifier list
	 * @return the core object identifier as an ArrayList<String>
	 */
	public ArrayList<String> getCoreObjects() {
		return abstractsObjects;
	}
	
	/**
	 * Set the core objects list
	 * @param coreObjectsList the new core objects list
	 */
	public void setCoreObjects(ArrayList<String> coreObjectsList) {
		abstractsObjects = coreObjectsList;
	}
	
	/**
	 * Remove all core objects of this place
	 */
	public void clearCoreObjects() {
		abstractsObjects.clear();
	}
	
	/**
	 * Add on object to this place
	 * @param obj the new abstract object identifier
	 */
	public boolean addCoreObject(String objId) {
		if(abstractsObjects.add(objId)){
			logger.debug("Core device "+objId+ " added to "+name+ "/ "+id);
			return true;
		} else {
			logger.error("Error adding "+objId+" to "+id);
		}
		
		return false;
	}
	
	/**
	 * Remove this object from this place
	 * 
	 * @param obj the object to remove
	 */
	public boolean removeCoreObject(String objId) {
		if(abstractsObjects.remove(objId)) {
			logger.debug("Core device "+objId+ " remove from "+name+ "/ "+id);
			return true;
		} else {
			logger.error("Error removing "+objId+" to "+id);
		}
		return false;
	}
	/**
	 * Check if a core object is located in this place
	 * @param objId the core object identifier
	 * @return true if the core object is associate to this location, false otherwise
	 */
	public boolean hasCoreObject(String objId) {
		return abstractsObjects.contains(objId);
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
			for(Space child :  children) {
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
