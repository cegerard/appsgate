/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeEventsTest extends NodeTest {

    public NodeEventsTest() {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ruleJSON.put("type", "events");
        ruleJSON.put("events", new JSONArray());
        ruleJSON.put("nbEventToOccur", 1);
        ruleJSON.put("duration", 0);
        this.instance = new NodeEvents(this.ruleJSON, null);

    }

    @Test
    public void testGetManyEvents() throws Exception {
        NodeEventTest t = new NodeEventTest();
        t.setUp();
        NodeEvent nodeEvent = (NodeEvent) t.instance;

        JSONObject json = new JSONObject();
                json.put("type", "events");
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(nodeEvent.getJSONDescription());
        json.put("events", jsonArray);
        json.put("nbEventToOccur", 2);
        json.put("duration", 0);
        NodeEvents n = new NodeEvents(json, null);
        
        n.call();
        n.endEventFired(new EndEvent(nodeEvent));
        Assert.assertTrue(n.isStarted());
        n.endEventFired(new EndEvent(nodeEvent));
        Assert.assertFalse(n.isStarted());
    }
    
    @Test
    public void testGetEventsDuration() throws Exception {
        NodeEventTest t = new NodeEventTest();
        t.setUp();
        NodeEvent nodeEvent = (NodeEvent) t.instance;
        

        JSONObject json = new JSONObject();
                json.put("type", "events");
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(nodeEvent.getJSONDescription());
        json.put("events", jsonArray);
        json.put("nbEventToOccur", 2);
        json.put("duration", 2);
        NodeEvents n = new NodeEvents(json, null);
        NodeEvent clockEvent = new NodeEvent("clock", n.getClockId(), null, null, instance);
        
        n.call();
        n.endEventFired(new EndEvent(nodeEvent));
        Assert.assertTrue(n.isStarted());
        n.endEventFired(new EndEvent(clockEvent));
        Assert.assertTrue(n.isStarted());

        n.endEventFired(new EndEvent(nodeEvent));
        Assert.assertTrue(n.isStarted());
        n.endEventFired(new EndEvent(nodeEvent));
        Assert.assertFalse(n.isStarted());
    }

}
