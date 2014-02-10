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
import org.json.JSONObject;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeWhileTest extends NodeTest {

    public NodeWhileTest() throws Exception {
        super();
        final ContextAgregatorSpec c = context.mock(ContextAgregatorSpec.class);
        final JSONObject events = new JSONObject();
        JSONObject e = new JSONObject();
        e.put("name", "event");
        events.put("endEvent", e);
        events.put("startEvent", e);

        context.checking(new Expectations() {
            {
                allowing(mediator).getContext();
                will(returnValue(c));
                allowing(c).getBrickType("test");
                will(returnValue("test"));
                allowing(c).getEventsFromState(with(any(String.class)), with(any(String.class)));
                will(returnValue(events));
                allowing(c).isOfState(with(any(String.class)), with(any(String.class)));
                will(returnValue(true));
                allowing(mediator).addNodeListening(with(any(NodeEvent.class)));
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
