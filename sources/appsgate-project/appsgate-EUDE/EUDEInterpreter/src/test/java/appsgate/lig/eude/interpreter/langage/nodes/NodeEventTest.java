/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.spec.ProgramStateNotification;

import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jr
 */
public class NodeEventTest extends NodeTest {

    private NodeEvent eventTest;

    public NodeEventTest() throws JSONException {
        super();
        NodeValueTest t = new NodeValueTest();
        JSONObject o = t.ruleJSON;
        o.put("value", "test");
        ruleJSON.put("type", "event");
        ruleJSON.put("source", o);
        ruleJSON.put("eventName", "test");
        ruleJSON.put("eventValue", "test");

        context.checking(new Expectations() {
            {
                allowing(mediator).notifyChanges(with(any(ProgramStateNotification.class)));
                allowing(mediator).addNodeListening(with(any(NodeEvent.class)));
            }
        });
    }

    @Before
    public void setUp() throws Exception {
        this.eventTest = new NodeEvent(ruleJSON, this.programNode);
        this.instance = this.eventTest;
    }

    /**
     * Test of getSourceId method, of class NodeEvent.
     */
    @Test
    public void testGetSourceId() {
        System.out.println("getSourceId");
        String expResult = "test";
        String result = this.eventTest.getSourceId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getEventName method, of class NodeEvent.
     */
    @Test
    public void testGetEventName() {
        System.out.println("getEventName");
        String expResult = "test";
        String result = this.eventTest.getEventName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getEventValue method, of class NodeEvent.
     */
    @Test
    public void testGetEventValue() {
        System.out.println("getEventValue");
        String expResult = "test";
        String result = this.eventTest.getEventValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of coreEventFired method, of class NodeEvent.
     */
    @Test
    public void testCoreEventFired() {
        System.out.println("coreEventFired");
        this.eventTest.coreEventFired();
    }

}
