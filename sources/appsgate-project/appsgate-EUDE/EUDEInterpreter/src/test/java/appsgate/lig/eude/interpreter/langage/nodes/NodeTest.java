/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 *
 * @author jr
 */
public abstract class NodeTest {

    protected Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    protected Node instance;
    protected JSONObject ruleJSON;
    protected EUDEInterpreterImpl interpreter;

    public NodeTest() {
        this.interpreter = context.mock(EUDEInterpreterImpl.class);

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

    /**
     * Test of stop method, of class NodeAction.
     *
     * @throws Exception
     */
    @Test
    public void testStop() throws Exception {
        System.out.println("stop");
        this.instance.stop();
    }

    /**
     * Test of call method, of class NodeAction.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCall() throws Exception {
        System.out.println("call");
        JSONObject expResult = null;
        JSONObject result = this.instance.call();
        assertEquals(expResult, result);
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

    @Test
    public void testGetInterpreter() {
        System.out.println("GetInterpreter");
        try {
            this.instance.getInterpreter();
            fail("Should raise an exception");
        } catch (SpokExecutionException ex) {
            System.out.println("Exception catched");
        }
    }

    @Test
    public void testGetResult() throws Exception {
        System.out.println("GetInterpreter");
        assertNull(this.instance.getResult());
    }
    
    @Test
    public void testGetJSONDescription() {
        System.out.println("GetJSONDescription");
        try {
            this.instance.getJSONDescription();
            fail("It is not supposed to be supported right now");
        } catch (UnsupportedOperationException e) {
            System.out.println("Exception catched");
        }
    }
}
