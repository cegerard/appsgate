/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeFunctionDefinition extends Node {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFunctionDefinition.class);

    private String name;
    private NodeSeqRules seqRules;

    /**
     *
     * @param programJSON
     * @param parent
     * @throws SpokNodeException
     */
    public NodeFunctionDefinition(JSONObject programJSON, Node parent)
            throws SpokException {
        super(parent);
        this.name = programJSON.optString("id");
        this.setSymbolTable(new SymbolTable(programJSON.optJSONArray("seqDefinitions")));
        this.seqRules = new NodeSeqRules(programJSON.optJSONArray("seqRules"), this);
    }

    private NodeFunctionDefinition(Node parent) {
        super(parent);
    }

    @Override
    protected void specificStop() {
    }

    @Override
    public String getExpertProgramScript() {
        String ret = "function " + this.name + "(";
        // Build the parameter list
        List<String> s = this.getSymbolTable().getVarList();
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

    @Override
    public void endEventFired(EndEvent e) {

    }

    /**
     *
     * @param parent
     * @return
     * @throws SpokException
     */
    public NodeSeqRules getCode(Node parent)
            throws SpokException {
        NodeSeqRules newRules = (NodeSeqRules) seqRules.copy(parent);
        return newRules;
    }
    
    @Override
    JSONObject getJSONDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    Node copy(Node parent) {
        NodeFunctionDefinition ret = new NodeFunctionDefinition(parent);
        ret.name = name;
        ret.seqRules = (NodeSeqRules) seqRules.copy(ret);
        return ret;
    }

    @Override
    public JSONObject call() {
        LOGGER.warn("Trying to call a non functional node");
        return null;
    }

}
