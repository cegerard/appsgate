/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.exceptions;

import appsgate.lig.eude.interpreter.langage.components.SpokObject;

/**
 *
 * @author jr
 */
public class SpokTypeException extends SpokException {

    public SpokTypeException(String type, SpokObject o) {
        super("[" +type + "] not correct found: " + o.getType(), null);
    }

}
