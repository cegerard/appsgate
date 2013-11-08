/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.EndEventListener;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEventListener;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 *
 * @author jr
 */
public abstract class NodeTest {

    protected Mockery context = new Mockery(){{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    protected Node instance;
    protected JSONObject ruleJSON;
    protected EUDEInterpreterImpl interpreter;

    public NodeTest() {
        this.interpreter = context.mock(EUDEInterpreterImpl.class);

    }

    @BeforeClass
    public static void setUpClass() {   
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

        this.instance = null;
        this.ruleJSON = new JSONObject();
        try {
            ruleJSON.put("targetType", "test");
            ruleJSON.put("targetId", "test");
            ruleJSON.put("methodName", "test");
            ruleJSON.put("args", (Collection) null);
        } catch (JSONException ex) {
            System.out.println("JsonEx");
        }

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of stop method, of class NodeAction.
     *
     */
    @Test
    public void testStop() {
        System.out.println("stop");
        this.instance.stop();
    }

    /**
     * Test of call method, of class NodeAction.
     * @throws java.lang.Exception
     */
    @Test
    public void testCall() throws Exception {
        System.out.println("call");
        Integer expResult = null;
        Integer result = this.instance.call();
        assertEquals(expResult, result);
    }

    /**
     * Test of fireStartEvent method, of class Node.
     */
    @Test
    public void testFireStartEvent() {
        System.out.println("fireStartEvent");
        StartEvent e = new StartEvent(this.instance);
        this.instance.fireStartEvent(e);
    }

    /**
     * Test of fireEndEvent method, of class Node.
     */
    @Test
    public void testFireEndEvent() {
        System.out.println("fireEndEvent");
        EndEvent e = new EndEvent(this.instance);
        this.instance.fireEndEvent(e);
    }

    /**
     * Test of addStartEventListener method, of class Node.
     */
    @Test
    public void testAddStartEventListener() {
        System.out.println("addStartEventListener");
        StartEventListener listener = null;
        this.instance.addStartEventListener(listener);
    }

    /**
     * Test of removeStartEventListener method, of class Node.
     */
    @Test
    public void testRemoveStartEventListener() {
        System.out.println("removeStartEventListener");
        StartEventListener listener = null;
        this.instance.removeStartEventListener(listener);
    }

    /**
     * Test of addEndEventListener method, of class Node.
     */
    @Test
    public void testAddEndEventListener() {
        System.out.println("addEndEventListener");
        EndEventListener listener = null;
        this.instance.addEndEventListener(listener);
    }

    /**
     * Test of removeEndEventListener method, of class Node.
     */
    @Test
    public void testRemoveEndEventListener() {
        System.out.println("removeEndEventListener");
        EndEventListener listener = null;
        this.instance.removeEndEventListener(listener);
    }

    /**
     * Test of getSymbolTable method, of class Node.
     */
    @Test
    public void testGetSymbolTable() {
        System.out.println("getSymbolTable");
        SymbolTable expResult = null;
        SymbolTable result = this.instance.getSymbolTable();
        assertEquals(expResult, result);
    }

    /**
     * Test of getParentSymbolTable method, of class Node.
     */
    @Test
    public void testGetParentSymbolTable() {
        System.out.println("getParentSymbolTable");
        SymbolTable expResult = null;
        SymbolTable result = this.instance.getParentSymbolTable();
        assertEquals(expResult, result);
    }

    /**
     * Test of startEventFired method, of class NodeProgram.
     */
    @Test
    public void testStartEventFired() {
        System.out.println("startEventFired");
        StartEvent e = new StartEvent(this.instance);
        this.instance.startEventFired(e);
    }

    /**
     * Test of endEventFired method, of the same class.
     */
    @Test
    public void testEndEventFired() {
        System.out.println("endEventFired");
        EndEvent e = new EndEvent(this.instance);
        this.instance.endEventFired(e);
    }

}
