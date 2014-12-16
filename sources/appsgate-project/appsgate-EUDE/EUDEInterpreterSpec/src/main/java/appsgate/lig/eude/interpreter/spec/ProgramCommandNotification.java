package appsgate.lig.eude.interpreter.spec;

import org.json.JSONArray;

/**
 *
 * @author jr
 */
public class ProgramCommandNotification extends ProgramTraceNotification {
    /**
     *
     * @param source
     * @param nodeId
     * @param targetId
     * @param description
     * @param params
     */
    public ProgramCommandNotification(ProgramDesc source, 
            String nodeId, String targetId, String description, JSONArray params) {
        super(source, nodeId, source.getId(), targetId, description, Type.WRITE);
        setParams(params);
    }


    
}
