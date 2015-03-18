package appsgate.lig.context.dependency.spec;

import appsgate.lig.ehmi.spec.SpokObject;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public interface DependencyManagerSpec {

    /**
     *
     * @return the current graph of dependencies
     */
    JSONObject getJSONGraph();
        
    /**
     * TODO: Methods to add in a near future
     * 
     * Dependencies getDeviceDependencies(String id)
     */
    /**
     * 
     * @param pid the program id
     * @return 
     */
    Dependencies getProgramDependencies(String pid);

    public void updateProgramStatus(String deviceId);

    public JSONObject buildGraph();

    public void updateDeviceStatus(String srcId, String varName, String value);

}
