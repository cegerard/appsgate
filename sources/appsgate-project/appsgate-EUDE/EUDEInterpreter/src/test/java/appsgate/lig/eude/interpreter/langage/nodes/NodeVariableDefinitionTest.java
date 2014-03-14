/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import junit.framework.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeVariableDefinitionTest extends NodeTest {

    public NodeVariableDefinitionTest() throws JSONException {
        ruleJSON.put("id", "ref");
        ruleJSON.put("type", "variableDefinition");

    }

    @Before
    public void setUp() throws Exception {
        this.instance = new NodeVariableDefinition(ruleJSON, null);

    }

    @Test
    @Override
    public void testCall() throws Exception {
        printTestName("call");
        JSONObject result = this.instance.call();
        Assert.assertNotNull(result);
        Assert.assertTrue("supposed to be started", this.instance.isStarted());
    }

}
