package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class ErrorMessagesFactory {

    /**
     * @return a JSONObject corresponding to empty program error message
     */
    public static JSONObject getEmptyProgramMessage() {
        JSONObject error = new JSONObject();
        try {
            error.put("msg", "programs.error.empty");
        } catch (JSONException ex) {
            // never happens
        }
        return error;
    }

    /**
     * @param nodeException the node exception that throw the invalid state for the program
     * @return a JSONObject corresponding to the NodeException
     */
    public static JSONObject getMessageFromSpokNodeException(SpokNodeException nodeException) {
        JSONObject error = new JSONObject();
        try {
            error.put("msg", "programs.error.node");
            error.put("nodeid", nodeException.getNodeId());
        } catch (JSONException ex) {
            // never happens
        }
        return error;
        
    }

}
