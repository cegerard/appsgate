/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.ProgramStateNotificationMsg;
import java.util.Collection;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
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
    public void setUp() {
        super.setUp();
        try {

            ruleJSON.put("id", "test");
            ruleJSON.put("runningState", "STOPPED");
            ruleJSON.put("userInputSource", "test");
            JSONObject source = new JSONObject();
            source.put("programName", "test");
            source.put("author", "test");
            source.put("target", "test");
            source.put("daemon", false);
            source.put("seqRules", (Collection) null);
            ruleJSON.put("source", source);

            this.programTest = new NodeProgram(interpreter, this.ruleJSON);

            this.instance = this.programTest;
        } catch (JSONException ex) {
            System.out.println("JSON Ex : " + ex.getMessage());
        } catch (NodeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Test of setDaemon method, of class NodeProgram.
     */
    @Test
    public void testSetDaemon() {
        System.out.println("setDaemon");
        this.programTest.setDaemon(true);
        assertTrue(this.programTest.isDaemon());
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
     * Test of setRunningState method, of class NodeProgram.
     */
    @Test
    public void testSetRunningState() {
        System.out.println("setRunningState");
        NodeProgram.RUNNING_STATE runningState = NodeProgram.RUNNING_STATE.STOPPED;
        this.programTest.setRunningState(runningState);
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
        JSONObject src = new JSONObject();
        src.put("programName", "test");
        src.put("author", "test");
        src.put("target", "test");
        src.put("daemon", true);
        src.put("seqRules", (Collection) null);
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
     * Test of call method, of class NodeAction.
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void testCall() throws Exception {
        System.out.println("call");
        Integer expResult = 1;
        Integer result = this.instance.call();
        assertEquals(expResult, result);
    }
}
