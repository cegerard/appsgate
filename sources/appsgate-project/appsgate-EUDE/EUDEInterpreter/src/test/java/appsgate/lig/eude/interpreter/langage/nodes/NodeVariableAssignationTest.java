package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeVariableAssignationTest extends NodeTest {
    private NodeVariableAssignation node;

    public NodeVariableAssignationTest() throws JSONException {
        NodeValueTest t = new NodeValueTest();
        JSONObject val = t.getDesc("number", "0");
        ruleJSON.put("value", val);
        ruleJSON.put("name", "test");
        ruleJSON.put("type", "assignation");

    }

    @Before
    public void setUp() throws Exception {
        this.node =  new NodeVariableAssignation(ruleJSON, programNode);
        this.instance = node;
    }

    @Test
    public void testGetResult() throws Exception {
        printTestName("GetResult");
        SpokObject result = this.node.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("number", result.getType());
        Assert.assertEquals("0", result.getValue());
    }
}
