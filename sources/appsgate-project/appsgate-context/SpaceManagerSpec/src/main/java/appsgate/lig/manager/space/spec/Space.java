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
 * a Space for AppsGate
 * 
 * @author Cédric Gérard
 * @version 1.2.0
 * @since February 10, 2014
 *
 */
public class Space {
	
	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(Space.class);

	/**
	 * The space unique identifier
	 */
	private String id;
	
	/**
	 * The space name
	 */
	private String name;
	
	/**
	 * tags list for this space
	 */
	private ArrayList<String> tags;
	
	/**
	 * Space properties list always contain the category property
	 */
	private HashMap<String, String> properties;
	
	/**
	 * The parent space in the hierarchy
	 */
	private Space parent;
	
	/**
	 * Sub-spaces of the current space
	 */
	private ArrayList<Space> children;
	
	/**
	 * Build a new space instance from the less information possible
	 * 
	 * @param id the space identifier
	 * @param name the space user name
	 * @param category the space category
	 * @param parent the parent space
	 */
	public Space(String id, String name, CATEGORY category, Space parent) {
		super();
		this.id = id;
		this.name = name;
		setParent(parent);
		children = new ArrayList<Space>();
		tags = new ArrayList<String>();
		properties = new HashMap<String, String>();
		this.setCategory(category);

	}

	/**
	 * Build a space with tags and properties
	 * 
	 * @param id the space identifier
	 * @param name the space name
	 * @param category the space category
	 * @param tags some tags for this space
	 * @param properties some properties for this space
	 * @param parent the parent space
	 */
	public Space(String id, String name, CATEGORY category, ArrayList<String> tags,
			HashMap<String, String> properties, Space parent) {
		super();
		this.id = id;
		this.name = name;
		this.tags = tags;
		this.properties = properties;
		this.setCategory(category);
		setParent(parent);
		this.children = new ArrayList<Space>();
	}
	
	/**
	 * The complete Symbolic space constructor
	 * 
	 * @param id the space identifier
	 * @param name the space name
	 * @param category the space category
	 * @param tags some tags for this space
	 * @param properties some properties for this space
	 * @param parent the parent space
	 * @param childrens all sub-spaces
	 */
	public Space(String id, String name, CATEGORY category, ArrayList<String> tags,
			HashMap<String, String> properties, Space parent,
			ArrayList<Space> childrens,
			ArrayList<String> abstractsObjects) {
		super();
		this.id = id;
		this.name = name;
		this.tags = tags;
		this.properties = properties;
		this.setCategory(category);
		setParent(parent);
		this.children = childrens;
	}


	/**
	 * Get the space identifier
	 * @return space identifier as a String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the space name
	 * @return the current space name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Change the name of this space
	 * @param name the new name of this space
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the parent of this space in the hierarchy
	 * @return the current parent of this space
	 */
	public Space getParent() {
		return parent;
	}

	/**
	 * Move the space in the hierarchy
	 * @param parent the new parent of this space
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
	 * Get the tags list of this space
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
	 * Remove all tags of this space
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
	 * Test if a tag is associated to this space
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
	 * Use only at initialization to set the category of this space
	 * @param category the category value
	 */
	private void setCategory(CATEGORY category) {
		properties.put("category", category.toString());
	}

	/**
	 * Set the properties map
	 * @param properties the new properties map
	 * @return true if the new properties map has been set, false otherwise
	 */
	public boolean setProperties(HashMap<String, String> properties) {
		boolean isOK = false;
		if(this.properties.containsKey("category")) {
			properties.put("category", this.properties.get("category"));
			this.properties = properties;
			isOK = true;
		}else if(properties.containsKey("category")) {
			this.properties = properties;
			isOK = true;
		}
		return isOK;
	}
	
	/**
	 * Remove all properties associated to this space
	 * except the category entry
	 */
	public void clearProperties() {
		String category = this.properties.get("category");
		properties.clear();
		properties.put("category", category);
	}
	
	/**
	 * Add a new property or update an existing one the category entry
	 * can not be replaced
	 * @param key the property key
	 * @param value the property value
	 * @return true if key is a new key false if the key exist and the value has been replaced
	 */
	public boolean addProperty(String key, String value) {
		if(!key.contentEquals("category")) {
			return (properties.put(key, value) == null);
		}
		return false;
	}
	
	/**
	 * Remove a property from the properties map, the category entry
	 * can not be removed
	 * @param key the property key
	 * @return true if the property has been removed
	 */
	public boolean removeProperty(String key) {
		if(!key.contentEquals("category")) {
			return (properties.remove(key) != null);
		}
		return false;
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
	 * Test if the property is associated to this space 
	 * @param key the key of the property
	 * @return true if the property key is found, false otherwise
	 */
	public boolean hasProperty(String key) {
		return properties.containsKey(key);
	}
	
	/**
	 * Get all children of this space
	 * @return the children list as an ArrayList<Symbolicspace>
	 */
	public ArrayList<Space> getChildren() {
		return children;
	}
	
	/**
	 * Set children of this space
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
	 * @param child the child space
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
	 * Test if the space contain the space in parameter has a child
	 * @param child the space to test
	 * @return true if child is a child of this space, false otherwise
	 */
	public boolean hasChild(Space child) {
		return children.contains(child);
	}

	/**
	 * Get the JSONdescription of this space
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
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return obj;
	}
	
	/**
	 * Enumeration use to standardize the basic category
	 * @author Cédric Gérard
	 * @since February 10, 2014
	 */
	public static enum CATEGORY{
		ROOT,
		PLACE,
		USER,
		GROUP,
		DEVICE,
		SERVICE,
		PROGRAM;
	}

}
