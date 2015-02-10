/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.langage.components.ReferenceTable.STATUS;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class used to store data about the device's references.
 *
 * @author bidoismorgan
 */
public class DeviceReferences {

    /**
     * deviceId : Id of the device referenced
     */
    private final String deviceId;

    /**
     * deviceStatus : STATUS of the device
     */
    private STATUS deviceStatus;

    /**
     * referencesData : Hashmap for the information about reference (ie Type,
     * name)
     */
    private final ArrayList<HashMap<String, String>> referencesData;

    public DeviceReferences(String deviceId, STATUS deviceStatus, ArrayList<HashMap<String, String>> referencesData) {
        this.deviceId = deviceId;
        this.deviceStatus = deviceStatus;
        if (referencesData != null) {
            this.referencesData = referencesData;
        } else {
            this.referencesData = new ArrayList<HashMap<String, String>>();
        }

    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceStatus(STATUS deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public STATUS getDeviceStatus() {
        return deviceStatus;
    }

    public ArrayList<HashMap<String, String>> getReferencesData() {
        return referencesData;
    }

    public void addReferencesData(HashMap<String, String> newData) {
        this.referencesData.add(newData);
    }

}
