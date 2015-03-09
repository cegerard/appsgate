package appsgate.lig.context.dependency.impl;

import appsgate.lig.context.dependency.spec.Dependencies;
import appsgate.lig.context.dependency.spec.DependencyManagerSpec;
import appsgate.lig.persistence.MongoDBConfiguration;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class DependencyManagerImpl implements DependencyManagerSpec {

    /**
     * The collection containing the links (wires) created, and deleted
     */
    private MongoDBConfiguration myConfiguration;

    /**
     * The last graph that has been saved
     */
    private JSONObject graph = new JSONObject();

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
    }

    @Override
    public JSONObject getGraph() {
        return this.graph;
    }

    @Override
    public Boolean addGraph(JSONObject lastGraph) {
        this.graph = lastGraph;
        return true;
    }

    @Override
    public Dependencies getProgramDependencies(String pid) {
        return new Dependencies();
    }
}
