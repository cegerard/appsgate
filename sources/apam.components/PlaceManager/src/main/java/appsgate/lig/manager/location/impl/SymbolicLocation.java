package appsgate.lig.manager.location.impl;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SymbolicLocation {
	
	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(SymbolicLocation.class);

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
	
	public SymbolicLocation(String id, String name) {
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
	
	public boolean isHere(AbstractObjectSpec obj) {
		return abstractsObjects.contains(obj);
	}
	
	/**
	 * Add on object to this place
	 * @param obj the new abstract object
	 */
	public void addObject(AbstractObjectSpec obj) {
		if(abstractsObjects.add(obj)){
			logger.debug("Core device "+obj.getAbstractObjectId()+ " added to "+name+ "/ "+id);
		} else {
			logger.error("Error adding "+obj.getAbstractObjectId()+" to "+id);
		}
	}
	
	/**
	 * Remove this object from this place
	 * 
	 * @param obj the object to remove
	 */
	public void removeObject(AbstractObjectSpec obj) {
		if(abstractsObjects.remove(obj)) {
			logger.debug("Core device "+obj.getAbstractObjectId()+ " remove from "+name+ "/ "+id);
		} else {
			logger.error("Error removing "+obj.getAbstractObjectId()+" to "+id);
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
			//TODO notify that this object move to -1
		}
		abstractsObjects.clear();
	}

	/**
	 * Get the JSONdescription of this location
	 * @return the JSONObject
	 */
	public JSONObject getDescription() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", id);
			obj.put("name", name);
		
			JSONArray objects = new JSONArray();
			AbstractObjectSpec abObj;
			Iterator<AbstractObjectSpec> it = abstractsObjects.iterator();
		
			while(it.hasNext()) {
				abObj = it.next();
				objects.put(abObj.getAbstractObjectId());
			}
		
			obj.put("devices", objects);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return obj;
	}

}
