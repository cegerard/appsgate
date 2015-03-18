package appsgate.lig.context.dependency.impl;

import appsGate.lig.manager.client.communication.ClientCommunicationManager;
import appsgate.lig.context.dependency.spec.Dependencies;
import appsgate.lig.context.dependency.spec.DependencyManagerSpec;
import appsgate.lig.context.dependency.graph.Graph;
import appsgate.lig.context.dependency.graph.ProgramGraph;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
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

    //
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
    public Dependencies getProgramDependencies(String pid) {
        if (g == null) {
            LOGGER.error("Dependency graph has not been build yet");
            return null;
        }
        return g.getDependencies(pid);
    }

    @Override
    public void updateDeviceStatus(String srcId, String varName, String value) {
        addGraph(graphManager.updateDeviceStatus(srcId, varName, value));
    }

    @Override
    public void updateProgramStatus(String deviceId) {
        addGraph(graphManager.updateProgramStatus(deviceId));
    }

    @Override
    public JSONObject buildGraph() {
        addGraph(graphManager.buildGraph());
        return this.jsonGraph;
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

    /**
     * add the new graph to the dependency history system (future work)
     * @param lastGraph
     * @return 
     */
    private Boolean addGraph(Graph lastGraph) {
        if (lastGraph != null) {
            this.jsonGraph = lastGraph.getJSONDescription();
            this.g = lastGraph;
            sendGraph(jsonGraph);
            return true;
        }
        return false;
    }

    /**
     * send the graph to the client
     * @param graph 
     */
    private void sendGraph(JSONObject graph) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("value", graph.toString());
            msg.put("objectId", "EHMI");
            msg.put("callId", "loadGraph");
        } catch (JSONException ex) {
        }
        ehmiProxy.sendFromConnection(ClientCommunicationManager.DEFAULT_SERVER_NAME, msg.toString());
    }
}
