package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.ehmi.spec.EHMIProxyMock;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.spec.ProgramCommandNotification;
import appsgate.lig.eude.interpreter.spec.ProgramLineNotification;

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
        final EHMIProxySpec c = new EHMIProxyMock("src/test/resources/jsonLibs/toto.json");
        final GenericCommand cmd = context.mock(GenericCommand.class);
        tested = context.states("NotYet");

        context.checking(new Expectations() {
            {
                allowing(mediator).notifyChanges(with(any(NotificationMsg.class)));
                allowing(mediator).executeCommand(with(any(String.class)), with(any(String.class)), with(any(JSONArray.class)), with(any(ProgramCommandNotification.class)));
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
        context.checking(new Expectations() {
            {
                allowing(mediator).notifyChanges(with(any(ProgramCommandNotification.class)));
                allowing(mediator).notifyChanges(with(any(ProgramLineNotification.class)));
            }
        });
        NodeKeepState n = (NodeKeepState) this.instance;
        System.out.println("=========== First call to keepState test");
        n.call();
        synchroniser.waitUntil(tested.is("listening"), 200);
        tested.become("no");
        System.out.println("============ End event fired");
        n.endEventFired(new EndEvent(n.getState()));
        synchroniser.waitUntil(tested.is("listening"), 200);
        tested.become("no");
        System.out.println("============ End event fired");
        n.endEventFired(new EndEvent(n.getState()));
        System.out.println("============ End event fired");
        n.endEventFired(new EndEvent(n.getState()));
        synchroniser.waitUntil(tested.is("listening"), 200);
        System.out.println("============ Stop");
        n.stop();
        synchroniser.waitUntil(tested.is("no"), 200);
        System.out.println("============ Call");
        n.call();
        synchroniser.waitUntil(tested.is("listening"), 200);

    }

}
