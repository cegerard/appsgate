/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.eude.interpreter.langage.nodes.NodeSelect;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final String SERVICE_ENTITY = "service";
    private final String DEVICE_ENTITY = "device";
    private final String SELECTOR_ENTITY = "selector";
    
    private final String CLOCK_ID = "21106637055";

    /**
     * @param interpreter
     */
    public GraphManager(EUDEInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * @return the graph in JSON format
     */
    public JSONObject getGraph() {
        return returnJSONObject;
    }

    /**
     * build the graph
     */
    public void buildGraph() {
        initJSONObject();
        // Retrieving programs id
        this.programsId = interpreter.getListProgramIds(null);
        int idSelector = -2;
        ArrayList<NodeSelect> selectorsSaved = new ArrayList<NodeSelect>();
        
        for (String pid : programsId) {
            NodeProgram p = interpreter.getNodeProgram(pid);
            if (p != null) {
                
                // Get the current status of the program
                HashMap<String, String> optArg = new HashMap<String, String>();
                optArg.put("state", p.getState().name());
                addNode(PROGRAM_ENTITY, pid, p.getProgramName(), optArg);
                
                // Program links : Reference or planified
                ReferenceTable references = p.getReferences();
                for (DeviceReferences rdevice : references.getDevicesReferences()) {
                    if (rdevice.getDeviceId().equals(CLOCK_ID)) {
                        addLink(PLANIFIED_LINK, pid, rdevice.getDeviceId());
                    } else {
                        addLink(REFERENCE_LINK, pid, rdevice.getDeviceId(), rdevice.getReferencesData());
                    }
                }
                
                for (String rProgram : references.getProgramsId()) {
                    addLink(REFERENCE_LINK, pid, rProgram);
                }

                if (addSelector(pid, references, selectorsSaved, idSelector)) {
                    idSelector--;
                }
            }

            // Planification
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<Object> task = new Callable<Object>() {
                public Object call() {
                   return interpreter.getContext().checkProgramsScheduled();
                }
             };
            Future<Object> future = executor.submit(task);
            try {
                JSONArray programsScheduled = (JSONArray) future.get(2, TimeUnit.SECONDS);
            
                // Links program - scheduler
                if (programsScheduled != null && programsScheduled.toString().contains(pid)) {
                    addLink(PLANIFIED_LINK, pid, CLOCK_ID);
                    //@TODO: if planified more than one time, have more than one relation...
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
            
        }
        // Retrieving devices id
        JSONArray devices = this.interpreter.getContext().getDevices();
        for (int i = 0; i < devices.length(); i++) {
            try {
                JSONObject o = devices.getJSONObject(i);
                addDevice(o);
                
                // Don't add the location link of the service Weather and Mail
                if (!o.getString("type").equals("102") && !o.getString("type").equals("103")) {
                    // Adding location link
                    addLink(LOCATED_LINK, o.getString("id"), o.getString("placeId"));
                }
                
            } catch (JSONException ex) {
            }

        }
        JSONArray places = this.interpreter.getContext().getPlaces();
        for (int i = 0; i < places.length(); i++) {
            try {
                addPlace(places.getJSONObject(i));
            } catch (JSONException ex) {
            }

        }
        // Add manual for the unlocated place
//        JSONObject unLocatedPlace = new JSONObject();
//        try {
//            unLocatedPlace.put("id", "-1");
//            unLocatedPlace.put("name", "Unlocated");
//            addPlace(unLocatedPlace);
//        } catch (JSONException ex) {
//            java.util.logging.Logger.getLogger(GraphManager.class.getName()).log(Level.SEVERE, null, ex);
//        }

    }

    /**
     *
     * @param o
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
                    case 3 : // Contact
                        optArg.put("deviceState", o.getString("contact"));
                        break;
                    case 4 : // CardSwitch
                        optArg.put("deviceState", o.getString("inserted"));
                        break;
                    case 6 : // Plug
                        optArg.put("deviceState", o.getString("plugState"));
                        break;
                    case 7 : // Lamp
                        optArg.put("deviceState", String.valueOf(o.getBoolean("value")));
                        break;
                    default:
                        break;
                }
                
            } catch (JSONException ex) {
            }
            
            // Time special case
            if (o.getString("id").equals(CLOCK_ID)) {
                addNode(TIME_ENTITY, o.getString("id"), o.getString("name"), optArg);
            } else {
                addNode(DEVICE_ENTITY, o.getString("id"), o.getString("name"), optArg);
            }

        } catch (JSONException ex) {
            LOGGER.error("A node is malformated missing {}", ex.getCause());
            LOGGER.debug("Node: {}", o.toString());
        }
    }

    /**
     *
     * @param o
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
                for(HashMap<String,String> refOpt : optArgs){ 
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
    private boolean addSelector(String pid, ReferenceTable ref, ArrayList<NodeSelect> selectorsSaved, int idSelector) {
        boolean ret = false;
        String typeDevices = "";
        ArrayList<SelectReferences> selectors = ref.getSelectors();
        // For each selector present in the program...
        for (SelectReferences selector : selectors) {
         
            HashMap<String, ArrayList<String>> elements = (HashMap<String, ArrayList<String>>) selector.getNodeSelect().getPlaceDeviceSelector();

            ArrayList<String> placesSelector = elements.get("placeSelector");
            String placeId = "";
            // Get the id of the selector's place
            if (placesSelector.size() > 0) {
               placeId =  placesSelector.get(0);
            }
                     
            // Get the devices of the selector and link them to the selector
            ArrayList<String> devicesSelector = elements.get("deviceSelector");
            for (String deviceId : devicesSelector) {
                // Save the type of the devices once
                if (typeDevices.equals("")) {
                    try {
                        typeDevices = interpreter.getContext().getDevice(deviceId).getString(("type"));
                    } catch (JSONException ex) {
                        java.util.logging.Logger.getLogger(GraphManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                addLink(DENOTES_LINKS, "selector-" + typeDevices + "-" + placeId, deviceId);
            }
            
            // Add the location link
            addLink(LOCATED_LINK, "selector-" + typeDevices + "-" + placeId, placeId);
            // Add the reference
            addLink(REFERENCE_LINK, pid, "selector-" + typeDevices + "-" + placeId, selector.getReferencesData());
            
            if (!typeDevices.equals("")){      
                // If the selector hasn't been add to the graph, create the entity
                if (!isSelectorAlreadySaved(selectorsSaved, selector.getNodeSelect())) {
                    // Add selector : name = type devices selected and add type = selector
                    HashMap<String, String> optArg = new HashMap<String, String>();
                    optArg.put("type", "selector");
                    // id : selector - Type - Place, to be able to link different program to the same selector
                    addNode(SELECTOR_ENTITY, "selector-" + typeDevices + "-" + placeId, typeDevices, optArg);
                }
                // add the selector, to the list of selector added
                selectorsSaved.add(selector.getNodeSelect());
                typeDevices = "";
                ret = true;
            }
        }
        return ret;
    }

}
