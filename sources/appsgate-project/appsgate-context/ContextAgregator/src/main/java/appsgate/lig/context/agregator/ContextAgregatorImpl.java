package appsgate.lig.context.agregator;

import appsgate.lig.context.agregator.spec.ContextAgregatorSpec;
import appsgate.lig.context.agregator.spec.StateDescription;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextAgregatorImpl implements ContextAgregatorSpec {

    /**
     * Static class member uses to log what happened in each instances
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ContextAgregatorImpl.class);

    /**
     *
     */
    protected Library lib;

    /**
     * Constructor
     *
     * @throws org.json.JSONException
     */
    public ContextAgregatorImpl() throws JSONException {
        lib = new Library();

    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() throws JSONException {
        LOGGER.trace("The context agregator ApAM component has been initialized");
        lib.addDesc(new JSONObject("{\n"
                + "    \"friendlyName\": \"lamp\",\n"
                + "    \"typename\":\"lamp\",\n"
                + "    \"properties\": [\n"
                + "    ],\n"
                + "    \"states\": [\n"
                + "        {\n"
                + "            \"name\": \"isOn\",\n"
                + "            \"stateName\": \"getCurrentState\",\n"
                + "            \"stateValue\": \"true\",\n"
                + "            \"setter\": {\n"
                + "                \"type\": \"NodeAction\",\n"
                + "                \"methodName\": \"On\"\n"
                + "            },\n"
                + "            \"endEvent\": {\n"
                + "                \"name\" : \"value\",\n"
                + "                \"value\" : \"false\"\n"
                + "            },\n"
                + "            \"startEvent\": {\n"
                + "                \"name\" : \"value\",\n"
                + "                \"value\" : \"true\"\n"
                + "            }\n"
                + "\n"
                + "        }"
                + "    ]\n"
                + "}"));
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
    public String getBrickType(String targetId) {
        return "lamp";
    }

    @Override
    public StateDescription getEventsFromState(String type, String stateName) {
        try {
            return new StateDescription(lib.getStateForType(type, stateName));
        } catch (JSONException ex) {
            LOGGER.error("unable to find events for the given type [{}/{}]", type, stateName);
            return null;
        }
    }

}
