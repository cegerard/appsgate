package appsgate.lig.eude.interpreter.references;

import appsgate.lig.eude.interpreter.references.ReferenceTable.STATUS;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author jr
 */
class Reference {

    /**
     * id : Id of the device referenced
     */
    private final String id;

    /**
     * status : STATUS of the device
     */
    private STATUS status;

    /**
     * the default name of the device
     */
    private final String defaultName;
    /**
     * referencesData : Hashmap for the information about reference (ie Type,
     * name)
     */
    private final ArrayList<HashMap<String, String>> referencesData;

    public Reference(String deviceId, STATUS deviceStatus, String name, ArrayList<HashMap<String, String>> referencesData) {
        this.id = deviceId;
        this.status = deviceStatus;
        if (referencesData != null) {
            this.referencesData = referencesData;
        } else {
            this.referencesData = new ArrayList<HashMap<String, String>>();
        }
        this.defaultName = name;
    }

    public final String getDefaultName() {
        return this.defaultName;
    }

    public String getId() {
        return id;
    }

    public void setStatus(STATUS deviceStatus) {
        this.status = deviceStatus;
    }

    public STATUS getStatus() {
        return status;
    }

    public ArrayList<HashMap<String, String>> getReferencesData() {
        return referencesData;
    }

    public void addReferencesData(HashMap<String, String> newData) {
        this.referencesData.add(newData);
    }

}
