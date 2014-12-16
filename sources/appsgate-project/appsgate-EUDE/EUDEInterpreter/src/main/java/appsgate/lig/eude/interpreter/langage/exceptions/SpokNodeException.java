/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.exceptions;

import appsgate.lig.eude.interpreter.langage.nodes.Node;

/**
 *
 * @author jr
 */
public class SpokNodeException extends SpokException {

    private static final long serialVersionUID = -8848996955176059448L;

    /**
     *
     */
    private final Node node;

    /**
     *
     * @param n the node which is not correct
     * @param name
     * @param jsonParam
     * @param ex
     */
    public SpokNodeException(Node n, String name, String jsonParam, Exception ex) {
        super("Missing parameter [" + jsonParam + "] for " + name, ex);
        this.node = n;
    }

    /**
     * @return the node id
     */
    public String getNodeId() {
        if (node != null) {
            return node.getIID();
        }
        return null;
    }

}
