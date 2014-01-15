/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.exceptions;

/**
 *
 * @author jr
 */
public class NodeException extends Exception {

    private static final long serialVersionUID = -8848996955176059448L;

    /**
     *
     * @param name
     * @param jsonParam
     * @param ex
     */
    public NodeException(String name, String jsonParam, Exception ex) {
        super("Missing parameter [" + jsonParam + "] for " + name, ex);
    }

}
