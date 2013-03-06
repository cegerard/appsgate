package appsgate.lig.logical.object.spec;

import org.json.simple.JSONObject;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate specification to share the AbstractObjectSpec java interface.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 13, 2013
 *
 */
public interface AbstractObjectSpec {
	
	/**
	 * This generic method allow caller to get the object id,
	 * whatever the real object type.
	 * 
	 * @return the real object identifier
	 */
	public String getAbstractObjectId();
	
	/**
	 * This method allow caller to get the name set by the user,
	 * whatever the object type.
	 * 
	 * @return the current user name
	 */
	public String getUserObjectName();
	
	/**
	 * This method allow the caller to get the location id of
	 * the current place where it was installed.
	 * 
	 * @return the identifier of the corresponding place
	 */
	public int getLocationId();
	
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
	 */
	public JSONObject getDescription();
	
	/**
	 * Change the current user name
	 * 
	 * @param userName, the new user name
	 */
	public void setUserObjectName(String userName);
	
	/**
	 * Change the current location for this object
	 * 
	 * @param locationId, the new location identifier
	 */
	public void setLocationId(int locationId);
	
	/**
	 * Change the current picture for this device
	 * 
	 * @param pictureId, the new picture identifier
	 */
	public void setPictureId(String pictureId);
	
}
