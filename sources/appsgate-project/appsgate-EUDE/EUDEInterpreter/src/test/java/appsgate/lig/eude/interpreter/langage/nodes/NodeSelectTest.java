package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.agregator.spec.ContextAgregatorSpec;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import org.json.JSONArray;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeSelectTest extends NodeTest {


    public NodeSelectTest() {
        final ContextAgregatorSpec appsGate = context.mock(ContextAgregatorSpec.class);

        context.checking(new Expectations() {
            {
                allowing(mediator).getContext();
                will(returnValue(appsGate));
                allowing(appsGate).getDevicesInSpaces(with(any(JSONArray.class)), with(any(JSONArray.class)));
                will(returnValue(null));
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
