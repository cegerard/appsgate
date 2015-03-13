/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.ehmi.trace;

import appsgate.lig.context.dependency.spec.Dependencies;
import java.util.ArrayList;
import java.util.HashMap;
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
public class TraceTools {

    /**
     * The logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(TraceTools.class);

    /**
     *
     * @param tracesTab
     * @return
     * @throws JSONException
     */
    public static HashMap<String, GroupTuple> getTracesByType(JSONArray tracesTab) throws JSONException {
        return getTraces(tracesTab).getGroupsByType();
    }

    /**
     *
     * @param tracesTab
     * @return
     */
    private static Traces getTraces(JSONArray tracesTab) throws JSONException {

        Traces traces = new Traces();
        int length = tracesTab.length();

        for (int i = 0; i < length; i++) {
            JSONObject superTrace = tracesTab.getJSONObject(i);
            ArrayList<JSONObject> innerTraces = mergeInnerTraces(superTrace);

            for (JSONObject trace : innerTraces) {
                traces.addEntity(trace);
            }
        }
        return traces;
    }

    /**
     *
     * @param tracesTab
     * @param focus
     * @param dep
     * @return
     * @throws org.json.JSONException
     */
    public static HashMap<String, GroupTuple> getTracesByDep(JSONArray tracesTab, String focus, Dependencies dep)
            throws JSONException {
        Traces traces = getTraces(tracesTab);
        Set<String> ids = traces.getIds();
        ids.remove(focus);
        HashMap<String, GroupTuple> groupFollower = new HashMap<>();
        if (dep != null) {
            ids.removeAll(dep.getActsOnEntities());
            ids.removeAll(dep.getReadedEntities());
            groupFollower.put("dependencies.act", new GroupTuple(2, new JSONArray(dep.getActsOnEntities())));
            groupFollower.put("dependencies.read", new GroupTuple(3, new JSONArray(dep.getReadedEntities())));
        }
        groupFollower.put("focus", new GroupTuple(1, new JSONArray().put(focus)));
        groupFollower.put("others", new GroupTuple(4, new JSONArray(ids)));
        return groupFollower;
    }

    /**
     *
     * @param tracesTab
     * @param focus
     * @return
     * @throws JSONException
     */
    public static HashMap<String, GroupTuple> getTracesByLocation(JSONArray tracesTab, String focus)
            throws JSONException {
        HashMap<String, GroupTuple> groupFollower = new HashMap<>();
        groupFollower.put("focus", new GroupTuple(1, new JSONArray().put(focus)));
        groupFollower.put("others", new GroupTuple(3, new JSONArray()));
        int l = tracesTab.length();

        groupFollower.put(focus, new GroupTuple(1, new JSONArray()));
        groupFollower.put("others", new GroupTuple(3, new JSONArray()));

        for (int i = 0; i < l; i++) {
            JSONObject superTrace = tracesTab.getJSONObject(i);
            ArrayList<JSONObject> innerTraces = TraceTools.mergeInnerTraces(superTrace);

            for (JSONObject trace : innerTraces) {
                JSONArray objs;
                if (trace.has("location")) { //Equipment
                    JSONObject loc = trace.getJSONObject("location");

                    if (loc.has("id") && loc.getString("id").equalsIgnoreCase("-1")) {
                        if (!groupFollower.get(focus).toString().contains(trace.getString("id"))) {
                            objs = groupFollower.get("others").getMembers();
                        } else {
                            objs = groupFollower.get("focus").getMembers();
                        }
                    } else {
                        if (loc.getString("name").equalsIgnoreCase(focus)) {
                            objs = groupFollower.get(focus).getMembers();
                            //Remove dependency id from others array
                            JSONArray others = new JSONArray();
                            for (int j = 0; j < groupFollower.get("others").getMembers().length(); j++) {
                                String id = groupFollower.get("others").getMembers().getString(j);
                                if (trace.has("id") && !id.equalsIgnoreCase(trace.getString("id"))) {
                                    others.put(id);
                                }
                            }
                            groupFollower.get("others").setMembers(others);
                        } else {
                            if (trace.has("id") && !groupFollower.get(focus).toString().contains(trace.getString("id"))) {
                                objs = groupFollower.get("others").getMembers();
                            } else {
                                objs = groupFollower.get("focus").getMembers();
                            }
                        }
                    }
                } else { //Program
                    objs = groupFollower.get("others").getMembers();
                }

                if (trace.has("id") && !objs.toString().contains(trace.getString("id"))) {
                    objs.put(trace.get("id"));
                }
            }
        }
        return groupFollower;
    }

    /**
     *
     * @param tracesTab
     * @param focus
     * @return
     * @throws JSONException
     */
    public static HashMap<String, GroupTuple> getTracesByType(JSONArray tracesTab, String focus)
            throws JSONException {
        HashMap<String, GroupTuple> groupFollower = new HashMap<>();

        groupFollower.put(focus, new GroupTuple(1, new JSONArray()));
        groupFollower.put("others", new GroupTuple(3, new JSONArray()));
        int l = tracesTab.length();

        for (int i = 0; i < l; i++) {
            JSONObject superTrace = tracesTab.getJSONObject(i);
            ArrayList<JSONObject> innerTraces = TraceTools.mergeInnerTraces(superTrace);

            for (JSONObject trace : innerTraces) {
                JSONArray objs;

                String type = "program"; //Defaut it is a program
                if (trace.has("type")) { //in fact it is an equipment
                    type = trace.getString("type");
                }

                if (type.equalsIgnoreCase(focus)) {
                    objs = groupFollower.get(focus).getMembers();
                } else {
                    objs = groupFollower.get("others").getMembers();
                }

                if (trace.has("id") && !objs.toString().contains(trace.getString("id"))) {
                    objs.put(trace.get("id"));
                }
            }
        }
        return groupFollower;
    }

    /**
     * Merge programs and equipment traces from a super traces into a simple
     * arraylist of JSONbject
     *
     * @param superTrace the super traces from any sources
     * @return an ArrayList<JSONObject> of all inner traces
     * @throws JSONException
     */
    public static ArrayList<JSONObject> mergeInnerTraces(JSONObject superTrace) throws JSONException {
        ArrayList<JSONObject> innerTraces = new ArrayList<>();

        JSONArray pgms = superTrace.getJSONArray("programs");
        JSONArray devices = superTrace.getJSONArray("devices");

        int nbPgms = pgms.length();
        int nbDev = devices.length();

        for (int i = 0; i < nbPgms; i++) {
            innerTraces.add(pgms.getJSONObject(i));
        }

        for (int j = 0; j < nbDev; j++) {
            innerTraces.add(devices.getJSONObject(j));
        }

        return innerTraces;
    }

}
