package appsgate.lig.context.dependency.spec;

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
    JSONObject getGraph();
    
    /**
     * 
     * @param lastGraph the dependency graph to save
     * @return true if the graph has been correctly saved
     */
    Boolean addGraph(JSONObject lastGraph);
    
    /**
     * TODO: Methods to add in a near future
     * 
     * Dependencies getProgramDependencies(String pid)
     * Dependencies getDeviceDependencies(String id)
     */
}
