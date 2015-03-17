package appsgate.lig.ehmi.trace.queue;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A thread class to trace all elements in the trace queue
 *
 * @author Cedric Gerard
 * @since August 06, 2014
 * @version 0.5.0
 */
public class TraceExecutor implements Runnable {

    /**
     * Indicates if the thread is in infinite waiting
     */
    private boolean sleeping;

    /**
     * Use to manage thread loop execution
     */
    private boolean start;

    /**
     * The parent trace queue
     */
    private final TraceQueue traceQueue;

    /**
     * Default constructor
     *
     * @param traceQueue
     */
    public TraceExecutor(TraceQueue traceQueue) {

        this.traceQueue = traceQueue;

        start = false;
        sleeping = false;
    }

    public void stop() {
        start = false;
        traceQueue.setInitiated(false);
    }

    @Override
    public void run() {
        start = true;
        while (start) {
            try {
                if (traceQueue.getDeltaTinMillis() > 0 || traceQueue.isEmpty()) {
                    synchronized (this) {
                        sleeping = true;
                        wait();
                        sleeping = false;
                    }
                }
                if (start) {
                    traceQueue.getManager().sendTraces(apply(null));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Apply a policy on the queue to aggregate traces The default policy is the
     * deltaTInMillis attribute
     *
     * @param policy other policies to apply (e.g. type of device)
     * @return the aggregated traces as a JSONArray
     * @throws JSONException
     */
    public JSONArray apply(JSONObject policy) throws JSONException {
        synchronized (traceQueue) {

            JSONArray aggregateTraces = new JSONArray();

            // No aggregation
            if (policy == null && traceQueue.getDeltaTinMillis() == 0) {

                while (!traceQueue.isEmpty()) {
                    JSONObject trace = traceQueue.poll();
                    aggregateTraces.put(trace);
                }

            } else {
                if (policy == null) { // default aggregation (time and
                    // identifiers)
                    // Get all traces from trace queue
                    JSONArray tempTraces = new JSONArray();
                    while (!traceQueue.isEmpty()) {
                        tempTraces.put(traceQueue.poll());
                    }
                    aggregation(aggregateTraces, tempTraces, traceQueue.getLogTime());

                } else { // Apply specific aggregation policy
                    // generic aggregation mechanism
                    // aggregationWithPolicy(aggregateTraces, tracesPacket,
                    // logTime, policy)
                }
            }

            return aggregateTraces;
        }
    }

    /**
     * Aggregates traces from a packet and add the aggregate trace to an array
     *
     * @param aggregateTraces result of all aggregations
     * @param tracesPacket traces to aggregates
     * @param logTime timestamp for this trace
     */
    public void aggregation(JSONArray aggregateTraces, JSONArray tracesPacket, long logTime) {
        // Create new aggregate trace instance
        JSONObject jsonTrace = new JSONObject();
        try {
            jsonTrace.put("timestamp", logTime);
        } catch (JSONException ex) {
        }
        HashMap<String, JSONObject> devicesToAgg = new HashMap<>();
        HashMap<String, JSONObject> programsToAgg = new HashMap<>();

        int nbTraces = tracesPacket.length();
        int i = 0;
        while (i < nbTraces) {
            // Get a trace to aggregate from the array
            JSONObject tempObj = tracesPacket.optJSONObject(i);
            JSONArray tempDevices = tempObj.optJSONArray("devices");
            JSONArray tempPgms = tempObj.optJSONArray("programs");

            int tempDevicesSize = tempDevices.length();
            int tempPgmsSize = tempPgms.length();

            // If there is some device trace to merge
            if (tempDevicesSize > 0) {
                int x = 0;
                while (x < tempDevicesSize) {
                    // Merge the device trace
                    JSONObject tempDev = tempDevices.optJSONObject(x);
                    String id = tempDev.optString("id");

                    if (!devicesToAgg.containsKey(id)) { // No aggregation for now
                        devicesToAgg.put(id, tempDev);

                    } else { // Device id exist for this time stamp -->
                        // aggregation
                        JSONObject existingDev = devicesToAgg.get(id);
                        if (tempDev.has("event")) {
                            try {
                            // replace the state by the
                                // last known state
                                existingDev.put("event", tempDev.opt("event"));
                            } catch (JSONException ex) {
                            }
                        }
                        // Aggregates the device trace has a decoration
                        JSONArray existingDecorations = existingDev.optJSONArray("decorations");
                        JSONArray tempDecs = tempDev.optJSONArray("decorations");
                        int decSize = tempDecs.length();
                        int x1 = 0;
                        while (x1 < decSize) {
                            JSONObject tempDec = tempDecs.optJSONObject(x1);
                            try {
                                tempDec.put("order", existingDecorations.length());
                            } catch (JSONException ex) {
                            }
                            existingDecorations.put(tempDec);
                            x1++;
                        }
                    }
                    x++;
                }
            }

            // If there is some program traces to merge
            if (tempPgmsSize > 0) {
                int y = 0;
                while (y < tempPgmsSize) {
                    // Merge program traces
                    JSONObject tempPgm = tempPgms.optJSONObject(y);
                    // tempPgm.put("timestamp", tempObj.get("timestamp"));
                    String id = tempPgm.optString("id");

                    if (!programsToAgg.containsKey(id)) {// No aggregation for
                        // now

                        programsToAgg.put(id, tempPgm);

                    } else { // program id exist for this time stamp -->
                        // aggregation

                        JSONObject existingPgm = programsToAgg.get(id);
                        if (tempPgm.has("event")) {
                            try {
                            // replace the state by the
                                // last known state
                                existingPgm.put("event", tempPgm.opt("event"));
                            } catch (JSONException ex) {
                            }
                        }

                        // Aggregates the device trace has a decoration
                        JSONArray existingDecorations = existingPgm.optJSONArray("decorations");
                        JSONArray tempDecs = tempPgm.optJSONArray("decorations");
                        int decSize = tempDecs.length();
                        int y1 = 0;
                        while (y1 < decSize) {
                            JSONObject tempDec = tempDecs.optJSONObject(y1);
                            try {
                                tempDec.put("order", existingDecorations.length());
                            } catch (JSONException ex) {
                            }
                            existingDecorations.put(tempDec);
                            y1++;
                        }
                    }
                    y++;
                }
            }
            i++;
        }
        try {
            jsonTrace.put("devices", new JSONArray(devicesToAgg.values()));
            jsonTrace.put("programs", new JSONArray(programsToAgg.values()));
            aggregateTraces.put(jsonTrace);
        } catch (JSONException ex) {
        }
    }

    /**
     * Is this thread infinitely sleeping
     *
     * @return true if the thread is waiting till the end of time, false
     * otherwise
     */
    public synchronized boolean isSleeping() {
        return sleeping;
    }

    /**
     * Get the parent trace queue
     *
     * @return the traceQueue instance associated to this TraceExecutor
     */
    public TraceQueue getTraceQueue() {
        return traceQueue;
    }

}
