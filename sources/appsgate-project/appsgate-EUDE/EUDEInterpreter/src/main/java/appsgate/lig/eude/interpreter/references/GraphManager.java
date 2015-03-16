package appsgate.lig.eude.interpreter.references;

import appsgate.lig.context.dependency.graph.Graph;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class GraphManager {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphManager.class);

    /**
     *
     */
    private final EUDEInterpreter interpreter;

    /**
     *
     */
    private Graph graph;

    /**
     * @param interpreter
     */
    public GraphManager(EUDEInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     *
     * @param needUpdateGraph
     * @return the graph in JSON format
     */
    public JSONObject getGraph(boolean needUpdateGraph) {
        if (needUpdateGraph) {
            updateGraph();
        }
        return graph.getJSONDescription();
    }

    /**
     * build the graph
     */
    public void buildGraph() {
        graph = new Graph();
        
        /* BUILD NODES FROM DEVICES */
        // Retrieving devices id
        JSONArray devices = getContext().getDevices();
        for (int i = 0; i < devices.length(); i++) {
            graph.addDevice(devices.optJSONObject(i));
        }

        /* BUILD NODES FROM PROGRAMS */
        for (String pid : interpreter.getListProgramIds(null)) {
            NodeProgram p = getProgramNode(pid);
            if (p != null) {
                graph.addProgram(pid, p.getProgramName(), p.getReferences(), p.getState().name());
            }
            // Link to the scheduler
            if (isPlanificationLink(pid)) {
                graph.addSchedulerEntity(pid);
            }
        }


        /* BUILD GHOSTS NODES */
        graph.buildGhosts();
        /* BUILD PLACE NODES */
        JSONArray places = getContext().getPlaces();
        for (int i = 0; i < places.length(); i++) {
            graph.addPlace(places.optJSONObject(i));

        }

        saveDependencyGraph();
    }

    /**
     * Method to build the planification link of a program
     *
     * @param pid : id of the program we want to build planification links
     */
    private boolean isPlanificationLink(String pid) {

        // Planification of the checkPrograms to avoid stucking if no scheduling service
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Object> task = new Callable<Object>() {
            @Override
            public Object call() {
                return getContext().checkProgramsScheduled();
            }
        };
        Future<Object> future = executor.submit(task);
        try {
            JSONArray programsScheduled = (JSONArray) future.get(2, TimeUnit.SECONDS);
            // Links program - scheduler
            return (programsScheduled != null && programsScheduled.toString().contains(pid));
        } catch (TimeoutException ex) {
            LOGGER.error("Time Out trying to reach scheduling service, aborting)");
            // handle the timeout
        } catch (InterruptedException e) {
            // handle the interrupts
        } catch (ExecutionException e) {
            // handle other exceptions
        } finally {
            future.cancel(true); // may or may not desire this
        }

        return false;
    }
   

    /**
     * Method to update the nodes graph with the latest values
     */
    private void updateGraph() {
        try {
            // Place names
            for (int j = 0; j < getContext().getPlaces().length(); j++) {
                JSONObject place = getContext().getPlaces().getJSONObject(j);
                graph.setPlaceName(place.optString("id"), place.optString("name"));
            }
            // Devices
            for (int j = 0; j < getContext().getDevices().length(); j++) {
                JSONObject currentDevice = getContext().getDevices().getJSONObject(j);
                switch (Integer.parseInt(currentDevice.getString("type"))) {
                    case 3: // Contact
                        graph.setDevice(currentDevice.optString("id"), currentDevice.optString(""), currentDevice.getString("contact"));
                        break;
                    case 4: // CardSwitch
                        graph.setDevice(currentDevice.optString("id"), currentDevice.optString(""), currentDevice.getString("inserted"));
                        break;
                    case 6: // Plug
                        graph.setDevice(currentDevice.optString("id"), currentDevice.optString(""), currentDevice.getString("plugState"));
                        break;
                    case 7: // Lamp
                        graph.setDevice(currentDevice.optString("id"), currentDevice.optString(""), currentDevice.getString("state"));
                        break;
                    default:
                        break;
                }
            }
            // Programs
            for (String s : interpreter.getListProgramIds(null)) {
                NodeProgram currentProgram = getProgramNode(s);
                graph.setProgramState(s, currentProgram.getState().name());

            }
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(GraphManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        saveDependencyGraph();
    }

    /**
     * @return the EHMI Proxy
     */
    private EHMIProxySpec getContext() {
        return this.interpreter.getContext();
    }

    /**
     *
     * @param pid
     * @return the program id corresponding to the pid
     */
    private NodeProgram getProgramNode(String pid) {
        return interpreter.getNodeProgram(pid);
    }

    private void saveDependencyGraph() {
        this.interpreter.saveDependencyGraph(graph);
    }

}
