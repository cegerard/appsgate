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
public class TrackerEntry {

    private final Long timestamp;
    private final String classId;
    private final String objectId;
    private final String value;
    private String cause;


    /**
     * Constructor
     *
     * @param t the timestamp
     * @param cid the class id of the equipment
     * @param oid the object id of the equipment
     * @param pre the presence or absence of the equipment
     * @param val the value of the event
     * @param c the cause
     */
    public TrackerEntry(long t, String cid, String oid, boolean pre, String val, String c) {
        this.timestamp = t;
        this.classId = cid;
        this.objectId = oid;
        this.cause = c;
        
        this.value = val;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getClassId() {
        return classId;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getCause() {
        return cause;
    }

    public String getId() {
        return classId + ":" + objectId;
    }


    /**
     *
     * @return
     */
    public JSONObject getJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", this.objectId);
            o.put("name", "toto");
            o.put("type", this.classId);
            o.put("state", this.value);
            JSONObject event = new JSONObject();
            JSONObject causality = new JSONObject();
            causality.put("type", "technical");
            causality.put("description", this.cause);
            event.put("eventType", "value");
            event.put("causality", causality);
            o.put("event", event);

        } catch (JSONException ex) {
        }
        return o;
    }

}
