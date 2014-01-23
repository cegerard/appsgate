/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import java.util.Collection;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import org.json.JSONObject;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeIfTest extends NodeTest {

    private NodeIf ifTest;

    public NodeIfTest() {
        context.checking(new Expectations() {
            {
                allowing(interpreter).notifyChanges(with(any(ProgramStateNotificationMsg.class)));
            }
        });
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        JSONObject v = new JSONObject();
        v.put("type", "boolean");
        v.put("value", "false");
        ruleJSON.put("expBool",  v);
        ruleJSON.put("seqRulesTrue", (Collection) null);
        ruleJSON.put("seqRulesFalse", (Collection) null);
        this.ifTest = new NodeIf(ruleJSON, null);
        this.instance = this.ifTest;
    }

}
