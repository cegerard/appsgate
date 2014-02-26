/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.impl;

import appsgate.lig.context.proxy.spec.ContextProxyMock;
import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.nodes.NodeEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.nodes.NodeActionTest;
import appsgate.lig.eude.interpreter.langage.nodes.NodeEventTest;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.router.spec.GenericCommand;
import appsgate.lig.router.spec.RouterApAMSpec;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;

import static org.jmock.Expectations.any;

import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author jr
 */
public class EUDEMediatorTest {

    /**
     *
     */
    protected Synchroniser synchroniser = new Synchroniser();

    /**
     *
     */
    protected Mockery context = new Mockery() {
        {
            setThreadingPolicy(synchroniser);
            setImposteriser(ClassImposteriser.INSTANCE);

        }
    };
    private States tested;
    private DataBasePullService pull_service;
    private DataBasePushService push_service;
    private RouterApAMSpec router;
    private EUDEMediator instance;
    private JSONObject programJSON;
    private ContextProxyMock contextProxy;

    public EUDEMediatorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        this.pull_service = context.mock(DataBasePullService.class);
        this.push_service = context.mock(DataBasePushService.class);
        this.router = context.mock(RouterApAMSpec.class);
        this.contextProxy = new ContextProxyMock("src/test/resources/jsonLibs/toto.json");
        final JSONArray deviceList = new JSONArray();
        JSONObject clock = new JSONObject();
        clock.put("id", "1");
        clock.put("type", 21);
        deviceList.put(clock);
        final JSONObject events = new JSONObject();
        JSONObject e = new JSONObject();
        e.put("name", "event");
        events.put("endEvent", e);
        events.put("startEvent", e);
        final NodeActionTest a = new NodeActionTest();

        final GenericCommand gc = context.mock(GenericCommand.class);

        tested = context.states("NotYet");
        context.checking(new Expectations() {
            {
                allowing(pull_service).pullLastObjectVersion(with(any(String.class)));
                will(returnValue(null));
                allowing(push_service).pushData_change(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
                allowing(push_service).pushData_add(with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
                will(returnValue(true));
                allowing(router).executeCommand(with("test"), with("testState"), with(any(JSONArray.class)));
                will(returnValue(gc));
                allowing(router).executeCommand(with("test"), with(any(String.class)), with(any(JSONArray.class)));
                then(tested.is("Yes"));
                allowing(router).executeCommand(with("flag1"), with(any(String.class)), with(any(JSONArray.class)));
                then(tested.is("flag1"));
                allowing(router).executeCommand(with("flag2"), with(any(String.class)), with(any(JSONArray.class)));
                then(tested.is("flag2"));

                allowing(gc).run();
                allowing(gc).getReturn();
                will(returnValue("2"));

                allowing(router).executeCommand(with(any(String.class)), with(any(String.class)), with(any(JSONArray.class)));
                allowing(router).getDevices();
                will(returnValue(deviceList));

            }
        });
        this.instance = new EUDEMediator();
        this.instance.setTestMocks(pull_service, push_service, router, contextProxy);
        programJSON = new JSONObject();
        programJSON.put("id", "test");

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of newInst method, of class EUDEMediator.
     */
    @Test
    public void testNewInst() {
        System.out.println("newInst");
        instance.newInst();
    }

    /**
     * Test of deleteInst method, of class EUDEMediator.
     */
    @Test
    public void testDeleteInst() {
        System.out.println("deleteInst");
        instance.deleteInst();
    }

    /**
     * Test of addProgram method, of class EUDEMediator.
     *
     * @throws org.json.JSONException
     */
    @Test
    public void testAddProgram() throws JSONException {
        System.out.println("addProgram");
        boolean expResult = false;
        boolean result = instance.addProgram(programJSON);
        assertEquals(expResult, result);
    }

    /**
     * Test of removeProgram method, of class EUDEMediator.
     */
    @Test
    public void testRemoveProgram() {
        System.out.println("removeProgram");
        String programId = "";
        boolean expResult = false;
        boolean result = instance.removeProgram(programId);
        assertEquals(expResult, result);
    }

    /**
     * Test of update method, of class EUDEMediator.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        boolean expResult = false;
        boolean result = instance.update(this.programJSON);
        assertEquals(expResult, result);
    }

    /**
     * Test of callProgram method, of class EUDEMediator.
     */
    @Test
    public void testCallProgram() {
        System.out.println("callProgram");
        String programId = "";
        boolean expResult = false;
        boolean result = instance.callProgram(programId);
        assertEquals(expResult, result);
    }

    /**
     * Test of stopProgram method, of class EUDEMediator.
     */
    @Test
    public void testStopProgram() {
        System.out.println("stopProgram");
        String programId = "";
        boolean expResult = false;
        boolean result = instance.stopProgram(programId);
        assertEquals(expResult, result);
    }

    /**
     * Test of pauseProgram method, of class EUDEMediator.
     */
    @Test
    public void testPauseProgram() {
        System.out.println("pauseProgram");
        String programId = "";
        boolean expResult = false;
        boolean result = instance.pauseProgram(programId);
        assertEquals(expResult, result);
    }

    /**
     * Test of getListPrograms method, of class EUDEMediator.
     */
    @Test
    public void testGetListPrograms() {
        System.out.println("getListPrograms");
        HashMap<String, JSONObject> result = instance.getListPrograms();
        assertEquals(1, result.size());
    }

    /**
     * Test of isProgramActive method, of class EUDEMediator.
     */
    @Test
    public void testIsProgramActive() {
        System.out.println("isProgramActive");
        String programId = "";
        boolean expResult = false;
        boolean result = instance.isProgramActive(programId);
        assertEquals(expResult, result);
    }

    /**
     * Test of getNodeProgram method, of class EUDEMediator.
     */
    @Test
    public void testGetNodeProgram() {
        System.out.println("getNodeProgram");
        String programId = "";
        NodeProgram expResult = null;
        NodeProgram result = instance.getNodeProgram(programId);
        assertEquals(expResult, result);
    }

    /**
     * Test of addNodeListening method, of class EUDEMediator.
     *
     * @throws org.json.JSONException
     * @throws SpokNodeException
     */
    @Test
    public void testAddNodeListening() throws Exception {
        System.out.println("addNodeListening");
        NodeEventTest t = new NodeEventTest();

        NodeEvent n = new NodeEvent(t.getRuleJSON(), null);
        instance.addNodeListening(n);
    }

    /**
     * Test of removeNodeListening method, of class EUDEMediator.
     *
     * @throws org.json.JSONException
     * @throws SpokNodeException
     */
    @Test
    public void testRemoveNodeListening() throws Exception {
        System.out.println("removeNodeListening");
        NodeEventTest t = new NodeEventTest();

        NodeEvent n = new NodeEvent(t.getRuleJSON(), null);

        instance.removeNodeListening(n);
    }

    /**
     * Test of notifyChanges method, of class EUDEMediator.
     */
    @Test
    public void testNotifyChanges() {
        System.out.println("notifyChanges");
        NotificationMsg notif = new ProgramStateNotificationMsg("test", "test", null);
        NotificationMsg result = instance.notifyChanges(notif);
        assertNotNull(result);
    }

    /**
     * Test of endEventFired method, of class EUDEMediator.
     *
     * @throws org.json.JSONException
     */
    @Test
    public void testEndEventFired() throws JSONException {
        System.out.println("endEventFired");
        EndEvent e = new EndEvent(new NodeProgram(this.instance, null));
        instance.endEventFired(e);
    }

    /**
     * Test of startEventFired method, of class EUDEMediator.
     */
    @Test
    public void testStartEventFired() {
        System.out.println("startEventFired");
        StartEvent e = null;
        instance.startEventFired(e);
    }

    /**
     * To test whether reading real program is working
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @throws JSONException
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testActions() throws Exception {
        System.out.println("Actions");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testActions.json")));
        Assert.assertTrue(instance.callProgram("testActions"));
        synchroniser.waitUntil(tested.is("Yes"), 500);
//        Assert.assertFalse(instance.isProgramActive("testActions"));

    }

    /**
     * To test whether reading real program is working
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @throws JSONException
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testPrograms() throws Exception {
        System.out.println("Programs");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testIf.json")));
        System.out.println(instance.getNodeProgram("testIf").getExpertProgramScript());

        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testPrograms.json")));
        System.out.println(instance.getNodeProgram("testPrograms").getExpertProgramScript());

        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testFail_1.json")));
        System.out.println(instance.getNodeProgram("program-4050").getExpertProgramScript());

        System.out.println("********************testIf************************");
        Assert.assertTrue(instance.callProgram("testIf"));
        System.out.println("*******************testPrograms*************************");
        Assert.assertTrue(instance.callProgram("testPrograms"));
        System.out.println("********************program-4050************************");
        Assert.assertTrue(instance.callProgram("program-4050"));
        synchroniser.waitUntil(tested.is("Yes"), 500);
        Assert.assertFalse(instance.isProgramActive("testPrograms"));
        Assert.assertFalse(instance.isProgramActive("testIf"));
        Assert.assertTrue(instance.isProgramActive("program-4050"));
    }

    /**
     * To test whether when node is working
     *
     * @throws Exception
     */
    @Test
    public void testWhen() throws Exception {
        System.out.println("When");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testWhen.json")));
        boolean callProgram = instance.callProgram("TestWhen");
        Assert.assertTrue(callProgram);
        contextProxy.notifAll("1");
        synchroniser.waitUntil(tested.is("Yes"), 500);
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        System.out.println("become no");
        tested.become("no");
        contextProxy.notifAll("2");
        synchroniser.waitUntil(tested.is("Yes"), 500);
        tested.become("no");
        contextProxy.notifAll("3");
        synchroniser.waitUntil(tested.is("Yes"), 500);

    }

    /**
     * To test how the while node is working
     *
     * @throws Exception
     */
    @Test
    public void testWhile() throws Exception {
        System.out.println("While test");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testWhile.json")));
        boolean p = instance.callProgram("TestWhile");
        Assert.assertTrue(p);
        contextProxy.notifAll("1");
        synchroniser.waitUntil(tested.is("flag1"), 500);
        contextProxy.notifAll("2");
        synchroniser.waitUntil(tested.is("flag2"), 500);
    }

    /**
     * To test how the while node is working
     *
     * @throws Exception
     */
    @Test
    public void testKeepState() throws Exception {
        System.out.println("Keep State test");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testKeepState.json")));
        boolean p = instance.callProgram("TestKeepState");
        Assert.assertTrue(p);
        contextProxy.notifAll("1");
    }

    @Test
    public void testSelect() throws Exception {
        System.out.println("Select");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/select.json")));
        
    }
    
    /**
     * To test whether reading real program is working
     *
     * @throws Exception
     */
    //@Test
    public void testPgm() throws Exception {
        System.out.println("Pgm calling TestWhen");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/pgm.json")));
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testWhen.json")));
        Assert.assertTrue(instance.callProgram("pgm"));
        Assert.assertFalse(instance.isProgramActive("TestWhen"));
        Assert.assertTrue(instance.isProgramActive("pgm"));
        contextProxy.notifAll("1");
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        Assert.assertTrue(instance.isProgramActive("pgm"));
        Assert.assertTrue(tested.isNot("yes").isActive());
        contextProxy.notifAll("2");
        synchroniser.waitUntil(tested.is("Yes"), 500);
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        Assert.assertTrue(instance.isProgramActive("pgm"));

    }

    //@Test
    public void testStopAndStart() throws Exception {
        System.out.println("Stop And Start");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testWhen.json")));
        System.out.println("Start");
        Assert.assertTrue(instance.callProgram("TestWhen"));
        System.out.println("Stop 1");
        Assert.assertTrue(instance.stopProgram("TestWhen"));
        System.out.println("Stop 2");
        Assert.assertTrue(instance.stopProgram("TestWhen"));
        Assert.assertFalse(instance.isProgramActive("TestWhen"));
        System.out.println("Start 2");
        Assert.assertTrue(instance.callProgram("TestWhen"));
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        contextProxy.notifAll("1");
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        contextProxy.notifAll("2");
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
//        Assert.fail("Fin");

    }

    @Test
    public void testWhenImbricated() throws Exception {
        System.out.println("Stop And Start");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testWhenImb.json")));
        System.out.println("Start");
        Assert.assertTrue(instance.callProgram("whenImb"));
        contextProxy.notifAll("1");
        contextProxy.notifAll("2");
        synchroniser.waitUntil(tested.is("Yes"), 200);

    }

    @Test
    public void testTreeImplementation() {
        System.out.println("Implementation");

    }

    @Test
    public void testIsThereARootProgram() {
        System.out.println("Test root program");
        Assert.assertNotNull("There must be a root program", instance.getNodeProgram("program-0"));
    }


}
