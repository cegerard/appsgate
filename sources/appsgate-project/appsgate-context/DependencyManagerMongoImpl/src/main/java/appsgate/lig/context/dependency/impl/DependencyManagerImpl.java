package appsgate.lig.context.dependency.impl;

import appsgate.lig.manager.client.communication.ClientCommunicationManager;
import appsgate.lig.context.dependency.spec.Dependencies;
import appsgate.lig.context.dependency.spec.DependencyManagerSpec;
import appsgate.lig.context.dependency.graph.Graph;
import appsgate.lig.context.dependency.graph.ProgramGraph;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.SpokObject;
import appsgate.lig.persistence.MongoDBConfiguration;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
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
    private MongoDBConfiguration conf;

    /**
     * Reference to the ApAM context proxy. Used to be notified when something
     * happen.
     */
    private EHMIProxySpec ehmiProxy;

    /**
     * The last graph that has been saved
     */
    private Graph graph;

    //
    private final GraphManager graphManager;

    public DependencyManagerImpl() {
        this.graphManager = new GraphManager(this);
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        Graph g;
        g = getDependencyAtTime(ehmiProxy.getCurrentTimeInMillis());
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
    }

    @Override
    public SpokObject getGraph() {
        return this.graph;
    }

    @Override
    public Dependencies getDependencies(String pid) {
        if (graph == null) {
            LOGGER.error("Dependency graph has not been build yet");
            return null;
        }
        return graph.getDependencies(pid);
    }

    @Override
    public void updateDeviceStatus(String srcId, String varName, String value) {
        if (graphManager.updateDeviceStatus(srcId, varName, value)) {
            addGraph(graphManager.getGraph());
        }
    }

    @Override
    public void updateProgramStatus(String deviceId, String status) {
        if (graphManager.updateProgramStatus(deviceId, status)) {
            addGraph(graphManager.getGraph());
        }
    }

    @Override
    public void buildGraph() {
        addGraph(graphManager.buildGraph());
    }

    public EHMIProxySpec getContext() {
        return ehmiProxy;
    }

    public Iterable<String> getListProgramIds() {
        JSONArray programs = ehmiProxy.getPrograms();
        ArrayList<String> ret = new ArrayList<>();
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
     *
     * @param lastGraph
     * @return
     */
    private Boolean addGraph(Graph lastGraph) {
        if (lastGraph != null) {
            this.graph = lastGraph;
            sendGraph(lastGraph.getJSONDescription());
            add(ehmiProxy.getCurrentTimeInMillis(), lastGraph);
            return true;
        }
        return false;
    }

    private void sendGraph(JSONObject graph) {
        sendGraph(graph, "loadGraph");
    }
    /**
     * send the graph to the client
     *
     * @param graph
     */
    private void sendGraph(JSONObject graph, String callId) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("value", graph.toString());
            msg.put("objectId", "EHMI");
            msg.put("callId", callId);
        } catch (JSONException ex) {
        }
        ehmiProxy.sendFromConnection(ClientCommunicationManager.DEFAULT_SERVER_NAME, msg.toString());
    }

    /**
     * The db name
     */
    private static final String DBNAME = "TraceHistory";
    /**
     * The collection containing symbol table
     */
    private static final String DEP = "dependencies";

    /**
     *
     * @param timestamp
     * @param o
     * @return
     */
    private boolean add(Long timestamp, Graph graph) {
        if (isValidConf()) {
            try {
                DBCollection context = conf.getDB(DBNAME).getCollection(DEP);

                BasicDBObject newVal = new BasicDBObject("time", timestamp)
                        .append("graph", graph.getJSONDescription().toString())
                        .append("dependencies", graph.getJSONDependencies().toString());

                context.insert(newVal);
                return true;

            } catch (MongoException e) {
                LOGGER.error("A Database Exception has been raised: " + e);
            }
        }
        return false;
    }

    /**
     *
     * @param timestamp
     * @return
     */
    public Graph getDependencyAtTime(Long timestamp) {
        if (isValidConf()) {
            DBCollection collection = conf.getDB(DBNAME).getCollection(DEP);
            DBCursor cursor = collection
                    .find(BasicDBObjectBuilder.start().add("time", BasicDBObjectBuilder.start("$lte", timestamp).get()).get())
                    .sort(new BasicDBObject("time", -1)).limit(1);
            try {
                if (!cursor.hasNext()) {
                    LOGGER.warn("No logs for before the start of window");
                    return null;
                }
            } catch (Exception e) {
                LOGGER.error("Unable to parse cursor" + e.getMessage());
                return null;
            }
            DBObject obj = cursor.next();
            JSONArray dependencies;
            JSONObject jsonGraph;
            try {
                dependencies = new JSONArray(obj.get("dependencies").toString());
                jsonGraph = new JSONObject(obj.get("graph").toString());
            } catch (JSONException ex) {
                return null;
            }
            return new Graph(jsonGraph, dependencies, timestamp.toString());
        }
        return null;
    }

    /**
     * Method to check the database connection
     *
     * @return true if the configuration is OK
     */
    private boolean isValidConf() {
        if (conf == null) {
            LOGGER.error("Unable to init DBManager, no MongoDB configuration");
            return false;
        }
        if (!conf.isValid()) {
            LOGGER.error("Unable to init DBManager, configuration not valid");
            return false;
        }
        return true;
    }

    @Override
    public Dependencies getDependenciesAt(String id, Long timestamp) {
        return getDependencyAtTime(timestamp).getDependencies(id);
    }

    @Override
    public void sendGraphAt(Long timestamp) {
        sendGraph(getDependencyAtTime(timestamp).getJSONDescription(), "loadGraphAt");
    }

}
