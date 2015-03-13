package appsgate.lig.context.dependency.impl;

import appsgate.lig.context.dependency.spec.Dependencies;
import appsgate.lig.context.dependency.spec.DependencyManagerSpec;
import appsgate.lig.context.dependency.spec.Graph;
import appsgate.lig.ehmi.spec.SpokObject;
import appsgate.lig.persistence.MongoDBConfiguration;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class DependencyManagerImpl implements DependencyManagerSpec {
    /**
     * The logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(DependencyManagerImpl.class);

    /**
     * The collection containing the links (wires) created, and deleted
     */
    private MongoDBConfiguration myConfiguration;

    /**
     * The last graph that has been saved
     */
    private JSONObject jsonGraph;
    /**
     * The last graph that has been saved
     */
    private Graph g;

    public DependencyManagerImpl() {
        this.jsonGraph = new JSONObject();
    }

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
    public JSONObject getJSONGraph() {
        return this.jsonGraph;
    }

    @Override
    public Boolean addGraph(SpokObject lastGraph) {
        this.jsonGraph = lastGraph.getJSONDescription();
        this.g = (Graph) lastGraph;
        return true;
    }

    @Override
    public Dependencies getProgramDependencies(String pid) {
        if (g == null) {
            LOGGER.error("Dependency graph has not been build yet");
            return null;
        }
        return g.getProgramDependencies(pid);
    }

}
