package appsgate.lig.manager.location.impl;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import appsgate.lig.logical.object.spec.AbstractObjectSpec;

/**
 * This class is an abstract representation of
 * a place
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 26, 2013
 *
 */
public class Location {

	/**
	 * The place unique identifier
	 */
	private String id;
	
	/**
	 * The place name
	 */
	private String name;
	
	/**
	 * The abstract device list
	 */
	private ArrayList<AbstractObjectSpec> abstractsObjects;
	
	public Location(String id, String name) {
		super();
		this.id = id;
		this.name = name;
		abstractsObjects = new ArrayList<AbstractObjectSpec>();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Add on object to this place
	 * @param obj, the new abstract object
	 */
	public void addObject(AbstractObjectSpec obj) {
		if(abstractsObjects.add(obj)){
			obj.setLocationId(Integer.valueOf(id));
		}
	}
	
	/**
	 * Remove this object from this place
	 * 
	 * @param obj, the object to remove
	 */
	public void removeObject(AbstractObjectSpec obj) {
		if(abstractsObjects.remove(obj)) {
			obj.setLocationId(-1);
		}
	}

	/**
	 * Remove all the objects from this place 
	 */
	public void removeAll() {
		Iterator<AbstractObjectSpec> it = abstractsObjects.iterator();
		AbstractObjectSpec abObj;
		while(it.hasNext()) {
			abObj = it.next();
			abObj.setLocationId(-1);
		}
	}

	/**
	 * Get the JSONdescription of this location
	 * @return the JSONObject
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getDescription() {
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("name", name);
		
		JSONArray objects = new JSONArray();
		AbstractObjectSpec abObj;
		Iterator<AbstractObjectSpec> it = abstractsObjects.iterator();
		
		while(it.hasNext()) {
			abObj = it.next();
			objects.add(abObj.getAbstractObjectId());
		}
		
		obj.put("devices", objects);
		
		return obj;
	}

}
