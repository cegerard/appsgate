/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.eude.interpreter.spec;

import org.json.JSONObject;

/**
 *
 * @author jr
 */
public interface ProgramDesc {
        /**
     * Program running state static enumeration
     *
     * @author Cédric Gérard
     * @since September 13, 2013
     */
    public static enum PROGRAM_STATE {

        INVALID("INVALID"), DEPLOYED("DEPLOYED"), PROCESSING("PROCESSING"),
        INCOMPLETE("INCOMPLETE"), LIMPING("LIMPING");

        private String name = "";

        PROGRAM_STATE(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    String getProgramName();
    
    PROGRAM_STATE getState();
    
    String getId();
    
    JSONObject getJSONDescription();
}
