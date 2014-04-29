/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.ehmi.spec.EHMIProxyMock;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;

import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import static org.jmock.Expectations.returnValue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeStateTest extends NodeTest {

    private NodeState state;
    
    public NodeStateTest() throws Exception {
        final EHMIProxySpec c = new EHMIProxyMock("src/test/resources/jsonLibs/toto.json");
        final JSONObject events = new JSONObject();
        JSONObject e = new JSONObject();
        e.put("name", "event");
        events.put("endEvent", e);
        events.put("startEvent", e);

        final GenericCommand cmd = context.mock(GenericCommand.class);

        context.checking(new Expectations() {
            {
                allowing(mediator).getContext();
                will(returnValue(c));
                allowing(mediator).addNodeListening(with(any(NodeEvent.class)));
                allowing(mediator).executeCommand(with(any(String.class)), with(any(String.class)), with(any(JSONArray.class)));
                will(returnValue(cmd));
                allowing(cmd).run();
                allowing(cmd).getReturn();
                will(returnValue("test"));
            }
        });
        NodeValueTest t = new NodeValueTest();
        JSONObject o = t.ruleJSON;
        o.put("type", "device");
        o.put("value", "test");
        ruleJSON.put("type", "state");
        ruleJSON.put("object", o);
        ruleJSON.put("name", "isOn");
    }

    @Before
    public void setUp() throws Exception {
        state = new NodeState(ruleJSON, programNode);
        this.instance = state;
    }

    @Test
    public void testExpertProgram() {
        String expertProgramScript = this.instance.getExpertProgramScript();
        System.out.println(expertProgramScript);
        Assert.assertEquals("/test/.isOfState(isOn)", expertProgramScript);
    }
    @Test
    public void testCompositeState()  throws JSONException{
        ruleJSON.put("name", "testState");
        try {
            state = new NodeState(ruleJSON, programNode);
        } catch (SpokNodeException ex) {
            ex.printStackTrace();
            Assert.fail("No exception should have been raised");
        }
        Assert.assertNull(state.call());
    }
}
