/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.langage.nodes.NodeException;
import appsgate.lig.eude.interpreter.langage.nodes.NodeSeqRules;
import java.util.ArrayList;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class SpokFunction{
    
    private final String name;
    private final ArrayList<SpokVariable> params;
    private final NodeSeqRules seqRules;

    /**
     * Constructor
     * @param obj the json description of the function
     * @throws NodeException 
     */
    public SpokFunction(JSONObject obj) throws NodeException {
        this.name = obj.optString("name");
        this.params = new ArrayList<SpokVariable>();
        this.seqRules = new NodeSeqRules(null, obj.optJSONArray(name), null);
    }
    
    /**
     * Method to print a function definition in Expert Program language
     * @return the function definition
     */
    String getExpertProgramDecl() {
        String ret = "function " + this.name + "(";
        for (SpokVariable v: params) {
            ret += v.getName() + ",";            
        }
        ret += ") {\n" + seqRules.getExpertProgramScript() + "\n,}";
        return ret;
    }

    
}
