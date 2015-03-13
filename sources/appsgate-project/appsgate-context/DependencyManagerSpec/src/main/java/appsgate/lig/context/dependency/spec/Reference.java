package appsgate.lig.context.dependency.spec;

import java.util.ArrayList;

/**
 *
 * @author jr
 */
public class Reference  {

    public enum STATUS {

        OK,
        UNSTABLE,
        INVALID,
        MISSING,
        UNKNOWN
    };
    
    public enum REFERENCE_TYPE {
        WRITING,
        READING
    };
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
     * referencesData : Hashmap for the information about reference (ie Type, name)
     */
    private final ArrayList<ReferenceDescription> referencesData;

    public Reference(String deviceId, STATUS deviceStatus, String name, ArrayList<ReferenceDescription> referencesData) {
        this.id = deviceId;
        this.status = deviceStatus;
        if (referencesData != null) {
            this.referencesData = referencesData;
        } else {
            this.referencesData = new ArrayList<ReferenceDescription>();
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

    public ArrayList<ReferenceDescription> getReferencesData() {
        return referencesData;
    }

    public void addReferencesData(ReferenceDescription newData) {
        this.referencesData.add(newData);
    }
    public boolean hasMethod(REFERENCE_TYPE refType) {
        for (ReferenceDescription d: referencesData){
            if(d.isReferenceType(refType))
                return true;
        }
        return false;
    }

}
