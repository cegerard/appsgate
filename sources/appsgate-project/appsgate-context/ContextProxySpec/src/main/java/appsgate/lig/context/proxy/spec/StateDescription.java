package appsgate.lig.context.proxy.spec;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class StateDescription {

    // Logger
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StateDescription.class.getName());

    /**
     * The name of the state, should be unique for a given type of device
     */
    private final String name;
    /**
     * the method to set an object to a certain state
     */
    private final JSONObject setter;

    /**
     * the event raised when a state start
     */
    private final JSONObject startEvent;
    /**
     * the event raised when a state ends
     */
    private final JSONObject endEvent;
    /**
     * the name of the state that can be called through a method
     */
    private final String stateName;
    /**
     * the value of the state which is retrieved from a method
     */
    private final String stateValue;

    /**
     * Constructor
     *
     * @param o
     * @throws org.json.JSONException
     */
    public StateDescription(JSONObject o) throws JSONException {
        if (o == null) {
            LOGGER.error("No object found");
            throw new JSONException("No object");
        }
        try {
            name = o.getString("name");
            setter = o.optJSONObject("setter");
            startEvent = o.optJSONObject("startEvent");
            endEvent = o.optJSONObject("endEvent");
            stateName = o.optString("stateName",null);
            stateValue = o.optString("stateValue", null);
        } catch (JSONException e) {
            LOGGER.error("Missing the name" + e);
            throw new JSONException("No name for this state");

        }
    }

    public String getName() {
        return name;
    }

    public JSONObject getSetter() {
        return setter;
    }

    public JSONObject getStartEvent() {
        return startEvent;
    }

    public JSONObject getEndEvent() {
        return endEvent;
    }

    public String getStateName() {
        return stateName;
    }

    public String getStateValue() {
        return stateValue;
    }

}
