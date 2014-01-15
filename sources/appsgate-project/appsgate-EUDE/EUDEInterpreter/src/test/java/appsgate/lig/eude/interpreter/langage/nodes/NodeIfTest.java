/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import org.json.JSONException;
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
    public void setUp() {
        try {
            super.setUp();
            ruleJSON.put("expBool", (Collection) null);
            ruleJSON.put("seqRulesTrue", (Collection) null);
            ruleJSON.put("seqRulesFalse", (Collection) null);
            this.ifTest = new NodeIf(interpreter, ruleJSON, null);
            this.instance = this.ifTest;
        } catch (JSONException ex) {
            System.out.println("JSON ex : " + ex.getMessage());
        } catch (NodeException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
