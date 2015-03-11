package appsgate.lig.eude.interpreter.spec;

import org.json.JSONArray;

/**
 *
 * @author jr
 */
public  class ProgramTraceNotification extends ProgramNotification{
    /**
     * the source id
     */
    private final String sid;
    /**
     * the target id
     */
    private final String tid;
    /**
     * the description of the event
     */
    private final String desc;

    /**
     * 
     */
    public enum Type { READ, WRITE};
    
    protected final Type t;
    
    /**
     * 
     */
    private JSONArray params = null;

    
    
    private String result = null;
    /**
     * 
     * @param d the program description
     * @param nodeId 
     * @param sourceId 
     * @param targetId 
     * @param desc 
     * @param type 
     */
    public ProgramTraceNotification(ProgramDesc d, String nodeId, String sourceId, String targetId, String desc, Type type) {
        super("", d.getId(), d.getState().toString(), d.getProgramName(), d.getJSONDescription(), nodeId);
        this.sid = sourceId;
        this.tid = targetId;
        this.desc = desc;
        this.t = type;
    }

    
    /**
     * @param stringResult 
     */
    protected final void setResult(String stringResult) {
        this.result = stringResult;
    }
    
    /**
     * @return the result if it exists
     */
    public final String getResult() {
        return this.result;
    }
    /**
     * 
     * @param paramArray 
     */
    protected final void setParams(JSONArray paramArray) {
        this.params = paramArray;
    }
   
    /**
     * 
     * @return 
     */
    public JSONArray getParams() {
        return this.params;
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

    public String getDeviceId() {
        return this.getTargetId();
    }
    
}
