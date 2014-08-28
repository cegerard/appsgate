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
    private final JSONObject activeNodes;
    private final JSONObject nodesCounter;

    /**
     *
     * @param pid
     * @param activeNodes
     * @param nodesCounter
     */
    public ProgramLineNotification(String pid, JSONObject activeNodes, JSONObject nodesCounter) {
        this.programId = pid;
        this.activeNodes = activeNodes;
        this.nodesCounter = nodesCounter;
    }

    @Override
    public String getSource() {
        return programId;
    }

    @Override
    public String getNewValue() {
        return null;
    }

    @Override
    public String getVarName() {
        return null;
    }


    @Override
    public JSONObject JSONize() {
        JSONObject content = new JSONObject();
        try {

            content.put("objectId", programId);
            content.put("activeNodes", activeNodes);
            content.put("nodesCounter", nodesCounter);

        } catch (JSONException ex) {
            //  No exception will be thrown since changes is not empty
        }
        return content;

    }

}
