/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.eude.interpreter.spec;

/**
 *
 * @author jr
 */
public class ProgramDeviceStateNotification extends ProgramTraceNotification{

    public ProgramDeviceStateNotification(ProgramDesc source, String nodeId, String targetId, String desc, String result) {
        super(source, nodeId, source.getId(), targetId, desc, Type.READ);
        this.setResult(result);
    }

    
}
