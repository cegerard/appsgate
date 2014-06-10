package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.ehmi.spec.EHMIProxyMock;
import appsgate.lig.ehmi.spec.EHMIProxySpec;

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
        final EHMIProxySpec c = new EHMIProxyMock("src/test/resources/jsonLibs/toto.json");

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
    public void setUp() throws Exception {
        this.instance = new NodeSelect(ruleJSON, programNode);
    }

}
