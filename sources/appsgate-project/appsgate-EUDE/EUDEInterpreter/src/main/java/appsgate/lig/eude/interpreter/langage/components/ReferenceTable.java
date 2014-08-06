/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import java.util.HashMap;
import java.util.Set;
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

    private final EUDEInterpreter interpreter;

    private final HashMap<String, Boolean> devices;
    private final HashMap<String, Boolean> programs;

    /**
     *
     * @param interpreter
     */
    public ReferenceTable(EUDEInterpreter interpreter) {
        devices = new HashMap<String, Boolean>();
        programs = new HashMap<String, Boolean>();
        this.interpreter = interpreter;
    }
    
    public void addProgram(String programId) {
        programs.put(programId, null);
    }
    public void addDevice(String deviceId) {
        devices.put(deviceId, null);
    }

    public void setDeviceStatus(String deviceId, Boolean status) {
        devices.put(deviceId, status);

    }

    public void setProgramStatus(String programId, Boolean status) {
        programs.put(programId, status);

    }

    public Set<String> getProgramsId() {
        return programs.keySet();
    }

    public Set<String> getDevicesId() {
        return devices.keySet();
    }

    public Boolean parseReferenceOK() {
        Boolean isOK = true;
        for (String k : programs.keySet()){
            if (programs.get(k) == null) {
                LOGGER.error("Trying to parse the reference while they are not all calculated");
                LOGGER.debug("Stoping at key : {}", k);
                return null;
            }
            if (!programs.get(k)) {
                LOGGER.warn("Program is invalid cause");
                isOK = false;
            }
        }
        return isOK;
    }
}
