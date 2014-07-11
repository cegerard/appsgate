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
public class DeviceEntry extends TrackerEntry {

    private final String classId;
    private final String value;


    /**
     * Constructor
     *
     * @param t the timestamp
     * @param cid the class id of the equipment
     * @param oid the object id of the equipment
     * @param val the value of the event
     */
    public DeviceEntry(long t, String cid, String oid, String val) {
        super(t, oid);
        this.classId = cid;
        this.value = val;
    }


    public String getClassId() {
        return classId;
    }



    public String getId() {
        return classId + ":" + this.getObjectId();
    }


    /**
     *
     * @return
     */
    @Override
    public JSONObject getJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", this.getObjectId());
            o.put("name", "toto");
            o.put("type", this.classId);
            o.put("state", this.value);
            o.put("event", getEvent());

        } catch (JSONException ex) {
        }
        return o;
    }

}
