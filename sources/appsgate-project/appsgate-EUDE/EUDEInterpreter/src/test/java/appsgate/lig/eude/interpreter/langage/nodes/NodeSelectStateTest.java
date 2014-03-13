package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.proxy.spec.ContextProxyMock;
import appsgate.lig.context.proxy.spec.ContextProxySpec;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
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

    public NodeSelectStateTest() throws JSONException {
        final ContextProxySpec c = new ContextProxyMock("src/test/resources/jsonLibs/toto.json");
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
        ruleJSON.put("devices", new JSONObject("{'type':'device', 'id':'test'}"));
        ruleJSON.put("state", "test");
        ruleJSON.put("value", "true");

    }

    @Before
    public void setUp() throws Exception {
        this.instance = new NodeSelectState(ruleJSON, programNode);
    }

    @Test
    public void testSelect() throws Exception {
        printTestName("Select");
        instance.call();
        SpokObject result = instance.getResult();
        Assert.assertNotNull(result);
        System.out.println(result.getJSONDescription().toString());
    }
}
