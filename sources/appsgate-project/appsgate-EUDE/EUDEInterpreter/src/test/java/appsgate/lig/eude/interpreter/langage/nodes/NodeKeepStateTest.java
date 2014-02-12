package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.agregator.ContextAgregatorImpl;
import appsgate.lig.router.spec.GenericCommand;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import static org.jmock.Expectations.returnValue;
import org.json.JSONArray;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeKeepStateTest extends NodeTest {

    public NodeKeepStateTest() throws Exception {
        super();
        final ContextAgregatorImpl c = new ContextAgregatorImpl();
        final GenericCommand cmd = context.mock(GenericCommand.class);

        context.checking(new Expectations() {
            {
                allowing(mediator).executeCommand(with(any(String.class)), with(any(String.class)), with(any(JSONArray.class)));
                will(returnValue(cmd));
                allowing(cmd).run();
                allowing(cmd).getReturn();
                will(returnValue("test"));

                allowing(mediator).getContext();
                will(returnValue(c));
                allowing(mediator).addNodeListening(with(any(NodeEvent.class)));
            }
        });
        NodeStateTest s = new NodeStateTest();

        ruleJSON.put("type", "keepstate");
        ruleJSON.put("state", s.getRuleJSON());
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.instance = new NodeKeepState(ruleJSON, programNode);
    }

}
