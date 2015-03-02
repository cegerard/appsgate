/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.references;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import appsgate.lig.eude.interpreter.langage.components.SpokParser;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.eude.interpreter.langage.nodes.NodeSelect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(SpokParser.class);

    /**
     *
     */
    private final EUDEInterpreter interpreter;
    /**
     *
     */
    private List<String> programsId;
    /**
     *
     */
    private JSONObject returnJSONObject;

    // Constants string for entity and relation JSON
    private final String REFERENCE_LINK = "reference";
    private final String LOCATED_LINK = "isLocatedIn";
    private final String PLANIFIED_LINK = "isPlanified";
    private final String PROGRAM_ENTITY = "program";
    private final String DENOTES_LINKS = "denotes";
    private final String PLACE_ENTITY = "place";
    private final String TIME_ENTITY = "time";
    private final String DEVICE_ENTITY = "device";
    private final String SELECTOR_ENTITY = "selector";

    private final String CLOCK_ID = "21106637055";
    private final String SCHEDULER_ID = "-21106637055";

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
        return returnJSONObject;
    }

    /**
     * build the graph
     */
    public void buildGraph() {
        initJSONObject();
        // Retrieving programs id
        this.programsId = interpreter.getListProgramIds(null);

        // Collection used to store the selector we added
        ArrayList<NodeSelect> selectorsSaved = new ArrayList<NodeSelect>();

        // Collection to store the entities (program and devices) we added
        HashSet<DeviceReference> entitiesAdded = new HashSet<DeviceReference>();

        // Collection to store the program ghost we will add
        HashSet<ProgramReference> ghostPrograms = new HashSet<ProgramReference>();

        boolean isSchedulerAdded = false;

        /* BUILD NODES FROM PROGRAMS */
        for (String pid : programsId) {
            NodeProgram p = interpreter.getNodeProgram(pid);
            if (p != null) {

                // Get the current status of the program
                HashMap<String, String> optArg = new HashMap<String, String>();
                optArg.put("state", p.getState().name());
                addNode(PROGRAM_ENTITY, pid, p.getProgramName(), optArg);

                // Program links : Reference or planified
                ReferenceTable references = p.getReferences();
                // Links to the devices
                for (DeviceReference rdevice : references.getDevicesReferences()) {
                    addLink(REFERENCE_LINK, pid, rdevice.getDeviceId(), rdevice.getReferencesData());
                    entitiesAdded.add(rdevice);
                }
                // Links to the programs
                for (ProgramReference rProgram : references.getProgramsReferences()) {
                    addLink(REFERENCE_LINK, pid, rProgram.getProgramId(), rProgram.getReferencesData());

                    if (rProgram.getProgramStatus() == ReferenceTable.STATUS.MISSING) {
                        ghostPrograms.add(rProgram);
                    }
                }

                // Add selectors from this program
                addSelector(pid, references, selectorsSaved);
            }

            // Link to the scheduler
            if (buildPlanificationLink(pid) && !isSchedulerAdded) {
                addNode(TIME_ENTITY, SCHEDULER_ID, "schedule");
                isSchedulerAdded = true;
            }
        }

        /* BUILD NODES FROM DEVICES */
        // Retrieving devices id
        JSONArray devices = this.interpreter.getContext().getDevices();
        for (int i = 0; i < devices.length(); i++) {
            try {
                JSONObject o = devices.getJSONObject(i);

                addDevice(o);
                // Remove this device from the potential ghost
                for (Iterator<DeviceReference> it = entitiesAdded.iterator(); it.hasNext();) {
                    DeviceReference dRef = it.next();
                    if (dRef.getDeviceId().equals(o.getString("id"))) {
                        it.remove();
                    }
                }

                // Don't add the location link of the service Weather and Mail
                if (!o.getString("type").equals("102") && !o.getString("type").equals("103")) {
                    // Adding location link
                    addLink(LOCATED_LINK, o.getString("id"), o.getString("placeId"));
                }

            } catch (JSONException ex) {
                LOGGER.error("JSON error during add device {}", ex.getCause());
            }
        }

        /* BUILD GHOSTS NODES */
        buildDeviceGhosts(entitiesAdded);
        buildProgramGhosts(ghostPrograms);

        /* BUILD PLACE NODES */
        buildPlaces();

    }

    /**
     * Method add the node placs to the json object
     */
    private void buildPlaces() {
        JSONArray places = this.interpreter.getContext().getPlaces();
        for (int i = 0; i < places.length(); i++) {
            try {
                addPlace(places.getJSONObject(i));
            } catch (JSONException ex) {
                LOGGER.error("JSON error during buidling place {}", ex.getCause());
            }

        }
        // Add manual for the unlocated place
        /*JSONObject unLocatedPlace = new JSONObject();
         try {
         unLocatedPlace.put("id", "-1");
         unLocatedPlace.put("name", "Unlocated");
         addPlace(unLocatedPlace);
         } catch (JSONException ex) {
         java.util.logging.Logger.getLogger(GraphManager.class.getName()).log(Level.SEVERE, null, ex);
         }*/
    }

    /**
     * Method to build the planification link of a program
     *
     * @param pid : id of the program we want to build planificatin links
     */
    private boolean buildPlanificationLink(String pid) {
        boolean isOnePlanification = false;

        // Planification of the checkPrograms to avoid stucking if no scheduling service
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Object> task = new Callable<Object>() {
            @Override
            public Object call() {
                return interpreter.getContext().checkProgramsScheduled();
            }
        };
        Future<Object> future = executor.submit(task);
        try {
            JSONArray programsScheduled = (JSONArray) future.get(2, TimeUnit.SECONDS);

            // Links program - scheduler
            if (programsScheduled != null && programsScheduled.toString().contains(pid)) {
                addLink(PLANIFIED_LINK, pid, SCHEDULER_ID);
                isOnePlanification = true;
            }
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

        return isOnePlanification;
    }

    /**
     * Method thats add Device to the json object
     *
     * @param o : JSONOjevt of the device to add
     */
    private void addDevice(JSONObject o) {
        try {
            HashMap<String, String> optArg = new HashMap<String, String>();
            String deviceType = "";
            try {
                // if it is a weather device, it will have a location, which will be used as a name
                optArg.put("location", o.getString("location"));
            } catch (JSONException ex) {
            }

            try {
                deviceType = o.getString("type");
                // send the deviceType to be able to recognize services
                optArg.put("deviceType", deviceType);
            } catch (JSONException ex) {
            }

            try {
                switch (Integer.parseInt(deviceType)) {
                    case 3: // Contact
                        optArg.put("deviceState", o.getString("contact"));
                        break;
                    case 4: // CardSwitch
                        optArg.put("deviceState", o.getString("inserted"));
                        break;
                    case 6: // Plug
                        optArg.put("deviceState", o.getString("plugState"));
                        break;
                    case 7: // Lamp
                        optArg.put("deviceState", String.valueOf(o.getBoolean("state")));
                        break;
                    default:
                        break;
                }

            } catch (JSONException ex) {
                LOGGER.error("JSON error of deviceState {}", ex.getCause());
            }

            // Time special case
            if (o.getString("id").equals(CLOCK_ID)) {
                addNode(TIME_ENTITY, o.getString("id"), "clock", optArg);
            } else {
                addNode(DEVICE_ENTITY, o.getString("id"), o.getString("name"), optArg);
            }

        } catch (JSONException ex) {
            LOGGER.error("A node is malformated missing {}", ex.getCause());
            LOGGER.debug("Node: {}", o.toString());
        }
    }

    /**
     * Method to add device ghosts to the json object
     *
     * @param ghosts : HashSet of the ghosts
     */
    private void buildDeviceGhosts(HashSet<DeviceReference> ghosts) {
        for (DeviceReference dRef : ghosts) {
            addGhost("device", dRef.getDefaultName(), dRef.getDeviceId());
        }
    }

    /**
     * Method to add program ghosts to the json object
     *
     * @param ghosts : HashSet of the ghosts
     */
    private void buildProgramGhosts(HashSet<ProgramReference> ghosts) {
        for (ProgramReference pRef : ghosts) {
            addGhost("program", pRef.getDefaultName(), pRef.getId());
        }
    }

    /**
     * Method to add a Ghost Node
     *
     * @param typeGhost : String to describe the type of ghost : Devive or
     * Program
     * @param id : String id of ghost
     */
    private void addGhost(String typeGhost, String name, String id) {
        HashMap<String, String> optArg = new HashMap<String, String>();
        optArg.put("isGhost", Boolean.TRUE.toString());
        if (typeGhost.equals("device")) {
            addNode(DEVICE_ENTITY, id, name, optArg);
        } else {
            addNode(PROGRAM_ENTITY, id, name, optArg);
        }
    }

    /**
     * Method that adds a place node to the json object
     *
     * @param o : JSONObject of the place to add
     */
    private void addPlace(JSONObject o) {
        try {
            addNode(PLACE_ENTITY, o.getString("id"), o.getString("name"));
        } catch (JSONException ex) {
            LOGGER.error("A node is malformated missing {}", ex.getCause());
            LOGGER.debug("Node: {}", o.toString());
        }
    }

    /**
     * Method that adds a node to the json object
     *
     * @param type the type of node (program, device, place)
     * @param id the id of the node (id of program or device, or plae)
     * @param name the name which will be rendered
     */
    private void addNode(String type, String id, String name) {
        try {
            JSONObject o = new JSONObject();
            o.put("type", type);
            o.put("id", id);
            o.put("name", name);
            returnJSONObject.getJSONArray("nodes").put(o);
        } catch (JSONException ex) {
            // Nothing will be raised since there is no null value
        }
    }

    /**
     * Method that adds a node to the json object
     *
     * @param type the type of node (program, device, place)
     * @param id the id of the node (id of program or device, or plae)
     * @param name the name which will be rendered
     * @param optArgs arguments for some exceptions : deviceType, location,..
     */
    private void addNode(String type, String id, String name, HashMap<String, String> optArgs) {
        try {
            JSONObject o = new JSONObject();
            o.put("type", type);
            o.put("id", id);
            o.put("name", name);
            for (Entry<String, String> arg : optArgs.entrySet()) {
                o.put(arg.getKey(), arg.getValue());
            }
            returnJSONObject.getJSONArray("nodes").put(o);
        } catch (JSONException ex) {
            // Nothing will be raised since there is no null value
        }
    }

    /**
     * Method that adds a link to the json object
     *
     * @param type the type of link
     * @param source the id of the source of the link
     * @param target the id of the source of the target
     */
    private void addLink(String type, String source, String target) {
        try {
            JSONObject o = new JSONObject();
            o.put("type", type);
            o.put("source", source);
            o.put("target", target);
            returnJSONObject.getJSONArray("links").put(o);
        } catch (JSONException ex) {
            // Nothing will be raised since there is no null value
        }
    }

    /**
     * Method that adds a link to the json object
     *
     * @param type the type of link
     * @param source the id of the source of the link
     * @param target the id of the source of the target
     */
    private void addLink(String type, String source, String target, ArrayList<HashMap<String, String>> optArgs) {
        try {
            JSONObject o = new JSONObject();
            o.put("type", type);
            o.put("source", source);
            o.put("target", target);
            JSONArray refArray = new JSONArray();
            if (optArgs != null) {
                for (HashMap<String, String> refOpt : optArgs) {
                    JSONObject ref = new JSONObject();
                    if (refOpt != null) {
                        for (Entry<String, String> arg : refOpt.entrySet()) {
                            ref.put(arg.getKey(), arg.getValue());
                        }
                    }
                    refArray.put(ref);
                }
                o.put("referenceData", refArray);
            }
            returnJSONObject.getJSONArray("links").put(o);
        } catch (JSONException ex) {
            // Nothing will be raised since there is no null value
        }
    }

    /**
     * Init the JSON Object that will be returned
     */
    private void initJSONObject() {
        try {
            returnJSONObject = new JSONObject();
            returnJSONObject.put("nodes", new JSONArray());
            returnJSONObject.put("links", new JSONArray());
        } catch (JSONException ex) {
            // Nothing will be raised since there is no null value
        }
    }

    /**
     * Method to check if a selector has already been added to the graph
     *
     * @param selectorsSaved : ArrayList of selector already added
     * @param programSelector : selector to check
     * @return true if programSelector already added
     */
    private boolean isSelectorAlreadySaved(ArrayList<NodeSelect> selectorsSaved, NodeSelect programSelector) {
        for (NodeSelect selSaved : selectorsSaved) {
            try {
                JSONArray pTypeDevice = (JSONArray) programSelector.getJSONDescription().get("what");
                JSONArray sTypeDevice = (JSONArray) selSaved.getJSONDescription().get("what");
                JSONArray pLocationDevice = (JSONArray) programSelector.getJSONDescription().get("where");
                JSONArray sLocationDevice = (JSONArray) selSaved.getJSONDescription().get("where");

                if (pTypeDevice.getJSONObject(0).get("value").equals(sTypeDevice.getJSONObject(0).get("value"))
                        && pLocationDevice.getJSONObject(0).get("value").equals(sLocationDevice.getJSONObject(0).get("value"))) {
                    return true;
                }
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(GraphManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    /**
     * Method to add the selector presented in a program
     *
     * @param pid : Program Id in which we search the selectors
     * @param ref : ReferenceTable of the program to get the selectors
     * @param selectorsSaved
     * @param idSelector
     * @return
     */
    private boolean addSelector(String pid, ReferenceTable ref, ArrayList<NodeSelect> selectorsSaved) {
        boolean ret = false;
        String typeDevices = "";
        ArrayList<SelectReference> selectors = ref.getSelectors();
        // For each selector present in the program...
        for (SelectReference selector : selectors) {
            HashMap<String, ArrayList<String>> elements = (HashMap<String, ArrayList<String>>) selector.getNodeSelect().getPlaceDeviceSelector();
            ArrayList<String> placesSelector = elements.get("placeSelector");

            // Boolean to know if a selector has already been created
            boolean isSelectorAlreadySaved = isSelectorAlreadySaved(selectorsSaved, selector.getNodeSelect());

            // If there is no place, it is by default "everywhere"
            String placeId = "everywhere";
            // Get the id of the selector's place
            if (placesSelector.size() > 0) {
                placeId = placesSelector.get(0);
            }

            // Get the devices of the selector and link them to the selector
            ArrayList<String> devicesSelector = elements.get("deviceSelector");
            for (String deviceId : devicesSelector) {
                // Save the type of the devices once (because they have all the same type, so that avoid to make multiple call to the interpreter)
                if (typeDevices.equals("")) {
                    try {
                        typeDevices = interpreter.getContext().getDevice(deviceId).getString(("type"));
                    } catch (JSONException ex) {
                        java.util.logging.Logger.getLogger(GraphManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (!isSelectorAlreadySaved) {
                    // Add the link "denotes" if the selector isn't already created or we will have more than one link
                    addLink(DENOTES_LINKS, "selector-" + typeDevices + "-" + placeId, deviceId);
                }
            }

            // Add the reference : Program - Selector
            addLink(REFERENCE_LINK, pid, "selector-" + typeDevices + "-" + placeId, selector.getReferencesData());

            if (!typeDevices.equals("")) {
                // If the selector hasn't been add to the graph, create the entity
                if (!isSelectorAlreadySaved) {
                    // Add selector : name = type devices selected and add type = selector
                    HashMap<String, String> optArg = new HashMap<String, String>();
                    optArg.put("type", "selector");
                    // id : selector - Type - Place, to be able to link different program to the same selector
                    addNode(SELECTOR_ENTITY, "selector-" + typeDevices + "-" + placeId, typeDevices, optArg);
                    // Add the location link : Location - Selector. Add one time
                    addLink(LOCATED_LINK, "selector-" + typeDevices + "-" + placeId, placeId);
                }
                // add the selector, to the list of selector added
                selectorsSaved.add(selector.getNodeSelect());
                typeDevices = "";
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Method to update the nodes graph with the latest values
     */
    private void updateGraph() {
        try {
            // Loop over all nodes to updates them
            for (int i = 0; i < this.returnJSONObject.getJSONArray("nodes").length(); i++) {
                JSONObject node = this.returnJSONObject.getJSONArray("nodes").getJSONObject(i);
                try {
                    // If ghost, no need update
                    node.get("isGhost");
                } catch (JSONException ex) {
                    // "isGhost" not presented in JSON, so catch JSONException
                    if (node.get("type") == DEVICE_ENTITY) {
                        // Device
                        JSONObject currentDevice = this.interpreter.getContext().getDevice(node.getString("id"));
                        // If null it is a ghost, so no need to update
                        if (currentDevice == null) {
                            continue;
                        }
                        // Update Value
                        switch (Integer.parseInt(currentDevice.getString("type"))) {
                            case 3: // Contact
                                node.put("deviceState", currentDevice.getString("contact"));
                                break;
                            case 4: // CardSwitch
                                node.put("deviceState", currentDevice.getString("inserted"));
                                break;
                            case 6: // Plug
                                node.put("deviceState", currentDevice.getString("plugState"));
                                break;
                            case 7: // Lamp
                                node.put("deviceState", String.valueOf(currentDevice.getBoolean("state")));
                                break;
                            default:
                                break;
                        }
                        // Update Name
                        node.put("name", currentDevice.getString("name"));
                    } else if (node.get("type") == PROGRAM_ENTITY) {
                        // Program
                        NodeProgram currentProgram = interpreter.getNodeProgram(node.getString("id"));
                        // If null it is a ghost, so no need to update
                        if (currentProgram != null) {
                            // Update state
                            node.put("state", currentProgram.getState());
                        }
                    } else if (node.get("type") == PLACE_ENTITY) {
                        // Place
                        for (int j = 0; j < this.interpreter.getContext().getPlaces().length(); j++) {
                            JSONObject place = this.interpreter.getContext().getPlaces().getJSONObject(j);
                            if (place.getString("id").equals(node.getString("id"))) {
                                // Update Name
                                node.put("name", place.getString("name"));
                            }
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(GraphManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
