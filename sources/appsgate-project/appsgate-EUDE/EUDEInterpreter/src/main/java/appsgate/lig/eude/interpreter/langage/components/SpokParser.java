/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class SpokParser {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SpokParser.class);

    /**
     *
     * @param result
     * @return the boolean value
     * @throws SpokTypeException if the object is not a boolean
     */
    public static Boolean getBooleanResult(SpokObject result) throws SpokTypeException {
        if (result == null) {
            LOGGER.error("Null argument passed to getBooleanResult");
            throw new SpokTypeException("Null argument", null);
        }
        if (result.getType().equalsIgnoreCase("boolean")) {
            return Boolean.parseBoolean(result.getValue());
        }
        throw new SpokTypeException("boolean", result);
    }

    /**
     *
     * @param result
     * @return
     * @throws SpokTypeException
     */
    public static Double getNumericResult(SpokObject result) throws SpokTypeException {
        try {
            if (result.getType().equalsIgnoreCase("number")) {
                return Double.parseDouble(result.getValue());
            }
        } catch (NumberFormatException ex) {
            throw new SpokTypeException("number", result);
        }
        throw new SpokTypeException("number", result);
    }

    /**
     *
     * @param l
     * @param r
     * @return
     */
    public static Boolean equals(SpokObject l, SpokObject r) {
        if (l == null || r == null || l.getValue() == null || l.getType() == null) {
            LOGGER.debug("A null value has been passed to the comparator");
            return false;
        }
        return l.getType().equalsIgnoreCase(r.getType())
                && l.getValue().equalsIgnoreCase(r.getValue());

    }

}
