/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.ehmi.spec.SpokObject;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeComparatorTest extends NodeTest {

    private NodeComparator node;

    public NodeComparatorTest() throws JSONException {
        super();
        JSONObject l = new JSONObject();
        ruleJSON.put("type", "comparator");
        ruleJSON.put("comparator", "==");
        l.put("type", "number");
        l.put("value", 12);
        l.put("iid", "");
        l.put("phrase", "");
        ruleJSON.put("leftOperand", l);
        ruleJSON.put("rightOperand", l);

    }

    @Before
    public void setUp() throws Exception {
        this.node = new NodeComparator(ruleJSON, programNode);
        this.instance = this.node;
    }

    @Test
    public void testOperators() throws Exception {
        ruleJSON.put("comparator", "");
        try {
            this.instance = new NodeComparator(ruleJSON, null);
            Assert.fail("An exception should have been raised");
        } catch (SpokNodeException ex) {
            Assert.assertNotNull(ex);
        }
        ruleJSON.put("comparator", 2);
        try {
            this.instance = new NodeComparator(ruleJSON, null);
            Assert.fail("An exception should have been raised");
        } catch (SpokNodeException ex) {
            Assert.assertNotNull(ex);
        }

    }

    @Test
    public void testGetResult() throws SpokException {
        printTestName("GetResult");
        SpokObject result = this.node.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result.getType());
        Assert.assertEquals("true", result.getValue());
    }

    @Test
    public void testNumericOperation() throws Exception {
        printTestName("NumericOperation");
        JSONObject o = new JSONObject();
        JSONObject l = new JSONObject();
        JSONObject r = new JSONObject();
        o.put("comparator", ">");
        // Test with numbers
        l.put("type", "boolean");
        l.put("value", true);
        o.put("leftOperand", l);
        o.put("rightOperand", l);
        NodeComparator e0 = new NodeComparator(o, null);
        Assert.assertNull(e0.getResult());

        l.put("type", "number");
        l.put("value", "12");
        o.put("leftOperand", l);
        o.put("rightOperand", l);
        NodeComparator e1 = new NodeComparator(o, null);
        SpokObject result = e1.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result.getType());
        Assert.assertEquals("false", result.getValue());
        r.put("type", "number");
        r.put("value", "11");
        o.put("leftOperand", l);
        o.put("rightOperand", r);
        NodeComparator e2 = new NodeComparator(o, null);
        SpokObject result2 = e2.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result2.getType());
        Assert.assertEquals("true", result2.getValue());
    }

}
