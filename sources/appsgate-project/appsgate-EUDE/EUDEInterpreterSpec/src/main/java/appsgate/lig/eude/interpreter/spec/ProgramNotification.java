package appsgate.lig.eude.interpreter.spec;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.ehmi.spec.messages.NotificationMsg;

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
    private final String name;

    /**
     * Constructor
     *
     * @param changes
     * @param programId
     * @param runningState
     * @param name
     * @param source
     */
    public ProgramNotification(String changes, String programId, String runningState,
            String name, JSONObject source) {
        super();
        this.changes = changes;
        this.programId = programId;
        this.runningState = runningState;
        this.source = source;
        if (name != null && !name.isEmpty()) {
            this.name = name;
        } else {
            this.name = getProgramNameFromSource();
        }

    }

    /**
     * @return the program id
     */
    public String getProgramId() {
        return this.programId;
    }

    /**
     * @return the program id
     */
    public String getRunningState() {
        return this.runningState;
    }

    /**
     * @return the program name
     */
    public String getProgramName() {
        return this.name;
    }

    /**
     *
     * @return the program name if it exists
     */
    public final String getProgramNameFromSource() {
        try {
            if (source != null) {
                return source.getString("name");
            }
        } catch (JSONException ex) {
        }
        return "";

    }

    @Override
    public String getSource() {
        return getProgramId();
    }

    @Override
    public String getVarName() {
        return changes;
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
