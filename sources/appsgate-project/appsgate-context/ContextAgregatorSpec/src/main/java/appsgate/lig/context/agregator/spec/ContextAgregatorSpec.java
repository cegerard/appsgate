package appsgate.lig.context.agregator.spec;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public interface ContextAgregatorSpec {

    /**
     * Return the devices of a list of type presents in the places
     *
     * @param typeList the list of types to look for (if null, return all
     * objects)
     * @param places the places where to find the objects (if null return all
     * places)
     * @return a list of objects contained in these places
     */
    public JSONArray getDevicesInSpaces(JSONArray typeList, JSONArray places);

    /**
     * Return a list of types descending from another types
     *
     * @param typeList the list of types to look for (if null, return all
     * subtypes)
     * @return an empty array if nothing is found or the array of types
     */
    public JSONArray getSubtypes(JSONArray typeList);

    /**
     *
     * @param type
     * @param stateName
     * @return
     */
    public JSONObject getEventsFromState(String type, String stateName);

    /**
     *
     * @param targetId
     * @return
     */
    public String getBrickType(String targetId);

    /**
     * 
     * @param value
     * @param stateName
     * @return 
     */
    public boolean isOfState(String value, String stateName);

    /**
     * 
     * @param stateName
     * @param type
     * @return the json description of the action to get the state
     */
    public JSONObject getSetter(String stateName, String type);

}
