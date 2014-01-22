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
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeValueTest extends NodeTest {

    public NodeValueTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        JSONObject o = new JSONObject();
        o.put("value", false);
        o.put("type", "boolean");
        this.instance = new NodeValue(o, null);

    }
    @Test
    @Override
    public void testGetResult() throws SpokException{
        printTestName("GetResult");
        SpokObject result = this.instance.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("boolean", result.getType());
        Assert.assertEquals("false", result.getValue());
    }

}
