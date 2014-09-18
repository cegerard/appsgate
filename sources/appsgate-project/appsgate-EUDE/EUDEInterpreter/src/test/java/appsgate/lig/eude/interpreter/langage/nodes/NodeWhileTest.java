/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.ehmi.spec.EHMIProxyMock;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.spec.ProgramCommandNotification;

import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import static org.jmock.Expectations.returnValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeWhileTest extends NodeTest {

    public NodeWhileTest() throws Exception {
        super();
        final EHMIProxySpec c = new EHMIProxyMock("src/test/resources/jsonLibs/toto.json");
        final JSONObject events = new JSONObject();
        JSONObject e = new JSONObject();
        e.put("name", "event");
        e.put("iid", "event");
        events.put("endEvent", e);
        events.put("startEvent", e);
        final GenericCommand cmd = context.mock(GenericCommand.class);

        context.checking(new Expectations() {
            {
                allowing(mediator).getContext();
                will(returnValue(c));
                allowing(mediator).addNodeListening(with(any(NodeEvent.class)));
                allowing(mediator).notifyChanges(with(any(NotificationMsg.class)));
                exactly(1).of(mediator).executeCommand(with(any(String.class)), with(any(String.class)), with(any(JSONArray.class)), with(any(ProgramCommandNotification.class)));
                will(returnValue(cmd));
                allowing(cmd).run();
                allowing(cmd).getReturn();
                will(returnValue("test"));

            }
        });
        NodeStateDeviceTest s = new NodeStateDeviceTest();
        NodeSeqRulesTest seq = new NodeSeqRulesTest();

        ruleJSON.put("type", "while");
        ruleJSON.put("state", s.getRuleJSON());
        ruleJSON.put("rules", seq.getRuleJSON());
        ruleJSON.put("rulesThen", seq.getRuleJSON());
    }

    @Before
    public void setUp() throws Exception {
        this.instance = new NodeWhile(ruleJSON, programNode);
    }

}
