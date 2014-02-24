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
    private final String userSource;

    /**
     * Constructor
     *
     * @param changes
     * @param programId
     * @param runningState
     * @param source
     * @param userSource
     */
    public ProgramNotification(String changes, String programId, String runningState,
            JSONObject source, String userSource) {
        super();
        this.changes = changes;
        this.programId = programId;
        this.runningState = runningState;
        this.source = source;
        this.userSource = userSource;
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
    public JSONObject JSONize() {
        JSONObject notif = new JSONObject();
        JSONObject content = new JSONObject();
        try {

            content.put("id", programId);
            content.put("runningState", runningState);
            content.put("source", source);
            content.put("userSource", userSource);

            if (changes.isEmpty()) {
                notif.put("", content);
            } else {
                notif.put(changes, content);
            }

        } catch (JSONException ex) {
            //  No exception will be thrown since changes is not empty
        }
        return notif;

    }

}
