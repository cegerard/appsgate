/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeExpBoolTest extends NodeTest {

    private NodeExpBool expBoolTest;

    public NodeExpBoolTest() {
    }

    @Before
    @Override
    public void setUp() {
        JSONArray array = new JSONArray();
        this.expBoolTest = new NodeExpBool(null, array);
        this.instance = this.expBoolTest;
    }

    /**
     * Test of startEventFired method, of class NodeExpBool.
     */
    @Test
    public void testStartEventFired() {
        System.out.println("startEventFired");
        StartEvent e = new StartEvent(this.expBoolTest);
        this.expBoolTest.startEventFired(e);
    }

    /**
     * Test of endEventFired method, of class NodeExpBool.
     */
    @Test
    public void testEndEventFired() {
        System.out.println("endEventFired");
        EndEvent e = new EndEvent(this.expBoolTest);
        this.expBoolTest.endEventFired(e);
    }

}
