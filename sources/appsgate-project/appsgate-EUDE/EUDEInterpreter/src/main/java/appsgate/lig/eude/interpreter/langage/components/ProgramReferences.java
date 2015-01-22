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
    private String programId;
    
    /**
     * programStatus : STATUS of the device 
     */
    private ReferenceTable.STATUS programStatus;
    
    /**
     * referencesData : Hashmap for the information about reference (ie Type, name)
     */
    private ArrayList<HashMap<String,String>> referencesData;

    public ProgramReferences(String deviceId, ReferenceTable.STATUS deviceStatus, ArrayList<HashMap<String, String>> referencesData) {
        this.programId = deviceId;
        this.programStatus = deviceStatus;
        this.referencesData = referencesData;
    }
    
    public ProgramReferences(String deviceId, ReferenceTable.STATUS deviceStatus) {
        this.programId = deviceId;
        this.programStatus = deviceStatus;
        this.referencesData = new ArrayList<HashMap<String, String>>();
    }

    public String getProgramId() {
        return programId;
    }
    
    public void setProgramStatus(ReferenceTable.STATUS programStatus) {
        this.programStatus = programStatus;
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
