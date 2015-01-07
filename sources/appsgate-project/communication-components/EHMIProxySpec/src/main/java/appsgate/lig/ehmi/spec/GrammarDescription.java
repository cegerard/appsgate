package appsgate.lig.ehmi.spec;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class GrammarDescription {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GrammarDescription.class);

    private final JSONObject json;

    private final HashMap<String, ArrayList<String>> commandsState = new HashMap<String, ArrayList<String>>();

    /**
     * public constructor
     *
     * @param o
     */
    public GrammarDescription(JSONObject o) {
        if (o == null) {
            this.json = new JSONObject();
            return;
        }
        this.json = o;
    }

    /**
     *
     * @return the type, empty string if no typename is defined
     */
    public String getType() {
        try {
            if (this.json.has("typename")) {
                return this.json.getString("typename");
            }
        } catch (JSONException ex) {
        }
        return "";
    }

    /**
     *
     * @return friendly name, "unknown" if no friendly name has been defined
     */
    public String getFriendlyName() {
        try {
            if (this.json.has("friendlyName")) {
                return this.json.getString("friendlyName");
            }
        } catch (JSONException ex) {
        }
        return "unknown";
    }

    /**
     * @return JSON object for compatibility
     */
    public JSONObject getJSON() {
        return this.json;
    }

    /**
     *
     * @param stateName
     * @return
     */
    public JSONObject getStateDescription(String stateName) {
        try {
            JSONArray grammarStates = this.json.getJSONArray("states");
            for (int i = 0; i < grammarStates.length(); i++) {
                if (grammarStates.getJSONObject(i).getString("name").equalsIgnoreCase(stateName)) {
                    return grammarStates.getJSONObject(i);
                }
            }
        } catch (JSONException ex) {
        }
        return null;
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getProperties() {
        ArrayList<String> ret = new ArrayList<String>();
        if (json.has("properties")) {
            try {
                JSONArray array = json.getJSONArray("properties");
                for (int i = 0; i < array.length(); i++) {
                    ret.add(array.getString(i));
                }
            } catch (JSONException ex) {

            }
        }
        return ret;
    }

    public String getValueVarName(String what) {
        String ret = what;
        try {
            JSONArray a = json.getJSONArray("traceDesc");
            int i = 0;
            for (String s : getProperties()) {
                if (s.equalsIgnoreCase(what)) {
                    return a.getString(i);
                }
                i++;
            }
        } catch (JSONException ex) {
        }
        return ret;
    }

    public boolean generateTrace() {
        return this.json.has("traceDesc");
    }

    /**
     * Return the trace message corresponding to the cmd
     *
     * @param cmd
     * @return
     */
    public String getTraceMessageFromCommand(String cmd) {
        return "decorations." + this.getType() + "." + cmd;
    }

    /**
     * Return the context corresponding to the cmd and the params
     *
     * @param cmd
     * @param params
     * @return
     */
    public JSONObject getContextFromParams(String cmd, JSONArray params) {
        int size = 0;
        if (params != null) {
            size = params.length();
        }
        ArrayList<String> arguments = getArgumentsFromCommand(cmd, size);
        if (params == null) {
            return null;
        }
        JSONObject ret = new JSONObject();
        for (int i = 0 ; i < arguments.size(); i++) {
            try {
                ret.put(arguments.get(i), params.getJSONObject(i).getString("value"));
            } catch (JSONException ex) {
                LOGGER.error("GetContextFromParams, array invalid: {}", params.toString());
                return null;
            }
        }
        return ret;
    }

    /**
     * Return the context corresponding to the cmd and the params
     *
     * @param cmd
     * @param params
     * @return
     */
    public JSONObject getContextFromParams(String cmd, ArrayList<Object> params) {
        int size = 0;
        if (params != null) {
            size = params.size();
        }
        ArrayList<String> arguments = getArgumentsFromCommand(cmd, size);
        if (params == null) {
            return null;
        }
        JSONObject ret = new JSONObject();
        for (int i = 0 ; i < arguments.size(); i++) {
                Object obj = params.get(i);
                String v = obj.toString();
            try {
                ret.put(arguments.get(i), v);
            } catch (JSONException ex) {
                // Could not happen
            }
        }
        return ret;
    }

    /**
     *
     * @param cmd the command name
     * @return the list of arguments of the given method, empty array if no
     * method is found
     */
    private ArrayList<String> getArgumentsFromCommand(String cmd, int size) {
        ArrayList<String> params = new ArrayList<String>();
        if (!this.json.has("commands")) {
            LOGGER.error("Command not found ({}) for type {}", cmd, this.getType());
            return params;
        }
        if (!this.json.optJSONObject("commands").has(cmd)) {
            LOGGER.error("Command not found ({}) for type {}", cmd, this.getType());
            return params;
        }
        JSONObject command = this.json.optJSONObject("commands").optJSONObject(cmd);

        if (command == null) {
            LOGGER.error("Command not found ({}) for type {}, grammar not well formed", cmd, this.getType());
            return params;
        }

        JSONArray array = command.optJSONArray("params");
        if (array == null) {
            LOGGER.debug("No parameters for this command ({}) of type {}", cmd, this.getType());
            return params;
        }
        if (array.length() != size) {
            LOGGER.error("Method ({} on {}) passed with the wrong parameters number {} waited, {} received", command, this.getType(), array.length(), size);
            return params;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject p = array.optJSONObject(i);
            if (p == null) {
                LOGGER.error("File not well formed for type {} : {}", this.getType(), params.toString());
                return params;
            }
            params.add(p.optString("name"));
        }

        return params;

    }

}
