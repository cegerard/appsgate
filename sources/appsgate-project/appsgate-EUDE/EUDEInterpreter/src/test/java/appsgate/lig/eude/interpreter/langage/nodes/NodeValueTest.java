/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeValueTest extends NodeTest {
    private NodeValue node;

    public JSONObject getDesc(String type, String value) throws JSONException {
        ruleJSON.put("type", type);
        ruleJSON.put("value", value);
        return ruleJSON;
    }
    
    public NodeValueTest() throws JSONException {
        ruleJSON.put("value", false);
        ruleJSON.put("type", "boolean");
    }

    @Test
    public void testSomeMethod() {
    }

    @Before
    public void setUp() throws Exception {
        this.node = new NodeValue(ruleJSON, null);
        this.instance = this.node;

    }

    @Test
    public void testGetResult() throws SpokException {
        printTestName("GetResult");
        SpokObject result = this.node.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result.getType());
        Assert.assertEquals("false", result.getValue());
        Assert.assertEquals("false", instance.getExpertProgramScript());
    }

    @Test
    public void testVariableValue() throws Exception {
        printTestName("Variables");
        JSONObject r = new JSONObject();
        NodeValue var = new NodeValue(new JSONObject("{'type':'number', 'value':'1'}"), null);
        programNode.setVariable("test", var);
        r.put("type", "variable");
        r.put("value", "test");
        NodeValue v = new NodeValue(r, programNode);
        Assert.assertNotNull(v);
        Assert.assertEquals("variable", v.getType());
        Assert.assertEquals("test", v.getValue());
        Assert.assertEquals("1", v.getVariableValue());
        Assert.assertEquals("test", v.getExpertProgramScript());

    }

    @Test
    public void testRefVariable() throws Exception {
        printTestName("Ref Variables");

    }

    @Test
    public void testDeviceValue() throws Exception {
        printTestName("Variables");
        JSONObject r = new JSONObject();
        r.put("type", "device");
        r.put("value", "test");
        NodeValue v = new NodeValue(r, null);
        Assert.assertNotNull(v);
        Assert.assertEquals("device", v.getType());
        Assert.assertEquals("test", v.getValue());
        Assert.assertEquals("/test/", v.getExpertProgramScript());
    }

    @Test
    public void testProgramCallValue() throws Exception {
        printTestName("Variables");
        JSONObject r = new JSONObject();
        r.put("type", "programCall");
        r.put("value", "test");
        NodeValue v = new NodeValue(r, null);
        Assert.assertNotNull(v);
        Assert.assertEquals("programcall", v.getType());
        Assert.assertEquals("test", v.getValue());
        Assert.assertEquals("|test|", v.getExpertProgramScript());
    }

    @Test
    public void testListValue() throws Exception {
        printTestName("Variables");
        JSONObject r = new JSONObject();
        r.put("type", "list");
        r.put("value", new JSONArray());
        NodeValue v = new NodeValue(r, null);
        Assert.assertNotNull(v);
        Assert.assertEquals("list", v.getType());
        Assert.assertNotNull(v.getValue());
        Assert.assertEquals("[]", v.getExpertProgramScript());
    }

    @Test
    public void testStringValue() throws Exception {
        printTestName("Variables");
        JSONObject r = new JSONObject();
        r.put("type", "string");
        r.put("value", "test");
        NodeValue v = new NodeValue(r, null);
        Assert.assertNotNull(v);
        Assert.assertEquals("string", v.getType());
        Assert.assertEquals("test", v.getValue());
        Assert.assertEquals("\"test\"", v.getExpertProgramScript());
    }

    @Test
    public void testNumberValue() throws Exception {
        printTestName("Variables");
        JSONObject r = new JSONObject();
        r.put("type", "number");
        r.put("value", 12.3);
        NodeValue v = new NodeValue(r, null);
        Assert.assertNotNull(v);
        Assert.assertEquals("number", v.getType());
        Assert.assertEquals("12.3", v.getValue());
        Assert.assertEquals("12.3", v.getExpertProgramScript());
    }
    
    
    @Test
    public void testEquals() {
        printTestName("Equals");
        Node copy = this.instance.copy(null);
        Assert.assertTrue("Should be equal", this.instance.equals(copy));
    }

    
}
