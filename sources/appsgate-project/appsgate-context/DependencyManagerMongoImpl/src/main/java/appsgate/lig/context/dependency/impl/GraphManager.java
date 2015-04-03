package appsgate.lig.context.dependency.impl;

import appsgate.lig.context.dependency.graph.Graph;
import appsgate.lig.context.dependency.graph.ProgramGraph;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import java.util.concurrent.ExecutionException;
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
    private Graph graph;

    private final DependencyManagerImpl dependency;

    /**
     * @param interpreter
     */
    public GraphManager(DependencyManagerImpl i) {
        this.dependency = i;
        this.graph = buildGraph();
    }

    /**
     * build the graph
     */
    public final Graph buildGraph() {
        LOGGER.debug("Building Graph *******************");
        EHMIProxySpec context;
        try {
            context = getContext();
        } catch (ExecutionException ex) {
            LOGGER.error("No Context found");
            return new Graph(0L);
        }
        graph = new Graph(context.getCurrentTimeInMillis());


        /* BUILD NODES FROM DEVICES */
        // Retrieving devices id
        JSONArray devices = context.getDevices();
        for (int i = 0; i < devices.length(); i++) {
            graph.addDevice(devices.optJSONObject(i));
        }

        /* BUILD NODES FROM PROGRAMS */
        for (String pid : dependency.getListProgramIds()) {
            ProgramGraph p = getProgramNode(pid);
            if (p != null) {
                graph.addProgram(pid, p.getProgramName(), p.getReferences(), p.getStateName());
            }
            // Link to the scheduler
            if (isPlanificationLink(pid)) {
                graph.addSchedulerEntity(pid);
            }
        }


        /* BUILD GHOSTS NODES */
        graph.buildGhosts();
        /* BUILD PLACE NODES */
        JSONArray places = context.getPlaces();
        for (int i = 0; i < places.length(); i++) {
            graph.addPlace(places.optJSONObject(i));

        }
        graph.buildTypes();
        return graph;
    }

    /**
     * Method to build the planification link of a program
     *
     * @param pid : id of the program we want to build planification links
     */
    private boolean isPlanificationLink(String pid) {
        try {
            return getContext().checkProgramIdScheduled(pid);
        } catch (ExecutionException ex) {
            LOGGER.error("No context found");
            return false;
        }
    }

    /**
     * Method to update the nodes graph with the latest values
     */
    private Graph updateGraph() {
        try {
            // Place names
            JSONArray places = getContext().getPlaces();
            if (places != null) {
                for (int j = 0; j < places.length(); j++) {
                    JSONObject place = places.getJSONObject(j);
                    graph.setPlaceName(place.optString("id"), place.optString("name"));
                }
            }

            // Devices
            JSONArray devices = getContext().getDevices();
            if (devices != null) {
                for (int j = 0; j < devices.length(); j++) {
                    JSONObject currentDevice = devices.getJSONObject(j);

                    String deviceType = currentDevice.getString("type");
                    switch (deviceType) {
                        case "3":
                            // Contact
                            graph.setDevice(currentDevice.optString("id"), currentDevice.get("contact").toString(), currentDevice.optString("name"));
                            break;
                        case "4":
                            // CardSwitch
                            graph.setDevice(currentDevice.optString("id"), currentDevice.get("inserted").toString(), currentDevice.optString("name"));
                            break;
                        case "6":
                            // Plug
                            graph.setDevice(currentDevice.optString("id"), currentDevice.get("plugState").toString(), currentDevice.optString("name"));
                            break;
                        case "7":
                            // Lamp
                            graph.setDevice(currentDevice.optString("id"), currentDevice.get("state").toString(), currentDevice.optString("name"));
                            break;
                    }

                }
            }
            // Programs
            for (String s : dependency.getListProgramIds()) {
                ProgramGraph currentProgram = getProgramNode(s);
                graph.setProgramState(s, currentProgram.getStateName());

            }
        } catch (JSONException ex) {
        } catch (ExecutionException e) {
            LOGGER.error("Context not found");
        }

        return graph;
    }

    /**
     * @return the EHMI Proxy
     */
    private EHMIProxySpec getContext() throws ExecutionException {
        if (this.dependency.getContext() == null) {
            throw new ExecutionException("Context not found", null);
        }
        return this.dependency.getContext();
    }

    /**
     *
     * @param pid
     * @return the program id corresponding to the pid
     */
    private ProgramGraph getProgramNode(String pid) {
        return dependency.getNodeProgram(pid);
    }

    /**
     *
     * @param programId
     * @return the graph once updated
     */
    public Boolean updateProgramStatus(String programId, String status) {
        return graph.setProgramState(programId, status);
    }

    /**
     *
     * @param srcId
     * @param varName
     * @param value
     * @return true if the graph has been updated
     */
    public Boolean updateDeviceStatus(String srcId, String varName, String value) {
        LOGGER.debug("update device status {}, {} =  {}", srcId, varName, value);
        switch (varName) {
            case "contact":
            case "inserted":
            case "plugState":
            case "state":
                return graph.setDeviceState(srcId, value);
            case "name":
                return graph.setDeviceName(srcId, value);
        }
        return false;
    }

    /**
     * 
     * @return graph
     */
    public Graph getGraph() {
        return graph;
    }

}
