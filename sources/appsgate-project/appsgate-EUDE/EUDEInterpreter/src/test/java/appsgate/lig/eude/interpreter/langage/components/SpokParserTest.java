/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import appsgate.lig.eude.interpreter.langage.nodes.NodeValue;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class SpokParserTest {

    public SpokParserTest() {
    }

    @Test
    public void testGetBooleanValue() {
        NodeValue v = new NodeValue("int", "true", null);
        Boolean r;
        try {
            r = SpokParser.getBooleanResult(v);
            Assert.fail("Should have raised an error");
        } catch (SpokTypeException e) {
            Assert.assertNotNull(e);
        }
        v = new NodeValue("boolean", "bruk", null);
        try {
            r = SpokParser.getBooleanResult(v);
            Assert.assertFalse(r);
        } catch (SpokTypeException e) {
            Assert.fail("No exception should have been raised");
        }
        v = new NodeValue("boolean", "TRUE", null);
        try {
            r = SpokParser.getBooleanResult(v);
            Assert.assertTrue(r);
        } catch (SpokTypeException e) {
            Assert.fail("No exception should have been raised");
        }
        v = new NodeValue("boolean", "FALSE", null);
        try {
            r = SpokParser.getBooleanResult(v);
            Assert.assertFalse(r);
        } catch (SpokTypeException e) {
            Assert.fail("No exception should have been raised");
        }
    }

    @Test
    public void testGetNumberValue() {
        NodeValue v = new NodeValue("number", "true", null);
        Double r;
        try {
            r = SpokParser.getNumericResult(v);
            Assert.fail("Should have raised an error");
        } catch (SpokTypeException e) {
            Assert.assertNotNull(e);
        }
        v = new NodeValue("boolean", "12", null);
        try {
            r = SpokParser.getNumericResult(v);
            Assert.fail("Should have raised an error");
        } catch (SpokTypeException e) {
            Assert.assertNotNull(e);
        }
        v = new NodeValue("number", "12.5", null);
        try {
            r = SpokParser.getNumericResult(v);
            Assert.assertEquals((Double)12.5, r);
        } catch (SpokTypeException e) {
            Assert.fail("No exception should have been raised");
        }
        v = new NodeValue("number", "fr43.12!sd", null);
        try {
            r = SpokParser.getNumericResult(v);
            Assert.fail("Should have raised an error");
        } catch (SpokTypeException e) {
            Assert.assertNotNull(e);
        }
    }
}
