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
     * 
     * @param lastGraph the dependency graph to save
     * @return true if the graph has been correctly saved
     */
    Boolean addGraph(SpokObject lastGraph);
    
    /**
     * TODO: Methods to add in a near future
     * 
     * Dependencies getProgramDependencies(String pid)
     * Dependencies getDeviceDependencies(String id)
     */
    /**
     * 
     * @param pid the program id
     * @return 
     */
    Dependencies getProgramDependencies(String pid);
}
