/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.impl.ClockProxy;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.spec.ProgramLineNotification;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import static org.jmock.Expectations.returnValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeEventsOrTest extends NodeTest {

    public NodeEventsOrTest() throws Exception {
        JSONObject desc = new JSONObject();
        desc.put("id", "12");
        final ClockProxy appsGate = new ClockProxy(desc);

        context.checking(new Expectations() {
            {
                allowing(mediator).getClock();
                will(returnValue(appsGate));
                allowing(mediator).addNodeListening(with(any(NodeEvent.class)));
                allowing(mediator).removeNodeListening(with(any(NodeEvent.class)));
                allowing(mediator).getTime();
                will(returnValue(new Long(2000)));
                allowing(mediator).notifyChanges(with(any(NotificationMsg.class)));
            }
        });
        ruleJSON.put("type", "eventsOr");
        ruleJSON.put("events", new JSONArray());
        ruleJSON.put("nbEventToOccur", 1);
        ruleJSON.put("duration", 0);

    }

    @Before
    public void setUp() throws Exception {
        this.instance = new NodeEventsOr(this.ruleJSON, programNode);
    }

    @Test
    public void testGetManyEvents() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(mediator).notifyChanges(with(any(NotificationMsg.class)));
            }
        });

        this.printTestName("GetManyEvents");
        NodeEventTest t = new NodeEventTest();
        t.setUp();
        NodeEvent nodeEvent = (NodeEvent) t.instance;

        JSONObject json = new JSONObject();
        json.put("type", "eventsOr");
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(nodeEvent.getJSONDescription());
        json.put("events", jsonArray);
        json.put("nbEventToOccur", 2);
        json.put("duration", 0);
        NodeEvents n = new NodeEventsOr(json, programNode);
        n.call();
        n.endEventFired(new EndEvent(nodeEvent));
        Assert.assertTrue("Node should be started", n.isStarted());
        n.endEventFired(new EndEvent(nodeEvent));
        Assert.assertFalse("Node should be stopped", n.isStarted());
    }

    @Test
    public void testGetEventsDuration() throws Exception {
        this.printTestName("GetEventsDuration");
        context.checking(new Expectations() {
            {
                allowing(mediator).notifyChanges(with(any(NotificationMsg.class)));
            }
        });

        NodeEventTest t = new NodeEventTest();
        t.setUp();
        NodeEvent nodeEvent = (NodeEvent) t.instance;

        JSONObject json = new JSONObject();
        json.put("type", "eventsOr");
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(nodeEvent.getJSONDescription());
        json.put("events", jsonArray);
        json.put("nbEventToOccur", 2);
        json.put("duration", 2);
        NodeEvents n = new NodeEventsOr(json, programNode);
        NodeEvent clockEvent = new NodeEvent("device", n.getMediator().getClock().getId(), null, null, instance);

        n.call();
        n.endEventFired(new EndEvent(nodeEvent));
        Assert.assertTrue("Node should be started", n.isStarted());
        n.endEventFired(new EndEvent(clockEvent));
        Assert.assertTrue("Node should be started", n.isStarted());

        n.endEventFired(new EndEvent(nodeEvent));
        Assert.assertTrue("Node should be started", n.isStarted());
        n.endEventFired(new EndEvent(nodeEvent));
        Assert.assertFalse("Node should be stopped", n.isStarted());
    }

}
