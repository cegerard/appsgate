package appsgate.lig.eude.interpreter.impl;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

public class ProgramNotification implements NotificationMsg {

    /**
     * The change that trigger notification
     */
    private final String changes;

    /**
     * The location identifier
     */
    private final String programId;

    /**
     * The location name
     */
    private final String runningState;

    /**
     * The specified type of this notification
     */
    private final JSONObject source;

    /**
     * Field for user
     */
    private final String userInputSource;

    /**
     * Constructor
     * 
     * @param changes
     * @param programId
     * @param runningState
     * @param source
     * @param userInputSource 
     */
    public ProgramNotification(String changes, String programId, String runningState,
            JSONObject source, String userInputSource) {
        super();
        this.changes = changes;
        this.programId = programId;
        this.runningState = runningState;
        this.source = source;
        this.userInputSource = userInputSource;
    }

    @Override
    public CoreObjectSpec getSource() {
        return null;
    }

    @Override
    public String getNewValue() {
        return changes + " " + programId;
    }

    @Override
    public JSONObject JSONize() throws JSONException {
        JSONObject notif = new JSONObject();
        JSONObject content = new JSONObject();

        content.put("id", programId);
        content.put("runningState", runningState);
        content.put("source", source);
        content.put("userInputSource", userInputSource);

        notif.put(changes, content);

        return notif;
    }

}
