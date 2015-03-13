package appsgate.lig.eude.interpreter.references;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * Class used to store data about the program's references.
 *
 * @author bidoismorgan
 */
public class ProgramReference extends Reference {

    public ProgramReference(String pid, ReferenceTable.STATUS status, String name, ArrayList<HashMap<String, String>> referencesData) {
        super(pid, status, name, referencesData);
    }

    /**
     * @return the program id
     */
    public String getProgramId() {
        return getId();
    }

    /**
     *
     * @param programStatus
     * @return true if the programStatus has changed
     */
    public Boolean setProgramStatus(ReferenceTable.STATUS programStatus) {
        if (this.getStatus() == programStatus) {
            return false;
        }
        this.setStatus(programStatus);
        return true;
    }

    public ReferenceTable.STATUS getProgramStatus() {
        return getStatus();
    }

}
