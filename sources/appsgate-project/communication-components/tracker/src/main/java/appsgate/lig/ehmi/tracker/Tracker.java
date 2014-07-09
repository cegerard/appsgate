/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.ehmi.tracker;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
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
     *
     * @param t
     * @return
     */
    private long getSecond(long t) {
        return t / (long) 1000 * 1000;
    }

    /**
     * @param t a tracker entry
     */
    public void addEntry(TrackerEntry t) {
        long time = t.getTimestamp();
        //
        JSONObject o = new JSONObject();
        entries.put(time, t);
        try {
            JSONArray a = new JSONArray();
            a.put(t.getJSON());
            o.put("devices", a);
            o.put("programs", new JSONArray());
            o.put("timestamp", time);
            String s = o.toString();
            fileJSON.println(o.toString() + ",");
        } catch (JSONException ex) {
        }
        sendToClientService.send("Tracker", o);

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
    public void gotNotification(NotificationMsg notif) {
        // Adding the devices
        if (notif.getSource().getCoreType() == CoreObjectSpec.CORE_TYPE.DEVICE) {
            String type = ((CoreObjectBehavior) notif.getSource()).getTypeFromGrammar();
            addEntry(new TrackerEntry(clock.getCurrentTimeInMillis(), type, notif.getSource().getAbstractObjectId(), true, notif.getNewValue(), ""));
        }
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
        this.fileJSON.close();
    }
}
