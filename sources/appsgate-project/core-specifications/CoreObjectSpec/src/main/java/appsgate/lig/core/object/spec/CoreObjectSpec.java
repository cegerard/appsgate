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
	 * 2 = In line or connected
	 * 
	 * @return an integer that represent the current status
	 */
	public int getObjectStatus();
	
	/**
	 * This method allow the caller to get the current picture
	 * of this object.
	 * 
	 * @return the id of the corresponding picture
	 */
	public String getPictureId();
	
	/**
	 * Get the JSON of an object
	 * @return the description as an JSONObject
     * @throws org.json.JSONException
	 */
	public JSONObject getDescription() throws JSONException;
	
	/**
	 * Change the current picture for this device
	 * 
	 * @param pictureId the new picture identifier
	 */
	public void setPictureId(String pictureId);
	
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
		SERVICE,
		DEVICE,
		SIMULATED_DEVICE,
		SIMULATED_SERVICE,
		ADAPTER,
		EXTENDED;
	}
        
	
}
