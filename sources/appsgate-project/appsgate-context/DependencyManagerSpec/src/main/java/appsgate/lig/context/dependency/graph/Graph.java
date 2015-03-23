package appsgate.lig.context.dependency.graph;

import appsgate.lig.context.dependency.spec.Dependencies;
import appsgate.lig.ehmi.spec.SpokObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class Graph implements SpokObject {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Graph.class);

    // Collection used to store the selector we added
    private final ArrayList<Selector> selectorsSaved;

    // Collection to store the devices we added
    private final HashMap<String, String> devices;
    // Collection to store the entities (program and devices) we added
    private final HashSet<DeviceReference> ghostDevices;

    // Collection to store the program we added
    private final HashMap<String, String> programs;

    // Collection to store the program ghost we will add
    private final HashSet<String> ghostPrograms;

    private final HashSet<String> deviceTypes;

    // Map to store the program states
    private final HashMap<String, String> programState;
    // Map to store the program states
    private final HashMap<String, String> placeName;
    // Map to store the program states
    private final HashMap<String, String> deviceState;
    // Map to store the program states
    private final HashMap<String, String> deviceName;

    private final HashMap<String, Dependencies> dependencies;

    //
    boolean isSchedulerAdded = false;
    //
    private final String time;
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

    // the json object that will be returned
    private final JSONObject returnJSONObject;

    /**
     * Constructor
     * @param timestamp
     */
    public Graph(Long timestamp) {
        this(new JSONObject(), new JSONArray(), timestamp.toString());
        try {
            returnJSONObject.put("nodes", new JSONArray());
            returnJSONObject.put("links", new JSONArray());
        } catch (JSONException ex) {
        }
    }
    
    /**
     * 
     * @param jsonGraph
     * @param devices 
     * @param timestamp 
     */
    public Graph(JSONObject jsonGraph, JSONArray devices, String timestamp) {
        
        this.dependencies = new HashMap<>();
        for (int i = 0 ; i < devices.length(); i++) {
            JSONObject line;
            try {
                line = devices.getJSONObject(i);
                String key = line.getString("object");
                this.dependencies.put(key, new Dependencies(key, line));
            } catch (JSONException e) {
                
            }
        }
        this.programs = new HashMap<>();
        this.ghostPrograms = new HashSet<>();
        this.devices = new HashMap<>();
        this.selectorsSaved = new ArrayList<>();
        this.returnJSONObject = jsonGraph;
        this.ghostDevices = new HashSet<>();
        this.programState = new HashMap<>();
        this.placeName = new HashMap<>();
        this.deviceState = new HashMap<>();
        this.deviceName = new HashMap<>();
        this.deviceTypes = new HashSet<>();
        this.time = timestamp;
    }

    @Override
    public JSONObject getJSONDescription() {
        return returnJSONObject;
    }

    @Override
    public String getType() {
        return "dependencyGraph";
    }

    @Override
    public String getValue() {
        return "[Dependency Graph]";
    }

    /**
     * Method that adds a place node to the json object
     *
     * @param o : JSONObject of the place to add
     */
    public void addPlace(JSONObject o) {
        if (o == null) {
            return;
        }
        try {
            addNode(PLACE_ENTITY, o.getString("id"), o.getString("name"));
        } catch (JSONException ex) {
            LOGGER.error("A node is malformated missing {}", ex.getCause());
            LOGGER.debug("Node: {}", o.toString());
        }
    }

    /**
     *
     * @param pid
     * @param programName
     * @param references
     * @param state
     */
    public void addProgram(String pid, String programName, Dependencies references, String state) {
        programs.put(pid, programName);
        // Get the current status of the program
        HashMap<String, String> optArg = new HashMap<>();
        optArg.put("state", state);
        addNode(PROGRAM_ENTITY, pid, programName, optArg);

        // Program links : Reference or planified
        // Links to the devices
        for (DeviceReference rdevice : references.getDevicesReferences()) {
            addLink(REFERENCE_LINK, pid, rdevice.getDeviceId(), rdevice.getReferencesData());
            if (rdevice.getDeviceStatus() == Reference.STATUS.MISSING) {
                ghostDevices.add(rdevice);
            }
        }
        // Links to the programs
        for (ProgramReference rProgram : references.getProgramsReferences()) {
            addLink(REFERENCE_LINK, pid, rProgram.getProgramId(), rProgram.getReferencesData());

            if (rProgram.getProgramStatus() == Reference.STATUS.MISSING) {
                ghostPrograms.add(pid);
            }
        }

        // Add selectors from this program
        addSelector(pid, references);
    }

    public void addSchedulerEntity(String pid) {
        addLink(PLANIFIED_LINK, pid, SCHEDULER_ID);
        if (!isSchedulerAdded) {
            addNode(TIME_ENTITY, SCHEDULER_ID, "schedule");
            isSchedulerAdded = true;
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
     * Method that adds a node to the JSON object
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
     * Method thats add Device to the JSON object
     *
     * @param o : JSONOject of the device to add
     */
    public void addDevice(JSONObject o) {
        if (o == null) {
            return;
        }
        try {

            HashMap<String, String> optArg = new HashMap<>();
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
                deviceTypes.add(deviceType);
            } catch (JSONException ex) {
            }

            try {
                if(deviceType.equals("3")) { // Contact
                        optArg.put("deviceState", o.getString("contact"));
                } else if(deviceType.equals("4")) { // CardSwitch
                        optArg.put("deviceState", o.getString("inserted"));
                } else if(deviceType.equals("6")) { // Plug
                        optArg.put("deviceState", o.getString("plugState"));
                } else if(deviceType.equals("7")) { // Lamp
                        optArg.put("deviceState", String.valueOf(o.getBoolean("state")));
                }

            } catch (JSONException ex) {
                LOGGER.error("JSON error of deviceState {}", ex.getCause());
            }
            devices.put(o.getString("id"), deviceType);

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
        // Remove this device from the potential ghost
        try {
            for (Iterator<DeviceReference> it = ghostDevices.iterator(); it.hasNext();) {
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
            LOGGER.error("A node is malformated missing {}", ex.getCause());
            LOGGER.debug("Node: {}", o.toString());
        }

    }

    /**
     * Method to add ghosts to the json object
     */
    public void buildGhosts() {
        for (DeviceReference dRef : this.ghostDevices) {
            addGhost("device", dRef.getDefaultName(), dRef.getDeviceId());
        }
        for (String ghost : this.ghostPrograms) {
            addGhost("program", programs.get(ghost), ghost);
        }

    }

    /**
     * Method to add a Ghost Node
     *
     * @param typeGhost : String to describe the type of ghost : Device or
     * Program
     * @param id : String id of ghost
     */
    private void addGhost(String typeGhost, String name, String id) {
        HashMap<String, String> optArg = new HashMap<>();
        if (typeGhost.equals("device")) {
            optArg.put("isGhost", "ghostDevice");
            addNode(DEVICE_ENTITY, id, name, optArg);
        } else {
            optArg.put("isGhost", "ghostProgram");
            addNode(PROGRAM_ENTITY, id, name, optArg);
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
    private void addLink(String type, String source, String target, ArrayList<ReferenceDescription> optArgs) {
        try {
            JSONObject o = new JSONObject();
            o.put("type", type);
            o.put("source", source);
            o.put("target", target);
            JSONArray refArray = new JSONArray();
            if (optArgs != null) {
                for (ReferenceDescription refOpt : optArgs) {
                    if (refOpt != null) {
                        refArray.put(refOpt.getJSONDescription());
                        addDependencyLink(refOpt.getReferenceType(), source, target);
                    }
                }
                o.put("referenceData", refArray);
            }
            returnJSONObject.getJSONArray("links").put(o);
        } catch (JSONException ex) {
            // Nothing will be raised since there is no null value
        }
    }

    private void addDependencyLink(String type, String source, String target) {
        Dependencies sDep = getDependencies(source);
        Dependencies tDep = getDependencies(target);
        switch (type.toUpperCase()) {
            case "READING":
                sDep.addLinkEntityReaded(target);
                tDep.addLinkEntityReading(source);
                break;
            case "WRITING":
                sDep.addLinkEntityChanged(target);
                tDep.addLinkEntityChanging(source);
                break;
            default:
                return;
        }
        dependencies.put(source, sDep);
        dependencies.put(target, tDep);
    }

    /**
     * Method to add the selector presented in a program
     *
     * @param pid : Program Id in which we search the selectors
     * @param ref : ReferenceTable of the program to get the selectors
     * @return
     */
    private boolean addSelector(String pid, Dependencies ref) {
        boolean ret = false;
        String typeDevices = "";
        Set<SelectReference> selectors = ref.getSelectors();
        // For each selector present in the program...
        for (SelectReference selector : selectors) {
            HashMap<String, ArrayList<String>> elements = (HashMap<String, ArrayList<String>>) selector.getNodeSelect().getPlaceDeviceSelector();
            ArrayList<String> placesSelector = elements.get("placeSelector");

            // Boolean to know if a selector has already been created
            boolean isSelectorAlreadySaved = isSelectorAlreadySaved(selector.getNodeSelect());

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
                    typeDevices = devices.get(deviceId);
                    if (typeDevices == null) {
                        typeDevices = "UnknownType";
                        LOGGER.error("Unknown type found for device {}", deviceId);
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
                    HashMap<String, String> optArg = new HashMap<>();
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
     * Method to check if a selector has already been added to the graph
     *
     * @param selectorsSaved : ArrayList of selector already added
     * @param programSelector : selector to check
     * @return true if programSelector already added
     */
    private boolean isSelectorAlreadySaved(Selector programSelector) {
        for (Selector selSaved : selectorsSaved) {
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
            }
        }
        return false;
    }

    /**
     *
     * @param pid
     * @param state
     */
    public void setProgramState(String pid, String state) {
        programState.put(pid, state);
    }

    /**
     *
     * @param id
     * @param name
     */
    public void setPlaceName(String id, String name) {
        placeName.put(id, name);
    }

    /**
     *
     * @param did
     * @param state
     * @param name
     */
    public void setDevice(String did, String state, String name) {
        deviceState.put(did, state);
        deviceName.put(did, name);
    }

    /**
     *
     * @throws JSONException
     */
    private void updateJSON() throws JSONException {

        for (int i = 0; i < this.returnJSONObject.getJSONArray("nodes").length(); i++) {
            JSONObject node = this.returnJSONObject.getJSONArray("nodes").getJSONObject(i);
            if (!node.has("isGhost")) {
                String id = node.getString("id");

                // "isGhost" not presented in JSON, so catch JSONException
                if (node.get("type") == DEVICE_ENTITY) {
                    if (deviceName.containsKey(id)) {
                        node.put("name", deviceName.get(id));
                    }
                    if (deviceState.containsKey(id)) {
                        node.put("deviceState", deviceState.get(id));
                    }
                } else if (node.get("type") == PROGRAM_ENTITY) {
                    if (programState.containsKey(id)) {
                        // Update state
                        node.put("state", programState.get(id));
                    }
                } else if (node.get("type") == PLACE_ENTITY) {
                    if (placeName.containsKey(id)) {
                        // Update Name
                        node.put("name", placeName.get(id));

                    }
                }
            }
        }
    }

    /**
     *
     * @param id
     * @return
     */
    public Dependencies getDependencies(String id) {
        if (dependencies.containsKey(id)) {
            return dependencies.get(id);
        }
        return new Dependencies(id);
    }

    /**
     * 
     */
    public void buildTypes() {
        try {
            returnJSONObject.put("types", deviceTypes);
        } catch (JSONException ex) {
        }

    }

    /**
     * 
     * @return 
     */
    public JSONArray getJSONDependencies() {
        JSONArray array = new JSONArray();
        for (String k : this.dependencies.keySet()) {
            array.put(this.dependencies.get(k).getJSONDescription());
        }
        return array;
    }
}
