package appsgate.lig.context.dependency.impl;

import appsgate.lig.context.dependency.spec.Dependencies;
import appsgate.lig.context.dependency.spec.DependencyManagerSpec;
import appsgate.lig.context.dependency.graph.Graph;
import appsgate.lig.context.dependency.graph.ProgramGraph;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.SpokObject;
import appsgate.lig.persistence.MongoDBConfiguration;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
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
     * Reference to the ApAM context proxy. Used to be notified when something
     * happen.
     */
    private EHMIProxySpec ehmiProxy;

    /**
     * The last graph that has been saved
     */
    private Graph g;

    private final GraphManager graphManager;

    public DependencyManagerImpl() {
        this.jsonGraph = new JSONObject();
        this.graphManager = new GraphManager(this);
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
        if (lastGraph != null) {
            this.jsonGraph = lastGraph.getJSONDescription();
            this.g = (Graph) lastGraph;
            return true;
        }
        return false;
    }

    @Override
    public Dependencies getProgramDependencies(String pid) {
        if (g == null) {
            LOGGER.error("Dependency graph has not been build yet");
            return null;
        }
        return g.getDependencies(pid);
    }

    @Override
    public void updateDeviceStatus(String srcId, String varName, String value) {
        graphManager.updateGraph();
    }

    @Override
    public void updateProgramStatus(String deviceId) {
        graphManager.updateGraph();
    }

    @Override
    public JSONObject buildGraph() {
        graphManager.buildGraph();
        return graphManager.getGraph(false);
    }

    public EHMIProxySpec getContext() {
        return ehmiProxy;
    }

    public Iterable<String> getListProgramIds() {
        JSONArray programs = ehmiProxy.getPrograms();
        ArrayList<String> ret = new ArrayList<String>();
        for (int i = 0; i < programs.length(); i++) {
            try {
                ret.add(programs.getJSONObject(i).getString("id"));
            } catch (JSONException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        return ret;
    }

    public ProgramGraph getNodeProgram(String pid) {
        return (ProgramGraph) ehmiProxy.getProgram(pid);
    }

}
