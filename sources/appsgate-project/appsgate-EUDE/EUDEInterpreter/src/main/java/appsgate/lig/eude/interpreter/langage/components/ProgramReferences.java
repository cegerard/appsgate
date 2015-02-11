/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.eude.interpreter.langage.components;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * Class used to store data about the program's references.
 * 
 * @author bidoismorgan
 */
public class ProgramReferences {
    
    /**
     * programId : Id of the device referenced
     */
    private final String programId;
    
    /**
     * programStatus : STATUS of the device 
     */
    private ReferenceTable.STATUS programStatus;
    
    /**
     * referencesData : Hashmap for the information about reference (ie Type, name)
     */
    private final ArrayList<HashMap<String,String>> referencesData;

    public ProgramReferences(String pid, ReferenceTable.STATUS status, ArrayList<HashMap<String, String>> referencesData) {
        this.programId = pid;
        this.programStatus = status;
        this.referencesData = referencesData;
    }
    

    public String getProgramId() {
        return programId;
    }
    /**
     * 
     * @param programStatus 
     * @return true if the programStatus has changed
     */
    public Boolean setProgramStatus(ReferenceTable.STATUS programStatus) {
        if (this.programStatus == programStatus) {
            return false;
        }
        this.programStatus = programStatus;
        return true;
    }

    public ReferenceTable.STATUS getProgramStatus() {
        return programStatus;
    }
    
    public ArrayList<HashMap<String, String>> getReferencesData() {
        return referencesData;
    }
    
    public void addReferencesData(HashMap<String,String> newData) {
        this.referencesData.add(newData);
    }
    
}
