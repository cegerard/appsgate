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
public class ProgramEventNotification extends ProgramTraceNotification{

    public ProgramEventNotification(ProgramDesc source, String nodeId, String targetId, String desc) {
        super(source, nodeId, targetId, source.getId(), desc, Type.WRITE);
    }
    
}
