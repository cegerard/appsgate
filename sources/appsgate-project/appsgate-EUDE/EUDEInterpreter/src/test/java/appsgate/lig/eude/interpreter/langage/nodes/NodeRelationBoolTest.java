/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import java.util.Collection;
import junit.framework.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeRelationBoolTest extends NodeTest {

    private NodeRelationBool relationTest;

    public NodeRelationBoolTest() {
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {
            JSONObject op = new JSONObject();
            op.put("targetId", "test");
            op.put("returnType", "test");
            op.put("targetType", "test");
            op.put("methodName", "test");
            op.put("args", (Collection) null);
            op.put("type", "string");
            op.put("value", "test");
            ruleJSON.put("operator", "test");
            ruleJSON.put("leftOperand", op);
            ruleJSON.put("rightOperand", op);
            this.relationTest = new NodeRelationBool(null, ruleJSON, null);
            this.instance = this.relationTest;
        } catch (JSONException ex) {
            System.out.println("JSON Ex : " + ex.getMessage());

        } catch (NodeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Test of getResult method, of class NodeRelationBool.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetResult() throws Exception {
        System.out.println("getResult");
        try {
            Boolean result = this.relationTest.getResult();
            Assert.fail("An exception is supposed to have been raised, instead a result has been returned: " + result);
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    /**
     * Test of endEventFired method, of class NodeProgram.
     */
    @Test
    @Override
    public void testEndEventFired() {
        System.out.println("endEventFired");
        NodeAction ac;
        try {
            ac = new NodeAction(this.interpreter, this.ruleJSON, null);
            EndEvent e = new EndEvent(ac);
            this.relationTest.endEventFired(e);
        } catch (NodeException ex) {
            System.out.println("NodeException ex: " + ex.getMessage());
            Assert.fail("unable to create a NodeAction for test");
        }
    }

}
