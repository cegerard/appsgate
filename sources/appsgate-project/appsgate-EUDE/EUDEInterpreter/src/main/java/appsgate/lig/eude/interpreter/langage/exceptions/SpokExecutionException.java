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
public class SpokExecutionException extends SpokException {


    /**
     * @param reason
     */
    public SpokExecutionException(String reason) {
        super(reason, null);
    }

    
}
