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
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
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
    private Node seqRules;

    /**
     *
     * @param programJSON
     * @param parent
     * @throws SpokNodeException
     */
    public NodeFunctionDefinition(JSONObject programJSON, Node parent)
            throws SpokNodeException {
        super(parent, programJSON.optString("iid"));
        this.name = programJSON.optString("id");
        try {
            this.setSymbolTable(new SymbolTable(programJSON.optJSONArray("seqDefinitions"), this));
        } catch (SpokException ex) {
            LOGGER.error("Unable to set the symbol table");
            throw new SpokNodeException("NodeFunctionDefinition", "seqDefinitions", ex);
        }
        try {
            this.seqRules = Builder.buildFromJSON(programJSON.optJSONObject("seqRules"), this);
        } catch (SpokTypeException ex) {
            throw new SpokNodeException("NodeFunctionDefinition", "seqRules", ex);
        }
    }

    /**
     * private constructor to copy method
     *
     * @param parent
     */
    private NodeFunctionDefinition(Node parent, String id) {
        super(parent, id);
    }

    @Override
    protected void specificStop() {
    }

    /**
     * @return the name of the function
     */
    public String getName() {
        return name;
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
    public Node getCode(Node parent)
            throws SpokException {
        Node newRules = seqRules.copy(parent);
        return newRules;
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
        try {
            o.put("type", "functionDefinition");
            o.put("id", this.name);

            o.put("seqRules", seqRules.getJSONDescription());
            o.put("seqDefinitions", getSymbolTableDescription());
        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case           
        }
        return o;
    }

    @Override
    protected Node copy(Node parent) {
        NodeFunctionDefinition ret = new NodeFunctionDefinition(parent, getIID());
        ret.name = name;
        ret.seqRules = seqRules.copy(ret);
        return ret;
    }

    @Override
    public JSONObject call() {
        LOGGER.warn("Trying to call a non functional node");
        return getJSONDescription();
    }

}
