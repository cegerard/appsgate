/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeSeqEventTest extends NodeTest {

    public NodeSeqEventTest() {
    }

    @Before
    @Override
    public void setUp() {
        try {
            this.instance = new NodeSeqEvent(null, new JSONArray());
        } catch (JSONException ex) {
            Logger.getLogger(NodeSeqEventTest.class.getName()).log(Level.SEVERE, null, ex);
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
        System.out.println("startEventFired : NOT implemented YET");
    }


}
