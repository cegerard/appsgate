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

    private final String errorMessage;

    /**
     *
     * @param n the node which is not correct
     * @param error the error message
     * @param ex
     */
    public SpokNodeException(Node n, String error, Exception ex) {
        super("NodeException: " + error, ex);
        this.node = n;
        this.errorMessage = error;
    }

    /**
     * @return the node id
     */
    public String getNodeId() {
        try {
            SpokNodeException c = (SpokNodeException) this.getCause();
            return c.getNodeId();
        } catch (ClassCastException e) {
            // Do nothing
        }
        if (node != null) {
            return node.getIID();
        }
        return null;
    }

    /**
     * @return the error message
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

}
