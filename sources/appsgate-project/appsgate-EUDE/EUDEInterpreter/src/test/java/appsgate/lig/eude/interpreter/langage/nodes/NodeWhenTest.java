/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeWhenTest extends NodeTest {

    public NodeWhenTest() throws Exception {
        super();
        NodeEventsTest events = new NodeEventsTest();
        ruleJSON.put("events", events.getRuleJSON());
        ruleJSON.put("seqRulesThen", emptySeqRules);
        ruleJSON.put("type", "when");

    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.instance = new NodeWhen(this.ruleJSON, null);

    }

}
