/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
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
        for (String pid : programsId) {
            NodeProgram p = interpreter.getNodeProgram(pid);
            if (p != null) {
                addNode("program", pid, p.getProgramName());
                ReferenceTable references = p.getReferences();
                for (String rdevice : references.getDevicesId()) {
                    addLink("reference", pid, rdevice);
                }
                for (String rProgram : references.getProgramsId()) {
                    addLink("reference", pid, rProgram);
                }
            }
        }
        // Retrieving devices id
        JSONArray devices = this.interpreter.getContext().getDevices();
        for (int i = 0; i < devices.length(); i++) {
            try {
                JSONObject o = devices.getJSONObject(i);
                addDevice(o);
                // Adding location link
                addLink("isLocatedIn", o.getString("id"), o.getString("placeId"));
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
        JSONObject unLocatedPlace = new JSONObject();
        try {
            unLocatedPlace.put("id", "-1");
            unLocatedPlace.put("name", "Unlocated");
            addPlace(unLocatedPlace);
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(GraphManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     * @param o
     */
    private void addDevice(JSONObject o) {
        try {
            HashMap<String, String> optArg = new HashMap<String, String>();
            try {
                // send the deviceType to be able to recognize services
                optArg.put("deviceType", o.getString("type"));
            } catch (JSONException ex) {
            }
            try {
                // if it is a weather device, it will have a location, which will be used as a name
                optArg.put("location", o.getString("location"));
            } catch (JSONException ex) {
            }
            addNode("device", o.getString("id"), o.getString("name"), optArg);
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
            addNode("place", o.getString("id"), o.getString("name"));
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

}
