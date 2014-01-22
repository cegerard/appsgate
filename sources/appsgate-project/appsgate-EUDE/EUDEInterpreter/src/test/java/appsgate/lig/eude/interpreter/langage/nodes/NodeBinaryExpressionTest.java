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
    public void testGetResult() throws SpokException{
        printTestName("GetResult");
        SpokObject result = this.instance.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result.getType());
        Assert.assertEquals("true", result.getValue());
    }
}
