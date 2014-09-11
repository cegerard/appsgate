/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class NodeStateProgram extends NodeState {

    public NodeStateProgram(JSONObject o, Node parent) throws SpokNodeException {
        super(parent, o);
    }

    /**
     * Private constructor to allow copy
     *
     * @param p the parent node
     */
    private NodeStateProgram(Node p) {
        super(p);
    }

    @Override
    protected Node copy(Node parent) {
        NodeStateProgram o = new NodeStateProgram(parent);
        return commonCopy(o);
    }

    @Override
    protected void buildEventsList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NodeAction getSetter() throws SpokExecutionException, SpokNodeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Boolean isOfState() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
