/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.context.dependency.spec;

import java.util.ArrayList;

/**
 * Class used to store data about the device's references.
 *
 * @author bidoismorgan
 */
public class DeviceReference extends Reference {

    //
    private final String type;
    
    public DeviceReference(String deviceId, STATUS deviceStatus, String name, ArrayList<ReferenceDescription> referencesData, String type) {
        super(deviceId, deviceStatus, name, referencesData);
        this.type = type;
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

    public String getType() {
        return type;
    }


}
