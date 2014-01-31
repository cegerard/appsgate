package appsgate.lig.router.spec;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Specification of the router that offer services about
 * core objects
 * 
 * @author Cedric Gérard
 * @version 1.0.0
 *
 */
public interface RouterApAMSpec {
	
	/**
	 * Execute a command from the outside to a specific device
	 * @param objectId the target object
	 * @param methodName the method to call
	 * @param args argument of the method
	 * @param paramType type of those arguments
	 * @return a Runnable object that can be execute everywhere.
	 */
	@SuppressWarnings("rawtypes")
	public GenericCommand executeCommand(String objectId, String methodName, ArrayList<Object> args, ArrayList<Class> paramType);

	/**
	 * Execute command from outside to a specific device
	 * @param objectId the targeted object
	 * @param methodName the method to call
	 * @param args arguments and their type as an JSONArray
	 * @return a Runnable object that can be execute everywhere.
	 */
	public GenericCommand executeCommand(String objectId, String methodName, JSONArray args);
	
	/**
	 * Get all the devices description as JSONArray
	 */
	public JSONArray getDevices();
	
	/**
	 * Get the device description
	 * @param objectId the object identifier
	 * @return the object information as a JSONObject
	 */
	public JSONObject getDevice(String objectId);
}
