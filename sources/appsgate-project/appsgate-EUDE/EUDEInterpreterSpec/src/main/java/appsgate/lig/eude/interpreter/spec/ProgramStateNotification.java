package appsgate.lig.eude.interpreter.spec;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ApAm Program state notification class
 *
 * @author Cédric Gérard
 *
 */
public class ProgramStateNotification extends ProgramNotification {

    /**
     * Constructor
     *
     * @param programId
     * @param varName
     * @param value
     * @param programName
     */
    public ProgramStateNotification(String programId, String varName, String value, String programName) {
        super("runningState", programId, value, programName, null);
    }
    
    @Override
    public JSONObject JSONize() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("objectId", getProgramId());
            obj.put("varName", "runningState");
            obj.put("value", getRunningState());
        } catch (JSONException e) {
            // No exception will be raised since put value is always set
        }

        return obj;
    }

}
