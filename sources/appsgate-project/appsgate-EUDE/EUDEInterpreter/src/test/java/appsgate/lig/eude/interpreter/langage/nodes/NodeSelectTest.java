package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.proxy.spec.ContextProxyMock;
import appsgate.lig.context.proxy.spec.ContextProxySpec;
import org.jmock.Expectations;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeSelectTest extends NodeTest {

    public NodeSelectTest() throws JSONException {
        final ContextProxySpec c = new ContextProxyMock("src/test/resources/jsonLibs/toto.json");

        context.checking(new Expectations() {
            {
                allowing(mediator).getContext();
                will(returnValue(c));
            }
        });
        ruleJSON.put("type", "select");
        ruleJSON.put("where", new JSONArray());
        ruleJSON.put("what", new JSONArray());

    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.instance = new NodeSelect(ruleJSON, programNode);
    }

}
