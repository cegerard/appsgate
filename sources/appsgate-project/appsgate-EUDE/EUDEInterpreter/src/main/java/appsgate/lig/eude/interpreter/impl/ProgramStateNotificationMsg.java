package appsgate.lig.eude.interpreter.impl;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * ApAm Program state notification class
 *
 * @author Cédric Gérard
 *
 */
public class ProgramStateNotificationMsg implements NotificationMsg {

    /**
     * the id of the program associated to the notification message
     */
    private final String programId;
    /**
     * the var that has changed in the program
     */
    private final String varName;
    /**
     * the new value of the var
     */
    private final String value;

    /**
     * Constructor
     *
     * @param programId
     * @param varName
     * @param value
     */
    public ProgramStateNotificationMsg(String programId, String varName, String value) {
        super();
        this.programId = programId;
        this.varName = varName;
        this.value = value;
    }

    @Override
    public CoreObjectSpec getSource() {
        return null;
    }

    @Override
    public String getNewValue() {
        return value;
    }

    @Override
    public JSONObject JSONize() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("objectId", programId);
        obj.put("varName", varName);
        obj.put("value", value);

        return obj;
    }

}
