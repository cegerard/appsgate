/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class NodeFunctionDefinition extends Node {

    private String name;
    private NodeSeqRules seqRules;

    /**
     *
     * @param interpreter
     * @param programJSON
     * @param parent
     * @throws NodeException
     * @throws JSONException
     */
    public NodeFunctionDefinition(EUDEInterpreterImpl interpreter, JSONObject programJSON, Node parent)
            throws NodeException, JSONException {
        super(interpreter, parent);
        this.name = programJSON.optString("id");
        this.setSymbolTable(new SymbolTable(programJSON.getJSONArray("seqDefinitions")));
        this.seqRules = new NodeSeqRules(interpreter, programJSON.optJSONArray("seqRules"), this);
    }

    private NodeFunctionDefinition(EUDEInterpreterImpl interpreter, Node parent) {
        super(interpreter, parent);
    }

    @Override
    protected void specificStop() {
    }

    @Override
    public String getExpertProgramScript() {
        String ret = "function " + this.name + "(";
        // Build the parameter list
        Set<String> s = this.getSymbolTable().getVarList();
        if (s != null && !s.isEmpty()) {

            Iterator<String> iter = s.iterator();
            StringBuilder builder = new StringBuilder(iter.next());
            while (iter.hasNext()) {
                builder.append(",").append(iter.next());
            }
            ret += builder.toString();
        }
        ret += ") {\n" + seqRules.getExpertProgramScript() + "\n}";
        return ret;
    }

    /**
     * do nothing, the symbol are defined inside the function
     * 
     * @param s the symbolic table to populate
     */
    @Override
    protected void collectVariables(SymbolTable s) {
    }

    @Override
    public void endEventFired(EndEvent e) {
        
    }

    /**
     * 
     * @param params
     * @param parent
     * @return 
     */
    NodeSeqRules getCode(JSONArray params, Node parent) {
        NodeSeqRules newRules = (NodeSeqRules) seqRules.copy(parent);
        newRules.initSymbolTableFromParams(params);
        return newRules;
    }

    @Override
    Node copy(Node parent) {
        NodeFunctionDefinition ret = new NodeFunctionDefinition(this.getInterpreter(), parent);
        ret.name = name;
        ret.seqRules = (NodeSeqRules) seqRules.copy(ret);
        return ret;
    }

}
