/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.impl.ClockProxy;
import org.jmock.Expectations;
import org.json.JSONObject;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeWaitTest extends NodeTest {

    public NodeWaitTest() throws Exception {
        ruleJSON.put("type", "lists");
        NodeValueTest t = new NodeValueTest();
        ruleJSON.put("waitFor", t.getDesc("number", "1"));
        final ClockProxy appsGate = new ClockProxy(new JSONObject("{'id':12}"));

        context.checking(new Expectations() {
            {
                allowing(mediator).notifyChanges(with(any(NotificationMsg.class)));
                allowing(mediator).getClock();
                will(returnValue(appsGate));
                allowing(mediator).addNodeListening(with(any(NodeEvent.class)));
                allowing(mediator).removeNodeListening(with(any(NodeEvent.class)));
                allowing(mediator).getTime();
                will(returnValue(new Long(2000)));
            }
        });

    }

    @Before
    public void setUp() throws Exception {
        this.instance = new NodeWait(ruleJSON, programNode);
    }
   
}
