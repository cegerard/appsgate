package appsgate.lig.context.dependency.graph;

import java.util.ArrayList;

/**
 *
 * Class used to store data about the program's references.
 *
 * @author bidoismorgan
 *
 */
public class ProgramReference extends Reference {

    public ProgramReference(String pid, STATUS status, String name, ArrayList<ReferenceDescription> referencesData) {
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
    public Boolean setProgramStatus(STATUS programStatus) {
        if (this.getStatus() == programStatus) {
            return false;
        }
        this.setStatus(programStatus);
        return true;
    }

    public STATUS getProgramStatus() {
        return getStatus();
    }

}
