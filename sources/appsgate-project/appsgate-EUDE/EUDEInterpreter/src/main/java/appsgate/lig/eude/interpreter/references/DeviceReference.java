/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.references;

import appsgate.lig.eude.interpreter.references.ReferenceTable.STATUS;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class used to store data about the device's references.
 *
 * @author bidoismorgan
 */
public class DeviceReference extends Reference {


    public DeviceReference(String deviceId, STATUS deviceStatus, String name, ArrayList<HashMap<String, String>> referencesData) {
        super(deviceId, deviceStatus, name, referencesData);
    }
    

    public String getDeviceId() {
        return getId();
    }

    public void setDeviceStatus(STATUS deviceStatus) {
        setStatus(deviceStatus);
    }

    public STATUS getDeviceStatus() {
        return getStatus();
    }


}
