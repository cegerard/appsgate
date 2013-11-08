/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import junit.framework.Assert;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeSeqAndBoolTest extends NodeTest {

    private NodeSeqAndBool seqTest;

    public NodeSeqAndBoolTest() {
    }

    @Before
    @Override
    public void setUp() {
        this.seqTest = new NodeSeqAndBool(null, new JSONArray());
        this.instance = this.seqTest;
    }

    /**
     * Test of getResult method, of class NodeSeqAndBool.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetResult() throws Exception {
        System.out.println("getResult");
        try {
            Boolean result = this.seqTest.getResult();
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
        System.out.println("undeploy : not implemented yet");
    }

    /**
     * Test of resume method, of class NodeAction.
     */
    @Test
    @Override
    public void testResume() {
        System.out.println("resume : not implemented yet");
    }

    /**
     * Test of getState method, of class NodeAction.
     */
    @Test
    @Override
    public void testGetState() {
        System.out.println("getState : not implemented yet");
    }

    /**
     * Test of startEventFired method, of class NodeProgram.
     */
    @Test
    @Override
    public void testStartEventFired() {
        System.out.println("startEventFired : NOT implemented YET");
    }
}
