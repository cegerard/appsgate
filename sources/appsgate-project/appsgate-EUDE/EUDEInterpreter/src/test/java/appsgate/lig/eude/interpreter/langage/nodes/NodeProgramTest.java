/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import java.util.Collection;
import junit.framework.Assert;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jr
 */
public class NodeProgramTest extends NodeTest {

    private NodeProgram programTest;

    public NodeProgramTest() {
        context.checking(new Expectations() {
            {
                allowing(interpreter).notifyChanges(with(any(ProgramStateNotificationMsg.class)));
            }
        });

    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ruleJSON.put("type","program");
        ruleJSON.put("id", "test");
        ruleJSON.put("runningState", "STOPPED");
        ruleJSON.put("userInputSource", "test");
        JSONObject rules = new JSONObject();
        rules.put("type", "instructions");
        rules.put("rules", new JSONArray());
        JSONObject source = new JSONObject();
        source.put("programName", "test");
        source.put("daemon", false);
        source.put("seqRules", rules);
        source.put("seqDefinitions", (Collection) null);
        ruleJSON.put("source", source);

        this.programTest = new NodeProgram(interpreter, this.ruleJSON);

        this.instance = this.programTest;
    }

    /**
     * Test of isDeamon method, of class NodeProgram.
     */
    @Test
    public void testIsDeamon() {
        System.out.println("isDeamon");
        boolean expResult = false;
        boolean result = this.programTest.isDaemon();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserInputSource method, of class NodeProgram.
     */
    @Test
    public void testGetUserInputSource() {
        System.out.println("getUserInputSource");
        String expResult = "test";
        String result = this.programTest.getUserInputSource();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProgramJSON method, of class NodeProgram.
     */
    @Test
    public void testGetProgramJSON() {
        System.out.println("getProgramJSON");
        JSONObject expResult = this.ruleJSON;
        JSONObject result = this.programTest.getProgramJSON();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRunningState method, of class NodeProgram.
     */
    @Test
    public void testGetRunningState() {
        System.out.println("getRunningState");
        NodeProgram.RUNNING_STATE expResult = NodeProgram.RUNNING_STATE.STOPPED;
        NodeProgram.RUNNING_STATE result = this.programTest.getRunningState();
        assertEquals(expResult, result);
    }

    /**
     * Test of update method, of class NodeProgram.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testUpdate() throws Exception {
        System.out.println("update");
        JSONObject jsonProgram = new JSONObject();
        jsonProgram.put("userInputSource", "test");
        JSONObject rules = new JSONObject();
        rules.put("type", "instructions");
        rules.put("rules", new JSONArray());

        JSONObject src = new JSONObject();
        src.put("programName", "test");
        src.put("author", "test");
        src.put("target", "test");
        src.put("daemon", true);
        src.put("seqRules", rules);
        src.put("seqDefinitions", (Collection) null);
        jsonProgram.put("source", src);

        boolean expResult = true;
        boolean result = this.programTest.update(jsonProgram);
        assertEquals(expResult, result);
    }

    /**
     * Test of pause method, of class NodeProgram.
     */
    @Test
    public void testPause() {
        System.out.println("pause");
        boolean expResult = false;
        boolean result = this.programTest.pause();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDeployed method, of class NodeProgram.
     *
     * @throws org.json.JSONException
     */
    @Test
    public void testSetDeployed() throws JSONException {
        System.out.println("setDeployed");
        this.programTest.setDeployed();
    }

    /**
     * Test of call method.
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void testCall() throws Exception {
        System.out.println("call");

        JSONObject result = this.instance.call();
        assertNotNull(result);
    }

    /**
     * Test of getSymbolTable method, of class Node.
     */
    @Test
    @Override
    public void testGetSymbolTable() {
        System.out.println("getSymbolTable");
        SymbolTable result = this.instance.getSymbolTable();
        assertNotNull(result);
    }

    @Test
    @Override
    public void testGetInterpreter() {
        System.out.println("GetInterpreter");
        try {
            EUDEInterpreterImpl i = this.instance.getInterpreter();
            Assert.assertNotNull(i);
        } catch (SpokExecutionException ex) {
            fail("Should not have raised an exception");
        }
    }
}
