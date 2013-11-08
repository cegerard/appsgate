/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.Collection;
import junit.framework.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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
            op.put("type", "test");
            op.put("value", "test");
            ruleJSON.put("operator", "test");
            ruleJSON.put("leftOperand", op);
            ruleJSON.put("rightOperand", op);
            this.relationTest = new NodeRelationBool(null, ruleJSON);
            this.instance = this.relationTest;
        } catch (JSONException ex) {
            System.out.println("JSON Ex : " + ex.getMessage());
            
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
     * Test of undeploy method, of class NodeAction.
     */
    @Test
    @Override
    public void testUndeploy() {
        System.out.println("undeploy : NOT IMPLEMENTED YET");
    }


    /**
     * Test of resume method, of class NodeAction.
     */
    @Test
    @Override
    public void testResume() {
        System.out.println("resume : NOT IMPLEMENTED YET");
    }

    /**
     * Test of getState method, of class NodeAction.
     */
    @Test
    @Override
    public void testGetState() {
        System.out.println("getState : NOT IMPLEMENTED YET");
    }

    /**
     * Test of startEventFired method, of class NodeProgram.
     */
    @Test
    @Override
    public void testStartEventFired() {
        System.out.println("startEventFired : NOT IMPLEMENTED YET");
    }
    /**
     * Test of fireEndEvent method, of class Node.
     */
    @Test
    @Override
    public void testEndEventFired() {
        System.out.println("fireEndEvent : NOT IMPLEMENTED YET");
    }
}
