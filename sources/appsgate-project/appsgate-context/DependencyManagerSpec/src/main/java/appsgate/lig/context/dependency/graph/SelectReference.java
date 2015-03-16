package appsgate.lig.context.dependency.graph;

import java.util.ArrayList;

/**
 * Class used to store data about the selector's references.
 * 
 * @author bidoismorgan
 */
public class SelectReference {
    
    /**
     * nodeSelect of the reference
     */
    private final Selector nodeSelect;
    
    /**
     * referencesData :  for the information about reference (ie Type, name)
     */
    private final ArrayList<ReferenceDescription> referencesData;

    public SelectReference(Selector nodeSelect, ArrayList<ReferenceDescription> referencesData) {
        this.nodeSelect = nodeSelect;
        this.referencesData = referencesData;
    }

    public Selector getNodeSelect() {
        return nodeSelect;
    }

    public ArrayList<ReferenceDescription> getReferencesData() {
        return referencesData;
    }
    
    public boolean addReferencesData(ReferenceDescription newRef) {
        return this.referencesData.add(newRef);
    }
    
}