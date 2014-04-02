/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import appsgate.lig.eude.interpreter.impl.TestUtilities;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeIfTest extends NodeTest {

    private NodeIf ifTest;

    public NodeIfTest() throws JSONException {
        JSONObject v = new JSONObject();
        v.put("type", "boolean");
        v.put("value", "false");
        ruleJSON.put("type", "if");
        ruleJSON.put("expBool", v);
        ruleJSON.put("seqRulesTrue", emptySeqRules);
        ruleJSON.put("seqRulesFalse", emptySeqRules);

        context.checking(new Expectations() {
            {
                allowing(mediator).notifyChanges(with(any(ProgramStateNotificationMsg.class)));
            }
        });
    }

    @Before
    public void setUp() throws Exception {
        this.ifTest = new NodeIf(ruleJSON, null);
        this.instance = this.ifTest;
    }

    @Test
    public void testBuildFromJson() throws Exception {
        printTestName("BuildFromJSON");
        NodeIf seq;
        seq = (NodeIf) Builder.buildFromJSON(TestUtilities.loadFileJSON("src/test/resources/node/if.json"), instance);
        Assert.assertNotNull(seq);
        System.out.println(seq.getExpertProgramScript());
        seq.call();
    }
}
