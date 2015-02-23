/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.ehmi.spec.EHMIProxyMock;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.references.ReferenceTable;

import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import static org.jmock.Expectations.returnValue;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeStateProgramTest extends NodeTest {

    private NodeStateProgram state;

    public NodeStateProgramTest() throws Exception {
        super();
        final EHMIProxySpec c = new EHMIProxyMock("src/test/resources/jsonLibs/toto.json");
        final JSONObject events = new JSONObject();
        JSONObject e = new JSONObject();
        e.put("name", "event");
        events.put("endEvent", e);
        events.put("startEvent", e);

        context.checking(new Expectations() {
            {
                allowing(mediator).newProgramStatus(with(any(String.class)), with(any(ReferenceTable.STATUS.class)));
                allowing(mediator).getContext();
                will(returnValue(c));
                allowing(mediator).addNodeListening(with(any(NodeEvent.class)));
                allowing(mediator).notifyChanges(with(any(NotificationMsg.class)));
                allowing(mediator).getNodeProgram(with(any(String.class)));
                will(returnValue(programNode));
            }
        });
        NodeValueTest t = new NodeValueTest();
        JSONObject o = t.ruleJSON;
        o.put("type", "programCall");
        o.put("value", "test");
        ruleJSON.put("type", "stateProgram");
        ruleJSON.put("object", o);
        ruleJSON.put("name", "isStarted");
    }

    @Before
    public void setUp() throws Exception {
        state = new NodeStateProgram(ruleJSON, programNode);
        this.instance = state;
    }

    @Test
    public void testExpertProgram() {
        String expertProgramScript = this.instance.getExpertProgramScript();
        System.out.println(expertProgramScript);
        Assert.assertEquals("|test|.isOfState(isStarted)", expertProgramScript);
    }

    @Test
    public void testState() {
        programNode.setProcessing(null);
        Assert.assertTrue(programNode.isRunning());
        Assert.assertTrue(state.isOfState());
        programNode.setStopped();
        Assert.assertFalse(programNode.isRunning());
        Assert.assertFalse(state.isOfState());

    }
}
