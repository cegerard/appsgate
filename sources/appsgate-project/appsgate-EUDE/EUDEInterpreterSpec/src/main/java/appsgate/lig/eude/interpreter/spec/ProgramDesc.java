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

    /**
     *
     * @return the program name
     */
    String getProgramName();

    /**
     *
     * @return the state of the program
     * INVALID/DEPLOYED/PROCESSING/INCOMPLETE/LIMPING
     */
    PROGRAM_STATE getState();

    /**
     * @return the ID of the program
     */
    String getId();

    /**
     * @return the json description of the program
     */
    JSONObject getJSONDescription();
}
