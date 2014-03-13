/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.proxy.spec.ContextProxyMock;
import appsgate.lig.context.proxy.spec.ContextProxySpec;
import appsgate.lig.router.spec.GenericCommand;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import static org.jmock.Expectations.returnValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeStateTest extends NodeTest {

    public NodeStateTest() throws Exception {
        final ContextProxySpec c = new ContextProxyMock("src/test/resources/jsonLibs/toto.json");
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
        JSONObject o = new JSONObject();
        o.put("type", "device");
        o.put("id", "test");
        ruleJSON.put("type", "state");
        ruleJSON.put("object", o);
        ruleJSON.put("name", "isOn");
    }

    @Before
    public void setUp() throws Exception {
        this.instance = new NodeState(ruleJSON, programNode);
    }

    @Test
    public void testExpertProgram() {
        String expertProgramScript = this.instance.getExpertProgramScript();
        System.out.println(expertProgramScript);
        Assert.assertEquals("/test/.isOfState(isOn)", expertProgramScript);
    }
}
