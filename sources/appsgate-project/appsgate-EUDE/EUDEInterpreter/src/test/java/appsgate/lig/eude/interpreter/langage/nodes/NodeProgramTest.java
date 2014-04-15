/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreter;
import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import appsgate.lig.eude.interpreter.impl.TestUtilities;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import java.util.Collection;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        ruleJSON = new JSONObject();
        ruleJSON.put("id", "test");
        ruleJSON.put("type", "program");
        ruleJSON.put("runningState", "STOPPED");
        ruleJSON.put("name", "test");
        ruleJSON.put("package", "test");
        JSONObject header = new JSONObject();
        header.put("author", "toto");
        ruleJSON.put("header", header);
        JSONObject rules = new JSONObject();
        rules.put("type", "setOfRules");
        rules.put("rules", (Collection) null);
        rules.put("iid", "set");
        ruleJSON.put("body", rules);
        ruleJSON.put("definitions", (Collection) null);
        ruleJSON.put("userSource", "test");

    }

    @Before
    public void setUp() throws Exception {
        this.programTest = new NodeProgram(mediator, this.ruleJSON, null);
        this.instance = this.programTest;
    }

    /**
     * Test of getuserSource method, of class NodeProgram.
     */
    @Test
    public void testGetuserSource() {
        System.out.println("getuserSource");
        String expResult = "test";
        String result = this.programTest.getUserSource();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getRunningState method, of class NodeProgram.
     */
    @Test
    public void testGetRunningState() {
        System.out.println("getRunningState");
        NodeProgram.RUNNING_STATE expResult = NodeProgram.RUNNING_STATE.STOPPED;
        NodeProgram.RUNNING_STATE result = this.programTest.getRunningState();
        Assert.assertEquals(expResult, result);
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
        rules.put("type", "seqRules");
        rules.put("rules", new JSONArray());
        JSONObject header = new JSONObject();
        header.put("author", "toto");

        rules.put("header", header);

        rules.put("name", "test");
        rules.put("body", rules);
        rules.put("definitions", (Collection) null);

        boolean expResult = true;
        boolean result = this.programTest.update(rules);
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of pause method, of class NodeProgram.
     */
    @Test
    public void testPause() {
        System.out.println("pause");
        boolean expResult = false;
        boolean result = this.programTest.pause();
        Assert.assertEquals(expResult, result);
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
        Assert.assertNotNull(result);
    }

    /**
     * Test of getSymbolTable method, of class Node.
     */
    @Test
    @Override
    public void testGetSymbolTable() {
        System.out.println("getSymbolTable");
        SymbolTable result = this.instance.getSymbolTable();
        Assert.assertNotNull(result);
    }

    @Test
    public void testgetMediator() {
        System.out.println("getMediator");
        try {
            EUDEInterpreter i = this.instance.getMediator();
            Assert.assertNotNull(i);
        } catch (SpokExecutionException ex) {
            Assert.fail("Should not have raised an exception");
        }
    }

    @Test
    public void testFromJSONFile() throws Exception {
        printTestName("FromJSONFiles");
        NodeProgram defNode = new NodeProgram(null, TestUtilities.loadFileJSON("src/test/resources/node/newjson.json"), null);
        Assert.assertNotNull(defNode);
        System.out.println(defNode.getExpertProgramScript());
    }

    @Test
    public void testGetPath() throws Exception {
        printTestName("getPath");
        NodeProgram p1 = new NodeProgram(null, null);
        p1.setId("t");
        Assert.assertEquals("", p1.getPath());
        NodeProgram p2 = new NodeProgram(null, p1);
        p2.setId("c");
        Assert.assertEquals("t", p2.getPath());
        NodeProgram p3 = new NodeProgram(null, p2);
        p3.setId("l");
        Assert.assertEquals("t.c", p3.getPath());

    }
}
