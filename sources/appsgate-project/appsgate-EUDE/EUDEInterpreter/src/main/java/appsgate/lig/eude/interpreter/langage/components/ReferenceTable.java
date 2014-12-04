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

    /**
     *
     */
    private final HashMap<String, STATUS> devices;
    /**
     *
     */
    private final HashMap<String, STATUS> programs;

    /**
     *
     */
    private final ArrayList<NodeSelect> nodes;

    /**
     *
     * @param interpreter
     * @param pid
     */
    public ReferenceTable(EUDEInterpreter interpreter, String pid) {
        devices = new HashMap<String, STATUS>();
        programs = new HashMap<String, STATUS>();
        nodes = new ArrayList<NodeSelect>();
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
        devices.put(deviceId, STATUS.UNKNOWN);
    }

    /**
     * @param aThis
     */
    public void addNodeSelect(NodeSelect aThis) {
        nodes.add(aThis);
    }

    /**
     *
     * @param deviceId
     * @param newStatus
     */
    public void setDeviceStatus(String deviceId, STATUS newStatus) {
        if (devices.containsKey(deviceId)) {
            devices.put(deviceId, newStatus);
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

    /**
     *
     * @return
     */
    public Set<String> getDevicesId() {
        return devices.keySet();
    }

    public ArrayList<NodeSelect> getSelectors() {
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
            LOGGER.trace("retrieveReferences(), program " + k + ", status " + devices.get(k));
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
        for (String k : devices.keySet()) {
            LOGGER.trace("retrieveReferences(), device " + k + ", status " + devices.get(k));
            JSONObject device = interpreter.getContext().getDevice(k);
            try {
                if (device == null || !device.has("status") || !device.getString("status").equals("2")) {
                    LOGGER.warn("The device {} is missing.", k);
                    setDeviceStatus(k, STATUS.MISSING);
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
                    break;
                case INVALID:
                case UNSTABLE:
                    setState(STATUS.UNSTABLE);
                    break;
            }
        }
        // Services && devices are treated the same way
        for (String k : devices.keySet()) {
            LOGGER.trace("computeStatus(), device " + k + ", status " + devices.get(k));
            switch (devices.get(k)) {
                case MISSING:
                    setState(STATUS.UNSTABLE);
                    break;
            }
        }
        for (NodeSelect s : nodes) {
            if (s.isEmptySelection()) {
                LOGGER.warn("Select node {} is empty.", s);
                setState(STATUS.UNSTABLE);
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

}
