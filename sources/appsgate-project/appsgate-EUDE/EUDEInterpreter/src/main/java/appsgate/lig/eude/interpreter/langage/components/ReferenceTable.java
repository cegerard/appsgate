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
import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class ReferenceTable {

    /**
     * Static class member uses to log what happened in each instances
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceTable.class);

    /**
     *
     */
    private final EUDEInterpreter interpreter;
    /**
     *
     */
    private final String myProgId;

    /**
     *
     */
    private STATUS state;
    
    private JSONObject err;

    /**
     *
     * @return
     */
    public STATUS getStatus() {
        return this.state;
    }

    public enum STATUS {

        OK,
        UNSTABLE,
        INVALID,
        MISSING,
        UNKNOWN
    };
    
    public enum REFERENCE_TYPE {
        WRITING,
        READING
    };

    /**
     *
     */
    private final ArrayList<DeviceReferences> devices;
    /**
     *
     */
    private final HashMap<String, STATUS> programs;

    /**
     *
     */
    private final ArrayList<SelectReferences> nodes;

    /**
     *
     * @param interpreter
     * @param pid
     */
    public ReferenceTable(EUDEInterpreter interpreter, String pid) {
        devices = new ArrayList<DeviceReferences>();
        programs = new HashMap<String, STATUS>();
        nodes = new ArrayList<SelectReferences>();
        this.interpreter = interpreter;
        this.state = STATUS.UNKNOWN;
        this.myProgId = pid;
    }

    /**
     *
     * @param programId
     */
    public void addProgram(String programId) {
        if (programId.equalsIgnoreCase(myProgId)) {
            LOGGER.debug("The program is self referenced (do not need to add it in the reference table.");
            return;
        }
        programs.put(programId, STATUS.UNKNOWN);
    }

    /**
     *
     * @param deviceId
     */
    public void addDevice(String deviceId) {
        devices.add(new DeviceReferences(deviceId, STATUS.UNKNOWN));
    }
    
     /**
     *
     * @param deviceId
     * @param refData - data about reference
     */
    public void addDevice(String deviceId, HashMap<String,String> refData) {
        DeviceReferences dRef = getDeviceFromId(deviceId);
        if (dRef != null) {
            dRef.addReferencesData(refData);
        } else {
            ArrayList<HashMap<String,String>> newRefData = new ArrayList<HashMap<String, String>>();
            newRefData.add(refData);
            devices.add(new DeviceReferences(deviceId, STATUS.UNKNOWN, newRefData));
        }
        
    }

    /**
     * @param aThis
     * @param refData - data about reference
     */
    public void addNodeSelect(NodeSelect aThis, HashMap<String,String> refData) {
        boolean refDataAdded = false;
        for (SelectReferences sRef : this.nodes) {
            if (sRef.getNodeSelect() == aThis) {
                refDataAdded = sRef.addReferencesData(refData);
            }
        }
        if (!refDataAdded) {
            ArrayList<HashMap<String,String>> newRefData = new ArrayList<HashMap<String, String>>();
            newRefData.add(refData);
            nodes.add(new SelectReferences(aThis, newRefData));
        }
    }

    /**
     *
     * @param deviceId
     * @param newStatus
     */
    public void setDeviceStatus(String deviceId, STATUS newStatus) {
        for (DeviceReferences device : this.devices) {
            if (device.getDeviceId().equals(deviceId)) {
                device.setDeviceStatus(newStatus);
            }
        }
    }

    /**
     *
     * @param programId
     * @param newStatus
     */
    public void setProgramStatus(String programId, STATUS newStatus) { // VOID
        if (programs.containsKey(programId)) {
            programs.put(programId, newStatus);
        }
    }


    /**
     *
     * @return
     */
    public Set<String> getProgramsId() {
        return programs.keySet();
    }
    
    public ArrayList<DeviceReferences> getDevicesReferences() {
        return this.devices;
    }

    /**
     *
     * @return
     */
    public Set<String> getDevicesId() {
        Set<String> devicesId = new HashSet<String>();
        for (DeviceReferences device : this.devices) {
            devicesId.add(device.getDeviceId());
        }
        return devicesId;
    }
    
    public DeviceReferences getDeviceFromId (String id) {
        for (DeviceReferences device : this.devices) {
            if (device.getDeviceId().equals(id)) {
                return device;
            }
        }
        return null;
    }

    public ArrayList<SelectReferences> getSelectors() {
        return nodes;
    }

    
    /**
     *
     * @param status
     */
    private void setState(STATUS status) {
        if (status == STATUS.INVALID) {
            this.state = status;
        } else if (status == STATUS.UNSTABLE && this.state == STATUS.OK) {
            this.state = status;
        }
    }

    /**
     *
     */
    private void retrieveReferences() {
        for (String k : programs.keySet()) {
            NodeProgram prog = interpreter.getNodeProgram(k);
            LOGGER.trace("retrieveReferences(), program " + k + ", status " + programs.get(k));
            if (prog != null) {
                if (!prog.isValid()) {
                    LOGGER.error("The program {} is not valid.", k);
                    setProgramStatus(k, STATUS.INVALID);
                } else if (prog.getReferences().state != STATUS.OK) {
                    LOGGER.warn("The program {} is not stable.", k);
                    setProgramStatus(k, STATUS.UNSTABLE);
                }
            } else {
                LOGGER.error("The program {} does not exist anymore.", k);
                setProgramStatus(k, STATUS.MISSING);
            }
        }
        // Services && devices are treated the same way
        for (DeviceReferences device : devices) {
            LOGGER.trace("retrieveReferences(), device " + device.getDeviceId() + ", status " + device.getDeviceStatus());
            JSONObject deviceJSON = interpreter.getContext().getDevice(device.getDeviceId());
            try {
                if (deviceJSON == null || !deviceJSON.has("status") || !deviceJSON.getString("status").equals("2")) {
                    LOGGER.warn("The device {} is missing.", device.getDeviceId());
                    setDeviceStatus(device.getDeviceId(), STATUS.MISSING);
                }
            } catch (JSONException ex) {
            }
        }

    }

    /**
     *
     * @return
     */
    public STATUS computeStatus() {
        this.err = new JSONObject();
        this.state = STATUS.OK;
        if (programs.size() + devices.size() + nodes.size() == 0) {
            LOGGER.trace("The table is empty, so the program is empty and no empty program is considered as valid");
            this.state=STATUS.INVALID;
        }
        for (String k : programs.keySet()) {
            LOGGER.trace("computeStatus(), program " + k + ", status :" + programs.get(k));
            switch (programs.get(k)) {
                case MISSING:
                    setState(STATUS.INVALID);
                    this.err = ErrorMessagesFactory.getMessageFromMissingProgram(k);
                    return this.state;
                case INVALID:
                case UNSTABLE:
                    this.err = ErrorMessagesFactory.getMessageFromInvalidProgram(k);
                    setState(STATUS.UNSTABLE);
                    break;
            }
        }
        // Services && devices are treated the same way
        for (DeviceReferences device : devices) {
            LOGGER.trace("computeStatus(), device " + device.getDeviceId() + ", status " + device.getDeviceStatus());
            switch (device.getDeviceStatus()) {
                case MISSING:
                    this.err = ErrorMessagesFactory.getMessageFromMissingDevice(device.getDeviceId());
                    setState(STATUS.UNSTABLE);
                    break;
            }
        }
        for (SelectReferences s : nodes) {
            if (s.getNodeSelect().isEmptySelection()) {
                LOGGER.warn("Select node {} is empty.", s.getNodeSelect());
                setState(STATUS.UNSTABLE);
                this.err = ErrorMessagesFactory.getMessageFromEmptySelect(s.getNodeSelect());
            }
        }
        return this.state;
    }

    /**
     *
     * @return
     */
    public STATUS checkReferences() {
        retrieveReferences();
        return computeStatus();
    }

    
    /**
     * 
     * @return the error message if any
     */
    public JSONObject getErrorMessage() {
        return this.err;
    }
}
