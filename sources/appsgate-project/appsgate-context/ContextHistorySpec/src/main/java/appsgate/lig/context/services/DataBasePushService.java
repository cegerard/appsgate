package appsgate.lig.context.services;

import java.util.ArrayList;
import java.util.Map;

/**
 * Service offer to push data into the data base
 * 
 * @author Cédric Gérard
 * @since June 16, 2013
 * @version 1.0.0
 * 
 */
public interface DataBasePushService {
	/**
	 * Push the last state of an object name "name". The state is represent by a set of properties
	 * and an header represent the last addition of this object. This method specify the user that
	 * trigger the update.
	 * @param name the object to push
	 * @param userID the user who make the modification
	 * @param objectID the core object concern by the modification
	 * @param addedValue the added value
	 * @param properties the complete object state
	 * @return true if the save is done correctly false otherwise
	 */
	public boolean pushData_add(   String name, String userID, String objectID, String addedValue, ArrayList<Map.Entry<String, Object>> properties);
	
	/**
	 * Push the last state of an object name "name". The state is represent by a set of properties
	 * and an header represent the last removal of this object. This method specify the user that
	 * trigger the update.
	 * @param name the object top push
	 * @param userID the user who trigger the modification
	 * @param objectID the core object concern by the modification
	 * @param removedValue the removed value
	 * @param properties the complete object state
	 * @return true if the save is done correctly false otherwise
	 */
	public boolean pushData_remove(String name, String userID, String objectID, String removedValue, ArrayList<Map.Entry<String, Object>> properties);
	
	/**
	 * Push the last state of an object name "name". The state is represent by a set of properties
	 * and an header represent the last change of this object. This method specify the user that
	 * trigger the update.
	 * @param name the object top push
	 * @param userID the user who trigger the modification
	 * @param objectID the core object concern by the modification
	 * @param oldValue the old value
	 * @param newValue the new value
	 * @param properties the complete object state
	 * @return true if the save is done correctly false otherwise
	 */
	public boolean pushData_change(String name, String userID, String objectID, String oldValue, String newValue, ArrayList<Map.Entry<String, Object>> properties);
	
	/**
	 * Push the last state of an object name "name". The state is represent by a set of properties
	 * and an header represent the last addition of this object. 
	 * @param name the object to push
	 * @param objectID the core object concern by the modification
	 * @param addedValue the added value
	 * @param properties the complete object state
	 * @return true if the save is done correctly false otherwise
	 */
	public boolean pushData_add(   String name, String objectID, String addedValue, ArrayList<Map.Entry<String, Object>> properties);
	
	/**
	 * Push the last state of an object name "name". The state is represent by a set of properties
	 * and an header represent the last removal of this object.
	 * @param name the object top push
	 * @param objectID the core object concern by the modification
	 * @param removedValue the removed value
	 * @param properties the complete object state
	 * @return true if the save is done correctly false otherwise
	 */
	public boolean pushData_remove(String name, String objectID, String removedValue, ArrayList<Map.Entry<String, Object>> properties);
	
	/**
	 * Push the last state of an object name "name". The state is represent by a set of properties
	 * and an header represent the last change of this object.
	 * @param name the object top push
	 * @param objectID the core object concern by the modification
	 * @param oldValue the old value
	 * @param newValue the new value
	 * @param properties the complete object state
	 * @return true if the save is done correctly false otherwise
	 */
	public boolean pushData_change(String name, String objectID, String oldValue, String newValue, ArrayList<Map.Entry<String, Object>> properties);
	
	/**
	 * Enumeration class for operation to save.
	 * @author Cédric Gérard
	 *
	 */
	public enum OP {
			ADD,
			REMOVE,
			CHANGE; 
	}

}
