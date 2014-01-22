package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.main.spec.AppsGateSpec;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import org.json.JSONArray;
import org.json.JSONException;
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
                allowing(interpreter).getAppsGate();
                will(returnValue(appsGate));
                allowing(appsGate).getSpecificDevices(with(any(JSONArray.class)), with(any(JSONArray.class)), with(any(JSONArray.class)));
                will(returnValue(null));
                allowing(programNode).getInterpreter();
                will(returnValue(interpreter));
            }
        });
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        JSONObject o = new JSONObject();
        try {
            o.put("where", (JSONArray) null);
            o.put("state", new JSONArray());
        } catch (JSONException ex) {
        }

        this.instance = new NodeSelect(o, programNode);
    }

    @Test
    @Override
    public void testGetInterpreter() {
        System.out.println("GetInterpreter");
        try {
            this.instance.getInterpreter();
        } catch (SpokExecutionException ex) {
            fail("One exception has been raised");
        }
    }

}
