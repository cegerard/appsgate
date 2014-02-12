package appsgate.lig.manager.space.spec;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	 * The space unique identifier
	 */
	private String id;
	
	/**
	 * The space type
	 */
	private TYPE type;
	
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
	 * @param type the space type
	 * @param parent the parent space
	 */
	public Space(String id, TYPE type, Space parent) {
		super();
		this.id = id;
		this.type = type;
		tags = new ArrayList<String>();
		properties = new HashMap<String, String>();
		
		setParent(parent);
		children = new ArrayList<Space>();
	}
	
	/**
	 * Build a new space instance from the less information possible
	 * 
	 * @param id the space identifier
	 * @param type the space type
	 * @param properties some properties for this space
	 * @param parent the parent space
	 */
	public Space(String id, TYPE type, HashMap<String, String> properties, Space parent) {
		super();
		this.id = id;
		this.type = type;
		tags = new ArrayList<String>();
		this.properties = properties;
		
		setParent(parent);
		children = new ArrayList<Space>();
	}

	/**
	 * Build a space with tags and properties
	 * 
	 * @param id the space identifier
	 * @param type the space type
	 * @param tags some tags for this space
	 * @param properties some properties for this space
	 * @param parent the parent space
	 */
	public Space(String id, TYPE type, ArrayList<String> tags,
			HashMap<String, String> properties, Space parent) {
		super();
		this.id = id;
		this.type = type;
		this.tags = tags;
		this.properties = properties;
		
		setParent(parent);
		this.children = new ArrayList<Space>();
	}
	
	/**
	 * The complete space constructor, it don't update other space
	 * 
	 * @param id the space identifier
	 * @param type the space type
	 * @param tags some tags for this space
	 * @param properties some properties for this space
	 * @param parent the parent space
	 * @param childrens all sub-spaces
	 */
	public Space(String id, TYPE type, ArrayList<String> tags,
			HashMap<String, String> properties, Space parent,
			ArrayList<Space> children) {
		super();
		this.id = id;
		this.type = type;
		this.tags = tags;
		this.properties = properties;
		
		this.parent = parent;
		this.children = children;
	}


	/**
	 * Get the space identifier
	 * @return space identifier as a String
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Get the type of this space
	 * @return the type enumerate value
	 */
	public TYPE getType(){
		return type;
	}

	/**
	 * Get the space name if it is set
	 * @return the current space name or an empty string
	 */
	public String getName() {
		String name = properties.get("name");
		if(name != null) {
			return name;
		}
		return "";
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
	 * Set the properties map
	 * @param properties the new properties map
	 */
	public void setProperties(HashMap<String, String> properties) {
		this.properties = properties;
	}
	
	/**
	 * Remove all properties associated to this space
	 * except the category entry
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
	 * Test if the property is associated to this space 
	 * @param key the key of the property
	 * @return true if the property key is found, false otherwise
	 */
	public boolean hasProperty(String key) {
		return properties.containsKey(key);
	}
	
	/**
	 * Get all children of this space
	 * @return the children list as an ArrayList<Space>
	 */
	public ArrayList<Space> getChildren() {
		return children;
	}
	
	/**
	 * Set children of this space but do not update parent field for each child
	 * @param children the new children list
	 */
	public void setChildren( ArrayList<Space> children) {
		this.children = children;
	}
	
	/**
	 * Get a specify sub-space of this space
	 * @param spaceId the space to get
	 * @return the space corresponding to the space id
	 */
	public Space getSubSpace(String spaceId) {
		Space theSubSpace = null;
		for(Space subSpace : children) {
			if(subSpace.getId() == spaceId) {
				theSubSpace = subSpace;
				break;
			}else {
				theSubSpace = subSpace.getSubSpace(spaceId);
				if(theSubSpace != null) {
					break;
				}
			}
		}
		return theSubSpace;
	}
	
	/**
	 * Get all sub-spaces of the this space as list
	 * @return an ArrayList<Space> of all sub-spaces
	 */
	public ArrayList<Space> getSubSpaces() {
		ArrayList<Space> subSpaces = new ArrayList<Space>();
		addSubSpaces(subSpaces);
		return subSpaces;
	}
	
	/**
	 * Add all sub-spaces recursively to an ArrayList<Space>
	 * @param subSpaceList the in/out ArrayList<Space>
	 */
	private void addSubSpaces(ArrayList<Space> subSpaceList) {
		for(Space subSpace : children) {
			subSpace.addSubSpaces(subSpaceList);
			subSpaceList.add(subSpace);
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
	 * Enumeration use to standardize the basic type
	 * @author Cédric Gérard
	 * @since February 10, 2014
	 */
	public static enum TYPE{
		ROOT, 			 // The model tree main root
		USER_ROOT, 		 // The user root in the model
		HABITAT_CURRENT, // The habitat where the system is installed root
		HABITAT_OTHER,   // Root of other habitat
		SPATIAL_ROOT, 	 // The root of places
		DEVICE_ROOT,	 // The root of devices
		SERVICE_ROOT,	 // The root of services
		PROGRAM_ROOT,	 // The root of programs
		PLACE,			 // Space that hold a place
		USER,			 // Space that hold a user
		GROUP,			 // Space that hold a group
		CATEGORY,		 // Space that hold a category
		DEVICE,			 // Space that hold a device
		SERVICE,		 // Space that hold a service
		PROGRAM;		 // Space that hold a program
	}

}
