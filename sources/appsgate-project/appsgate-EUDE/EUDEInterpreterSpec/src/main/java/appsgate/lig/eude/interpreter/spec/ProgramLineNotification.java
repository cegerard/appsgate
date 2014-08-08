package appsgate.lig.eude.interpreter.spec;

import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class ProgramLineNotification implements NotificationMsg {

    private final String programId;
    private final String nodeId;

    /**
     *
     * @param pid
     * @param nodeId
     */
    public ProgramLineNotification(String pid, String nodeId) {
        this.programId = pid;
        this.nodeId = nodeId;
    }

    @Override
    public String getSource() {
        return programId;
    }

    @Override
    public String getNewValue() {
        return nodeId;
    }

    @Override
    public String getVarName() {
        return "nodeId";
    }

    @Override
    public JSONObject JSONize() {
        JSONObject content = new JSONObject();
        try {

            content.put("id", programId);
            content.put("nodeId", nodeId);

        } catch (JSONException ex) {
            //  No exception will be thrown since changes is not empty
        }
        return content;

    }

}
