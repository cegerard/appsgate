/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.langage.nodes.NodeSelect;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class used to store data about the selector's references.
 * 
 * @author bidoismorgan
 */
public class SelectReferences {
    
    /**
     * nodeSelect of the reference
     */
    private NodeSelect nodeSelect;
    
    /**
     * referencesData : Hashmap for the information about reference (ie Type, name)
     */
    private ArrayList<HashMap<String,String>> referencesData;

    public SelectReferences(NodeSelect nodeSelect, ArrayList<HashMap<String, String>> referencesData) {
        this.nodeSelect = nodeSelect;
        this.referencesData = referencesData;
    }

    public NodeSelect getNodeSelect() {
        return nodeSelect;
    }

    public ArrayList<HashMap<String, String>> getReferencesData() {
        return referencesData;
    }
    
    public boolean addReferencesData(HashMap<String,String> newRef) {
        return this.referencesData.add(newRef);
    }
    
}