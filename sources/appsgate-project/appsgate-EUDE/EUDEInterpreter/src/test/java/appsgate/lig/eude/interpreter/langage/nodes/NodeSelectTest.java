package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.main.spec.AppsGateSpec;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeSelectTest extends NodeTest {

    private final NodeProgram programNode;

    public NodeSelectTest() {
        final AppsGateSpec appsGate = context.mock(AppsGateSpec.class);
        programNode = context.mock(NodeProgram.class);

        context.checking(new Expectations() {
            {
                allowing(mediator).getAppsGate();
                will(returnValue(appsGate));
                allowing(appsGate).getSpecificDevices(with(any(JSONArray.class)), with(any(JSONArray.class)), with(any(JSONArray.class)));
                will(returnValue(null));
                allowing(programNode).getMediator();
                will(returnValue(mediator));
            }
        });
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ruleJSON.put("type", "select");
        ruleJSON.put("where", new JSONArray());
        ruleJSON.put("state", new JSONArray());
        ruleJSON.put("what", new JSONArray());
        this.instance = new NodeSelect(ruleJSON, programNode);
    }

    @Test
    @Override
    public void testgetMediator() {
        System.out.println("getMediator");
        try {
            this.instance.getMediator();
        } catch (SpokExecutionException ex) {
            fail("One exception has been raised");
        }
    }

}
