/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.agregator.spec.ContextAgregatorSpec;
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
        final ContextAgregatorSpec c = context.mock(ContextAgregatorSpec.class);
        final JSONArray a = new JSONArray();
        
        context.checking(new Expectations() {
            {
                allowing(mediator).getContext();
                will(returnValue(c));
                allowing(c).getBrickType("test");
                will(returnValue("test"));
                allowing(c).getEventsFromState(with(any(String.class)), with(any(String.class)), with(any(String.class)));
                will(returnValue(a));
            }
        });
        JSONObject o = new JSONObject();
        o.put("type", "device");
        o.put("id", "test");
        JSONObject v = new JSONObject();
        v.put("type", "boolean");
        v.put("value", true);
        ruleJSON.put("type", "state");
        ruleJSON.put("object", o);
        ruleJSON.put("stateName", "isOn");
        ruleJSON.put("stateValue", v);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.instance = new NodeState(ruleJSON, programNode);
    }

    @Test
    public void testExpertProgram() {
        String expertProgramScript = this.instance.getExpertProgramScript();
        System.out.println(expertProgramScript);
        Assert.assertEquals("/test/.isOn(true)", expertProgramScript);
    }
}
