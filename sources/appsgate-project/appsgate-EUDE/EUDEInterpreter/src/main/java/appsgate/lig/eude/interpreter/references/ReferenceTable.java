/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.references;

import appsgate.lig.context.dependency.spec.Dependencies;
import appsgate.lig.context.dependency.graph.DeviceReference;
import appsgate.lig.context.dependency.graph.ProgramReference;
import appsgate.lig.context.dependency.graph.Reference;
import appsgate.lig.context.dependency.graph.Reference.STATUS;
import appsgate.lig.context.dependency.graph.ReferenceDescription;
import appsgate.lig.context.dependency.graph.SelectReference;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import appsgate.lig.eude.interpreter.langage.components.ErrorMessagesFactory;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.eude.interpreter.langage.nodes.NodeSelect;
import appsgate.lig.eude.interpreter.spec.ProgramDesc;
import java.util.ArrayList;
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
public class ReferenceTable extends Dependencies {

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
    private STATUS state;
    
    private JSONObject err;

    /**
     *
     * @return
     */
    public STATUS getStatus() {
        return this.state;
    }


    /**
     *
     */
    private final Set<DeviceReference> devices;
    /**
     *
     */
    private final Set<ProgramReference> programs;

    /**
     *
     */
    private final Set<SelectReference> nodes;

    /**
     *
     * @param interpreter
     * @param pid
     */
    public ReferenceTable(EUDEInterpreter interpreter, String pid) {
        super(pid);
        devices = new HashSet<DeviceReference>();
        programs = new HashSet<ProgramReference>();
        nodes = new HashSet<SelectReference>();
        this.interpreter = interpreter;
        this.state = STATUS.UNKNOWN;
    }

    /**
     *
     * @param programId
     * @param name
     * @param refData - data about reference
     */
    public void addProgram(String programId, String name, ReferenceDescription refData) {
        if (programId.equalsIgnoreCase(getId())) {
            LOGGER.debug("The program is self referenced (do not need to add it in the reference table.");
            return;
        }
        
        ProgramReference pRef = getProgramFromId(programId);
        if (pRef != null) {
            pRef.addReferencesData(refData);
        } else {
            ArrayList<ReferenceDescription> newRefData = new ArrayList<ReferenceDescription>();
            newRefData.add(refData);
            programs.add(new ProgramReference(programId, STATUS.UNKNOWN, name, newRefData));
        }
    }

    /**
     *
     * @param deviceId
     * @param deviceName
     */
    public void addDevice(String deviceId, String deviceName) {
        
        devices.add(new DeviceReference(deviceId, STATUS.UNKNOWN, deviceName, null, getDeviceType(deviceId)));
    }
    
     /**
     *
     * @param deviceId
     * @param deviceName
     * @param refData - data about reference
     */
    public void addDevice(String deviceId, String deviceName, ReferenceDescription refData) {
        DeviceReference dRef = getDeviceFromId(deviceId);
        if (dRef != null) {
            dRef.addReferencesData(refData);
        } else {
            ArrayList<ReferenceDescription> newRefData = new ArrayList<ReferenceDescription>();
            newRefData.add(refData);
            devices.add(new DeviceReference(deviceId, STATUS.UNKNOWN, deviceName, newRefData, getDeviceType(deviceId)));
        }
        
    }

    /**
     * @param aThis
     * @param refData - data about reference
     */
    public void addNodeSelect(NodeSelect aThis, ReferenceDescription refData) {
        boolean refDataAdded = false;
        for (SelectReference sRef : this.nodes) {
            if (sRef.getNodeSelect() == aThis) {
                refDataAdded = sRef.addReferencesData(refData);
            }
        }
        if (!refDataAdded) {
            ArrayList<ReferenceDescription> newRefData = new ArrayList<ReferenceDescription>();
            newRefData.add(refData);
            nodes.add(new SelectReference(aThis, newRefData));
        }
    }

    /**
     *
     * @param deviceId
     * @param newStatus
     */
    public void setDeviceStatus(String deviceId, STATUS newStatus) {
        for (DeviceReference device : this.devices) {
            if (device.getDeviceId().equals(deviceId)) {
                device.setDeviceStatus(newStatus);
            }
        }
    }

    /**
     *
     * @param programId
     * @param newStatus
     * @return true if the program referenced has changed of status
     */
    public Boolean setProgramStatus(String programId, STATUS newStatus) { // VOID
        for (ProgramReference pRef : this.programs) {
            if (pRef.getProgramId().equals(programId)) {
                return pRef.setProgramStatus(newStatus);
            }
        }
        return false;
    }


    /**
     *
     * @return
     */
    public Set<String> getProgramsId() {
        Set<String> programsId = new HashSet<String>();
        for (ProgramReference pRef : this.programs) {
            programsId.add(pRef.getProgramId());
        }
        return programsId;
    }
    
     public ProgramReference getProgramFromId (String id) {
        for (ProgramReference pRef : this.programs) {
            if (pRef.getProgramId().equals(id)) {
                return pRef;
            }
        }
        return null;
    }
    /**
     * 
     * @return 
     */
    @Override
    public Set<DeviceReference> getDevicesReferences() {
        return this.devices;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public Set<ProgramReference> getProgramsReferences() {
        return this.programs;
    }

    /**
     *
     * @return
     */
    public Set<String> getDevicesId() {
        Set<String> devicesId = new HashSet<String>();
        for (DeviceReference device : this.devices) {
            devicesId.add(device.getDeviceId());
        }
        return devicesId;
    }
    
    public DeviceReference getDeviceFromId (String id) {
        for (DeviceReference device : this.devices) {
            if (device.getDeviceId().equals(id)) {
                return device;
            }
        }
        return null;
    }
    
    /**
     *
     * @return
     */
    @Override
    public Set<SelectReference> getSelectors() {
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
        for (ProgramReference pRef : programs) {
            NodeProgram prog = interpreter.getNodeProgram(pRef.getProgramId());
            LOGGER.trace("retrieveReferences(), program " + pRef.getProgramId() + ", status " + pRef.getProgramStatus());
            if (prog != null) {
                if (!prog.isValid()) {
                    LOGGER.error("The program {} is not valid.", pRef.getProgramId());
                    setProgramStatus(pRef.getProgramId(), STATUS.INVALID);
                } else if (prog.getReferences().state != STATUS.OK) {
                    LOGGER.warn("The program {} is not stable.", pRef.getProgramId());
                    setProgramStatus(pRef.getProgramId(), STATUS.UNSTABLE);
                }
            } else {
                LOGGER.error("The program {} does not exist anymore.", pRef.getProgramId());
                setProgramStatus(pRef.getProgramId(), STATUS.MISSING);
            }
        }
        // Services && devices are treated the same way
        for (DeviceReference device : devices) {
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
            this.err = ErrorMessagesFactory.getEmptyProgramMessage();
            this.state=STATUS.INVALID;
        }
        // Services && devices are treated the same way
        for (DeviceReference device : devices) {
            LOGGER.trace("computeStatus(), device " + device.getDeviceId() + ", status " + device.getDeviceStatus());
            switch (device.getDeviceStatus()) {
                case MISSING:
                    this.err = ErrorMessagesFactory.getMessageFromMissingDevice(device.getDeviceId());
                    setState(STATUS.UNSTABLE);
                    break;
            }
        }
        for (SelectReference s : nodes) {
            if (s.getNodeSelect().isEmptySelection()) {
                LOGGER.warn("Select node {} is empty.", s.getNodeSelect());
                setState(STATUS.UNSTABLE);
                this.err = ErrorMessagesFactory.getMessageFromEmptySelect(s.getNodeSelect());
            }
        }
        for (ProgramReference pRef : programs) {
            LOGGER.trace("computeStatus(), program " + pRef.getProgramId() + ", status :" + pRef.getProgramStatus());
            switch (pRef.getProgramStatus()) {
                case MISSING:
                    setState(STATUS.INVALID);
                    this.err = ErrorMessagesFactory.getMessageFromMissingProgram(pRef.getProgramId());
                    return this.state;
                case INVALID:
                    this.err = ErrorMessagesFactory.getMessageFromInvalidProgram(pRef.getProgramId());
                    setState(STATUS.UNSTABLE);
                    break;
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
    
    public static STATUS getProgramStatus(ProgramDesc.PROGRAM_STATE runningState) {
        switch(runningState){
            case DEPLOYED:
            case PROCESSING:
                return STATUS.OK;
            case INCOMPLETE:
            case LIMPING:
                return STATUS.UNSTABLE;
            case INVALID:
                return STATUS.INVALID;
            default:
                return STATUS.UNKNOWN;
        }
    }

    /**
     * @param deviceId
     * @return 
     */
    private String getDeviceType(String deviceId) {
        JSONObject device = this.interpreter.getContext().getDevice(deviceId);
        return device.optString("type");
    }

    @Override
    public Set<String> getActsOnEntities() {
        HashSet ret = new HashSet<String>();
        for (DeviceReference d :devices) {
            if (d.hasMethod(Reference.REFERENCE_TYPE.WRITING)) {
                ret.add(d.getDeviceId());
            }
        }
        return ret;
    }

    @Override
    public Set<String> getReadedEntities() {
        HashSet ret = new HashSet<String>();
        for (DeviceReference d :devices) {
            if (d.hasMethod(Reference.REFERENCE_TYPE.READING)) {
                ret.add(d.getDeviceId());
            }
        }
        return ret;
    }

    @Override
    public Set<String> getEntitiesActsOn() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> getEntitiesRead() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
