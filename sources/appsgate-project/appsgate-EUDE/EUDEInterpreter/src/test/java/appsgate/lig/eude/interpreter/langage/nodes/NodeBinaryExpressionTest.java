/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeBinaryExpressionTest extends NodeTest {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        JSONObject o = new JSONObject();
        JSONObject l = new JSONObject();
        o.put("operator", "EQUALS");
        l.put("type", "number");
        l.put("value", 12);
        o.put("leftOperand", l);
        o.put("rightOperand", l);
        this.instance = new NodeBinaryExpression(o, null);

    }

    @Test
    @Override
    public void testGetResult() throws SpokException {
        printTestName("GetResult");
        SpokObject result = this.instance.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result.getType());
        Assert.assertEquals("true", result.getValue());
    }

    @Test
    public void testAndOperation() throws Exception {
        printTestName("AndOperation");
        JSONObject o = new JSONObject();
        JSONObject l = new JSONObject();
        JSONObject r = new JSONObject();
        o.put("operator", "AND");
        // Test with numbers
        l.put("type", "number");
        l.put("value", 12);
        o.put("leftOperand", l);
        o.put("rightOperand", l);
        try {
            NodeBinaryExpression e0 = new NodeBinaryExpression(o, null);
            e0.getResult();
            Assert.fail("An exception should have been raised");
        } catch (SpokException ex) {
            Assert.assertNotNull(ex);
        }
        // Test with 1 AND 1
        l.put("type", "boolean");
        l.put("value", "true");
        o.put("leftOperand", l);
        o.put("rightOperand", l);
        NodeBinaryExpression e1 = new NodeBinaryExpression(o, null);
        SpokObject result = e1.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result.getType());
        Assert.assertEquals("true", result.getValue());
        // Test with 1 AND 0
        r.put("type", "boolean");
        r.put("value", "false");
        o.put("leftOperand", l);
        o.put("rightOperand", r);
        NodeBinaryExpression e2 = new NodeBinaryExpression(o, null);
        SpokObject result2 = e2.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result2.getType());
        Assert.assertEquals("false", result2.getValue());
        // Test with 0 AND 0
        l.put("type", "boolean");
        l.put("value", "false");
        o.put("leftOperand", l);
        o.put("rightOperand", r);
        NodeBinaryExpression e3 = new NodeBinaryExpression(o, null);
        SpokObject result3 = e3.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result3.getType());
        Assert.assertEquals("false", result3.getValue());
        // Test with 0 AND 1
        r.put("type", "boolean");
        r.put("value", "true");
        o.put("leftOperand", l);
        o.put("rightOperand", r);
        NodeBinaryExpression e4 = new NodeBinaryExpression(o, null);
        SpokObject result4 = e4.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result4.getType());
        Assert.assertEquals("false", result4.getValue());
    }

    @Test
    public void testNumericOperation() throws Exception {
        printTestName("NumericOperation");
        JSONObject o = new JSONObject();
        JSONObject l = new JSONObject();
        JSONObject r = new JSONObject();
        o.put("operator", "MORE_THAN");
        // Test with numbers
        l.put("type", "boolean");
        l.put("value", true);
        o.put("leftOperand", l);
        o.put("rightOperand", l);
        try {
            NodeBinaryExpression e0 = new NodeBinaryExpression(o, null);
            e0.getResult();

            Assert.fail("An exception should have been raised");
        } catch (SpokException ex) {
            Assert.assertNotNull(ex);
        }
        l.put("type", "number");
        l.put("value", "12");
        o.put("leftOperand", l);
        o.put("rightOperand", l);
        NodeBinaryExpression e1 = new NodeBinaryExpression(o, null);
        SpokObject result = e1.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result.getType());
        Assert.assertEquals("false", result.getValue());
        r.put("type", "number");
        r.put("value", "11");
        o.put("leftOperand", l);
        o.put("rightOperand", r);
        NodeBinaryExpression e2 = new NodeBinaryExpression(o, null);
        SpokObject result2 = e2.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result2.getType());
        Assert.assertEquals("true", result2.getValue());
    }

    @Test
    public void testOrOperation() throws Exception {
        printTestName("OrOperation");
        JSONObject o = new JSONObject();
        JSONObject l = new JSONObject();
        JSONObject r = new JSONObject();
        o.put("operator", "OR");
        // Test with numbers
        l.put("type", "number");
        l.put("value", 12);
        o.put("leftOperand", l);
        o.put("rightOperand", l);
        try {
            NodeBinaryExpression e0 = new NodeBinaryExpression(o, null);
            e0.getResult();

            Assert.fail("An exception should have been raised");
        } catch (SpokException ex) {
            Assert.assertNotNull(ex);
        }
        // Test with 1 Or 1
        l.put("type", "boolean");
        l.put("value", "true");
        o.put("leftOperand", l);
        o.put("rightOperand", l);
        NodeBinaryExpression e1 = new NodeBinaryExpression(o, null);
        SpokObject result = e1.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result.getType());
        Assert.assertEquals("true", result.getValue());
        // Test with 1 Or 0
        r.put("type", "boolean");
        r.put("value", "false");
        o.put("leftOperand", l);
        o.put("rightOperand", r);
        NodeBinaryExpression e2 = new NodeBinaryExpression(o, null);
        SpokObject result2 = e2.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result2.getType());
        Assert.assertEquals("true", result2.getValue());
        // Test with 0 Or 0
        l.put("type", "boolean");
        l.put("value", "false");
        o.put("leftOperand", l);
        o.put("rightOperand", r);
        NodeBinaryExpression e3 = new NodeBinaryExpression(o, null);
        SpokObject result3 = e3.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result3.getType());
        Assert.assertEquals("false", result3.getValue());
        // Test with 0 Or 1
        r.put("type", "boolean");
        r.put("value", "true");
        o.put("leftOperand", l);
        o.put("rightOperand", r);
        NodeBinaryExpression e4 = new NodeBinaryExpression(o, null);
        SpokObject result4 = e4.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result4.getType());
        Assert.assertEquals("true", result4.getValue());
    }

    @Test
    public void testNotOperation() throws Exception {
        printTestName("NotOperation");
        JSONObject o = new JSONObject();
        JSONObject l = new JSONObject();
        o.put("operator", "NOT");
        // Test with numbers
        l.put("type", "number");
        l.put("value", 12);
        o.put("leftOperand", l);
        try {
            NodeBinaryExpression e0 = new NodeBinaryExpression(o, null);
            e0.getResult();
            Assert.fail("An exception should have been raised");
        } catch (SpokException ex) {
            Assert.assertNotNull(ex);
        }
        // Test with true
        l.put("type", "boolean");
        l.put("value", "true");
        o.put("leftOperand", l);
        NodeBinaryExpression e1 = new NodeBinaryExpression(o, null);
        SpokObject result1 = e1.getResult();
        Assert.assertNotNull(result1);
        Assert.assertEquals("boolean", result1.getType());
        Assert.assertEquals("false", result1.getValue());
        // Test with false
        l.put("type", "boolean");
        l.put("value", "false");
        o.put("leftOperand", l);
        NodeBinaryExpression e2 = new NodeBinaryExpression(o, null);
        SpokObject result2 = e2.getResult();
        Assert.assertNotNull(result2);
        Assert.assertEquals("boolean", result2.getType());
        Assert.assertEquals("true", result2.getValue());
    }
}
