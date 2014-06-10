package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.ehmi.spec.EHMIProxyMock;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.manager.propertyhistory.services.PropertyHistoryManager;
import appsgate.lig.manager.propertyhistory.services.PropertyHistoryManagerMock;
import org.jmock.Expectations;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeSelectStateTest extends NodeTest {
    
    private NodeSelectState node;

    public NodeSelectStateTest() throws JSONException {
        final EHMIProxySpec c = new EHMIProxyMock("src/test/resources/jsonLibs/toto.json");
        final PropertyHistoryManager prop = new PropertyHistoryManagerMock();

        context.checking(new Expectations() {
            {
                allowing(mediator).getContext();
                will(returnValue(c));
                allowing(mediator).getPropHistManager();
                will(returnValue(prop));
                allowing(mediator).getTime();
                will(returnValue(new Long(0)));
            }
        });
        ruleJSON.put("type", "selectState");
        NodeValueTest t = new NodeValueTest();
        ruleJSON.put("devices", t.getDesc("device", "test"));
        ruleJSON.put("state", "test");
        ruleJSON.put("value", "true");

    }

    @Before
    public void setUp() throws Exception {
        this.node = new NodeSelectState(ruleJSON, programNode);
        this.instance = this.node;
    }

    @Test
    public void testSelect() throws Exception {
        printTestName("Select");
        node.call();
        NodeValue result = node.getResult();
        Assert.assertNotNull(result);
        System.out.println(result.getJSONDescription().toString());
    }
}
