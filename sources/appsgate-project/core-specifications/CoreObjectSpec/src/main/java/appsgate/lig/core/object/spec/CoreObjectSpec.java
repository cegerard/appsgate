package appsgate.lig.core.object.spec;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate specification to share the AbstractObjectSpec java interface.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 13, 2013
 *
 */
public interface CoreObjectSpec {
	
	/**
	 * Constant, define a DataBase name that can be used for persistence of object states
	 */
	public static final String DBNAME_DEFAULT = "AppsGate-CoreObject";
	
	/**
	 * This generic method allow caller to get the object id,
	 * whatever the real object type.
	 * 
	 * @return the real object identifier
	 */
	public String getAbstractObjectId();

	
	/**
	 * This method allow the caller to get a user friendly 
	 * description of the object type.
	 * 
	 * @return the user friendly type
	 */
	public String getUserType(); 
        
	/**
	 * This method allow the caller to get the current
	 * object status.
	 * 
	 * Status values:
	 * 
	 * 0 = Off line or out of range
	 * 1 = In validation mode (test range for sensor for instance)
	 * 2 = In line or connected, the core Object is available and usable
	 * @seee CORE_STATUS for predefined statuses
	 * @return an integer that represent the current status
	 */
	public int getObjectStatus();
	
	/**
	 * Get the JSON of an object
	 * @return the description as an JSONObject
     * @throws org.json.JSONException
	 */
	public JSONObject getDescription() throws JSONException;
	
	/**
	 * Get the type of the core object it can
	 * be a service or device of the Core World, a Communication Tecnology Adapter, or an extended-service
	 * @return the core type value
	 */
	public CORE_TYPE getCoreType();
	
	/**
	 * Core type value enumeration type
	 * @author Cédric Gérard
	 * @since 21 February, 2014
	 */

        /**
         * 
         * @return 
         */
        public JSONObject getBehaviorDescription();
        
        public enum CORE_TYPE{
		SERVICE("Service"),
		DEVICE("Device"),
		SIMULATED_DEVICE("SimulatedDevice"),
		SIMULATED_SERVICE("SimulatedService"),
		ADAPTER("Adapter"),
		EXTENDED("Extended");
		
    	private String name;
    	private CORE_TYPE(String name) {
			this.name = name;
		}
    	public String getName() {
    		return name;
    	}
	}
        
        public String KEY_STATUS = "status";
        
        /**
         * Constants to get predefined status
         * Developers may specify their own status (for instance to represent a pairing mode or an error status)
         * But if a device if available, it can be used getDescription() and methods 
         * @author thibaud
         */
        public enum CORE_STATUS{
		UNAVAILABLE(0),
		AVAILABLE(2);
		
    	private int status;
    	private CORE_STATUS(int status) {
			this.status = status;
		}
    	
    	public int getStatus() {
    		return status;
    	}
    	
    	/**
    	 * Heper methods
    	 * @param status
    	 * @return
    	 */
    	public static boolean isAvailable(int status) {
    		if (status == AVAILABLE.getStatus()) {
    			return true;
    		} else {
    			return false;
    		}
    	}
    	public static boolean isAvailable(String status) {
    		if (String.valueOf(AVAILABLE.getStatus()).equals(status)) {
    			return true;
    		} else {
    			return false;
    		}
    	}
	}
        
	
}
