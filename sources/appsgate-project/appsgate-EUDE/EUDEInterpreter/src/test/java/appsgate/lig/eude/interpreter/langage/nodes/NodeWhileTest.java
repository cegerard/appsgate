/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.context.agregator.ContextAgregatorImpl;
import appsgate.lig.context.agregator.spec.ContextAgregatorSpec;
import appsgate.lig.router.spec.GenericCommand;
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
        final ContextAgregatorSpec c = new ContextAgregatorImpl();
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
        NodeStateTest s = new NodeStateTest();
        NodeSeqRulesTest seq = new NodeSeqRulesTest();

        ruleJSON.put("type", "while");
        ruleJSON.put("state", s.getRuleJSON());
        ruleJSON.put("rules", seq.getRuleJSON());
        ruleJSON.put("rulesThen", "toto");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.instance = new NodeWhile(ruleJSON, programNode);
    }

}
