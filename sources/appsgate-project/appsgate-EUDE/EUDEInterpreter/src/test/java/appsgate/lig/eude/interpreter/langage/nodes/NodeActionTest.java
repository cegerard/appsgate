/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import org.junit.Before;
import static org.junit.Assert.*;

/**
 *
 * @author jr
 */
public class NodeActionTest extends NodeTest {

    private NodeAction actionTest;
    
    public NodeActionTest() {
    }


    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {
            this.actionTest = new NodeAction(null, ruleJSON);
            this.instance = this.actionTest;
        } catch (NodeException ex) {
            System.out.println("JSon Exception");
        }
    }

    /**
     * Test of getResult method, of class NodeAction.
     */
    @org.junit.Test
    public void testGetResult() {
        System.out.println("getResult");
        Object expResult = null;
        Object result = this.actionTest.getResult();
        assertEquals(expResult, result);
    }

}
