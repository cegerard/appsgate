package appsgate.lig.context.device.properties.table.spec;

import appsgate.lig.ehmi.spec.GrammarDescription;

/**
 * Table that allow users to set properties to devices and services as they
 * want.
 *
 * @author Cedric Gérard
 * @version 1.0.0
 *
 */
public interface DevicePropertiesTableSpec {

    /**
     * Add a new name to the hash map
     *
     * @param objectId the targeted object
     * @param usrId which user the name stand for
     * @param newName the new name for this device
     */
    public void addName(String objectId, String usrId, String newName);

    /**
     * Delete an object name in the ash map
     *
     * @param objectId the object to change
     * @param usrId the user which the name stand for
     */
    public void deleteName(String objectId, String usrId);

    /**
     * Get the name give to a device by a specified user
     *
     * @param objectId the device
     * @param usrId the user who give the name
     * @return the user object name of the device
     */
    public String getName(String objectId, String usrId);

    /**
     * Add a grammar for a device type
     *
     * @param deviceId the id of device to add
     * @param deviceType the device type to add a grammar
     * @param grammar the grammar description to associate to a device type
     * @return true if the grammar is new, false if a previous grammar has been
     * replaced
     */
    public boolean addGrammarForDevice(String deviceId, String deviceType, GrammarDescription grammar);

    /**
     * Remove a grammar associate to a device type
     *
     * @param deviceType the device type from which the grammar has to be
     * removed
     * @return true if the grammar has been removed, false otherwise
     */
    public boolean removeGrammarForDeviceType(String deviceType);

    /**
     * Get the grammar associate to a device type
     *
     * @param deviceType the device type from which to get the grammar
     * @return the grammar as a JSONObject
     */
    public GrammarDescription getGrammarFromType(String deviceType);
    /**
     * Get the grammar associate to a device type
     *
     * @param deviceId the device id from which to get the grammar
     * @return the grammar as a JSONObject
     */
    public GrammarDescription getGrammarFromDevice(String deviceId);


    /**
     * set a type of a device
     * @param deviceId
     * @param type 
     */
    public void setType(String deviceId, String type);
    
    /**
     * 
     * @param deviceId
     * @return the type associated to the device id
     */
    public String getType(String deviceId);
    

}
