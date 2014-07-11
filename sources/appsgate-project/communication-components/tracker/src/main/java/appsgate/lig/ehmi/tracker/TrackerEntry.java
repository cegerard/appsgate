/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.ehmi.tracker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public abstract class TrackerEntry {

    private final Long timestamp;
    private final String objectId;
    private final String cause = "";
    private final String eventType = "update";
    private final String causeType = "technical";

    public TrackerEntry(long time, String id) {
        this.timestamp = time;
        this.objectId = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getCause() {
        return cause;
    }

    public abstract JSONObject getJSON();

    /**
     *
     * @return
     */
    protected JSONObject getEvent() {
        JSONObject event = new JSONObject();
        try {
            JSONObject causality = new JSONObject();
            causality.put("type", causeType);
            causality.put("description", cause);
            event.put("type", eventType);
            event.put("causality", causality);
        } catch (JSONException ex) {

        }
        return event;

    }

}
