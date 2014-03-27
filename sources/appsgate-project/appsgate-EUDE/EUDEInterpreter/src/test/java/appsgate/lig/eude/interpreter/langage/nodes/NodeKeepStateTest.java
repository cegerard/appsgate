package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.proxy.spec.ContextProxyMock;
import appsgate.lig.context.proxy.spec.ContextProxySpec;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.router.spec.GenericCommand;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import static org.jmock.Expectations.returnValue;
import org.jmock.States;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeKeepStateTest extends NodeTest {

    private States tested;

    public NodeKeepStateTest() throws Exception {
        super();
        final ContextProxySpec c = new ContextProxyMock("src/test/resources/jsonLibs/toto.json");
        final GenericCommand cmd = context.mock(GenericCommand.class);
        tested = context.states("NotYet");

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
                then(tested.is("listening"));
                allowing(mediator).removeNodeListening(with(any(NodeEvent.class)));
                then(tested.is("no"));
            }
        });
        NodeStateTest s = new NodeStateTest();

        ruleJSON.put("type", "keepstate");
        ruleJSON.put("state", s.getRuleJSON());
    }

    @Before
    public void setUp() throws Exception {
        this.instance = new NodeKeepState(ruleJSON, programNode);
    }

    @Test
    public void testKeepState() throws Exception {
        NodeKeepState n = (NodeKeepState) this.instance;
        n.call();
        synchroniser.waitUntil(tested.is("listening"), 200);
        tested.become("no");
        n.endEventFired(new EndEvent(n.getState()));
        synchroniser.waitUntil(tested.is("listening"), 200);
        tested.become("no");
        n.endEventFired(new EndEvent(n.getState()));
        n.endEventFired(new EndEvent(n.getState()));
        synchroniser.waitUntil(tested.is("listening"), 200);
        n.stop();
        synchroniser.waitUntil(tested.is("no"), 200);
        n.call();
        synchroniser.waitUntil(tested.is("listening"), 200);

    }

}
