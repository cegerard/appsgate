package appsgate.lig.eude.interpreter.spec;

import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class ProgramCommandNotification extends ProgramNotification {

    /**
     *
     */
    private final String sid;
    /**
     *
     */
    private final String tid;
    /**
     *
     */
    private final String desc;
    
    public enum Type { READ, WRITE};
    
    private Type t;

    /**
     *
     * @param source
     * @param programId
     * @param programName
     * @param runningState
     * @param instructionId
     * @param sourceId
     * @param targetId
     * @param description
     * @param type
     */
    public ProgramCommandNotification(JSONObject source, String programId, String programName,
            String runningState, String instructionId, String sourceId, String targetId, String description, Type type) {
        super("", programId, runningState, programName, source, instructionId);
        this.sid = sourceId;
        this.tid = targetId;
        this.desc = description;
        this.t = type;
    }

    /**
     * @return the source id
     */
    public String getSourceId() {
        return sid;
    }

    /**
     * @return the target id
     */
    public String getTargetId() {
        return tid;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return desc;
    }
    
    /**
     * @return the string representing the type of notification (ie 'read' or 'write')
     */
    public String getType() {
        return this.t.toString().toLowerCase();
    }

}
