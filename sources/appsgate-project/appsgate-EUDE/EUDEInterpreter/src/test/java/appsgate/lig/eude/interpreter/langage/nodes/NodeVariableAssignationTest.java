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

    public NodeVariableAssignationTest() throws JSONException {
        JSONObject val = new JSONObject();
        val.put("type", "number");
        val.put("value", "0");
        ruleJSON.put("value", val);
        ruleJSON.put("name", "test");
        ruleJSON.put("type", "assignation");

    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.instance = new NodeVariableAssignation(ruleJSON, null);
    }

    @Test
    @Override
    public void testGetResult() throws Exception {
        printTestName("GetResult");
        SpokObject result = this.instance.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("number", result.getType());
        Assert.assertEquals("0", result.getValue());
    }
}
