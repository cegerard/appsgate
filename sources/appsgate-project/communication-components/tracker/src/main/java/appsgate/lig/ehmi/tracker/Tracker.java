/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.ehmi.tracker;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class Tracker {

    /**
     * The logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(Tracker.class);

    /**
     *
     */
    HashMap<Long, TrackerEntry> entries;

    private PrintWriter fileJSON;

    private CoreClockSpec clock;

    private SendWebsocketsService sendToClientService;

    /**
     * @param t a tracker entry
     */
    private void addEntry(TrackerEntry t) {
        long time = t.getTimestamp();
        JSONArray devices = new JSONArray();
        JSONArray programs = new JSONArray();
        if (t.getClass().equals(DeviceEntry.class)) {
            devices.put(t.getJSON());
        }
        if (t.getClass().equals(ProgramEntry.class)) {
            programs.put(t.getJSON());
        }
        
        entries.put(time, t);
        JSONObject o = formatAnswer(time, devices, programs);
        if (fileJSON != null) {
            fileJSON.println(o.toString() + ",");
        }
        sendToClientService.send("Tracker", o);
    }

    /**
     *
     * @param time
     * @param devices
     * @param programs
     * @return
     */
    private JSONObject formatAnswer(long time, JSONArray devices, JSONArray programs) {
        JSONObject o = new JSONObject();
        try {
            o.put("devices", devices);
            o.put("programs", programs);
            o.put("timestamp", time);
        } catch (JSONException ex) {
        }
        return o;

    }

    /**
     *
     * @param timeStart
     * @param timeEnd
     * @return
     */
    protected JSONObject getLogs(long timeStart, long timeEnd) {
        return new JSONObject();
    }

    /**
     *
     * @param clientId
     * @param timeStart
     * @param timeEnd
     */
    public void sendLogsToClient(int clientId, long timeStart, long timeEnd) {
        sendToClientService.send(clientId, "Tracker", getLogs(timeStart, timeEnd));
    }

    /**
     *
     * @param notif
     */
    public void gotNotification(appsgate.lig.core.object.messages.NotificationMsg notif) {
        // Adding the devices
        if (notif.getSource().getCoreType() == CoreObjectSpec.CORE_TYPE.DEVICE) {
            //String type = ((CoreObjectBehavior) notif.getSource()).getTypeFromGrammar();
            String type = "undefined";
//            String type = ((CoreObjectBehavior) notif.getSource()).getTypeFromGrammar();
            addEntry(new DeviceEntry(clock.getCurrentTimeInMillis(), type, notif.getSource().getAbstractObjectId(), notif.getNewValue()));
        }
    }

    /**
     *
     * @param notif
     */
    public void gotEHMINotification(appsgate.lig.ehmi.spec.messages.NotificationMsg notif) {
        // Adding the devices
        addEntry(new ProgramEntry(clock.getCurrentTimeInMillis(), notif.getSource(), notif.getVarName(), notif.getNewValue()));
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void newInst() {
        try {
            entries = new HashMap<Long, TrackerEntry>();
            this.fileJSON = new PrintWriter("/Users/jr/Desktop/remi.json");
        } catch (FileNotFoundException ex) {
            this.fileJSON = null;
            LOGGER.error("Unable to open remi.json file");
        }
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void deleteInst() {
        if (this.fileJSON != null) {
            this.fileJSON.close();
            this.fileJSON = null;
        }
    }
}
