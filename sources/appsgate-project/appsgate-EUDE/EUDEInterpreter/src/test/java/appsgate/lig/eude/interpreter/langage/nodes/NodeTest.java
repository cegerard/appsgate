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
import java.util.Iterator;
import junit.framework.Assert;
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
    public void setUp() throws Exception {

        this.instance = null;
        this.ruleJSON = new JSONObject();

    }

    /**
     * Test of stop method, of class NodeAction.
     *
     * @throws Exception
     */
    @Test
    public void testStop() throws Exception {
        printTestName("stop");
        this.instance.stop();
    }

    /**
     * Test of call method, of class NodeAction.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCall() throws Exception {
        printTestName("call");
        JSONObject expResult = null;
        JSONObject result = this.instance.call();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSymbolTable method, of class Node.
     */
    @Test
    public void testGetSymbolTable() {
        printTestName("getSymbolTable");
        SymbolTable expResult = null;
        SymbolTable result = this.instance.getSymbolTable();
        assertEquals(expResult, result);
    }

    /**
     * Test of startEventFired method, of class NodeProgram.
     */
    @Test
    public void testStartEventFired() {
        printTestName("startEventFired");
        StartEvent e = new StartEvent(this.instance);
        this.instance.startEventFired(e);
    }

    /**
     * Test of endEventFired method, of the same class.
     */
    @Test
    public void testEndEventFired() {
        printTestName("endEventFired");
        EndEvent e = new EndEvent(this.instance);
        this.instance.endEventFired(e);
    }

    @Test
    public void testGetInterpreter() {
        printTestName("GetInterpreter");
        try {
            this.instance.getInterpreter();
            fail("Should raise an exception");
        } catch (SpokExecutionException ex) {
            System.out.println("Exception catched");
        }
    }

    @Test
    public void testGetResult() throws Exception {
        printTestName("GetResult");
        assertNull(this.instance.getResult());
    }

    @Test
    public void testGetJSONDescription() throws JSONException {
        printTestName("GetJSONDescription");
        try {
            JSONObject jsonDescription = this.instance.getJSONDescription();
            Assert.assertTrue("Two Json Object should be equals", compareTo(this.ruleJSON, jsonDescription));
        } catch (UnsupportedOperationException e) {
            fail("It is supposed to be supported now");
        }
    }

    @Test
    public void testCopy() throws JSONException {
        printTestName("Copy");

        Node copy = this.instance.copy(null);
        assertNotNull(copy);
        Assert.assertTrue("Two copies should have same json description", compareTo(this.instance.getJSONDescription(), copy.getJSONDescription()));
    }

    /**
     * Method that compare two JSON Objects and tell where they differ
     *
     * @param orig
     * @param copy
     * @return
     * @throws JSONException
     */
    private boolean compareTo(JSONObject orig, JSONObject copy) throws JSONException {
        Boolean ret = true;
        Iterator k = orig.keys();
        while (k.hasNext()) {
            String key = (String) k.next();
            if (!copy.has(key)) {
                ret = false;
                System.out.println(key + " is in original, but not in copy");
            } else {
                if (orig.get(key).getClass() == JSONObject.class) {
                    return compareTo((JSONObject) orig.get(key), (JSONObject) copy.get(key));
                }
                if (!orig.get(key).toString().equals(copy.get(key).toString())) {
                    ret = false;
                    System.out.println("For key (" + key + "), contents is not equal:\n"
                            + orig.get(key).toString() + "\n"
                            + copy.getString(key).toString());
                }
            }
        }
        return ret;
    }

    protected void printTestName(String testName) {
        System.out.println(testName + " - " + this.getClass().getSimpleName());
    }

}
