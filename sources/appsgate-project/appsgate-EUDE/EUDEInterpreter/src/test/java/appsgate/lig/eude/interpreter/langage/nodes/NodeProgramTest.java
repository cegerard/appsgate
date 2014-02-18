/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEMediator;
import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import appsgate.lig.eude.interpreter.impl.TestUtilities;
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

    public NodeProgramTest() throws JSONException {
        context.checking(new Expectations() {
            {
                allowing(mediator).notifyChanges(with(any(ProgramStateNotificationMsg.class)));
            }
        });

        ruleJSON.put("id", "test");
        ruleJSON.put("type", "program");
        ruleJSON.put("runningState", "STOPPED");
        ruleJSON.put("name", "test");
        ruleJSON.put("daemon", false);
        ruleJSON.put("package", "test");
        JSONObject header = new JSONObject();
        header.put("author", "toto");
        ruleJSON.put("header", header);
        JSONObject rules = new JSONObject();
        rules.put("type", "setOfRules");
        rules.put("rules", (Collection) null);
        ruleJSON.put("body", rules);
        ruleJSON.put("definitions", (Collection) null);
        ruleJSON.put("userSource", "test");

    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.programTest = new NodeProgram(mediator, this.ruleJSON, null);
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
     * Test of getuserSource method, of class NodeProgram.
     */
    @Test
    public void testGetuserSource() {
        System.out.println("getuserSource");
        String expResult = "test";
        String result = this.programTest.getUserSource();
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
        JSONObject rules = new JSONObject();
        rules.put("userSource", "test");
        rules.put("type", "instructions");
        rules.put("rules", new JSONArray());
        JSONObject header = new JSONObject();
        header.put("author", "toto");

        rules.put("header", header);

        rules.put("name", "test");
        rules.put("daemon", true);
        rules.put("body", rules);
        rules.put("definitions", (Collection) null);

        boolean expResult = true;
        boolean result = this.programTest.update(rules);
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
    public void testgetMediator() {
        System.out.println("getMediator");
        try {
            EUDEMediator i = this.instance.getMediator();
            Assert.assertNotNull(i);
        } catch (SpokExecutionException ex) {
            fail("Should not have raised an exception");
        }
    }

    @Test
    public void testFromJSONFile() throws Exception {
        printTestName("FromJSONFiles");
        NodeProgram defNode = new NodeProgram(null, TestUtilities.loadFileJSON("src/test/resources/node/newjson.json"), null);
        assertNotNull(defNode);
        System.out.println(defNode.getExpertProgramScript());
    }
}
