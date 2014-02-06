package appsgate.lig.context.agregator;

import appsgate.lig.context.agregator.spec.ContextAgregatorSpec;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextAgregatorImpl implements ContextAgregatorSpec {

    /**
     * Static class member uses to log what happened in each instances
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ContextAgregatorImpl.class);

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        LOGGER.trace("The context agregator ApAM component has been initialized");
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        LOGGER.trace("The context agregator ApAM component has been stopped");
    }

    @Override
    public JSONArray getDevicesInSpaces(JSONArray typeList, JSONArray places) {
        return new JSONArray();
    }

    @Override
    public JSONArray getSubtypes(JSONArray typeList) {
        return new JSONArray();
    }

    @Override
    public JSONArray getEventsFromState(String brickType, String stateName, String value) {
        return new JSONArray();
    }

    @Override
    public String getBrickType(String targetId) {
        return null;
    }

}
