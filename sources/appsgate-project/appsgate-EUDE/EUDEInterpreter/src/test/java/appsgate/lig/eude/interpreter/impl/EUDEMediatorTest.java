/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.impl;

import appsgate.lig.context.follower.listeners.CoreListener;
import appsgate.lig.context.follower.spec.ContextFollowerSpec;
import appsgate.lig.context.history.services.DataBasePullService;
import appsgate.lig.context.history.services.DataBasePushService;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.nodes.NodeEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.router.spec.GenericCommand;
import appsgate.lig.router.spec.RouterApAMSpec;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.lib.concurrent.Synchroniser;
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
        }
    };
    private States tested;
    private DataBasePullService pull_service;
    private DataBasePushService push_service;
    private RouterApAMSpec router;
    private ContextFollowerTest contextFollower;
    private EUDEMediator instance;
    private JSONObject programJSON;

    public EUDEMediatorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws JSONException {
        this.pull_service = context.mock(DataBasePullService.class);
        this.push_service = context.mock(DataBasePushService.class);
        this.router = context.mock(RouterApAMSpec.class);
        this.contextFollower = new ContextFollowerTest();

        final GenericCommand gc = new GenericCommand(null, null, this, null);
        tested = context.states("NotYet");
        context.checking(new Expectations() {
            {
                allowing(pull_service).pullLastObjectVersion(with(any(String.class)));
                will(returnValue(null));
                allowing(push_service).pushData_change(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
                allowing(push_service).pushData_add(with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
                will(returnValue(true));
                allowing(router).executeCommand(with("test"), with(any(String.class)), with(any(JSONArray.class)));
                then(tested.is("Yes"));
                allowing(router).executeCommand(with("flag1"), with(any(String.class)), with(any(JSONArray.class)));
                then(tested.is("flag1"));
                allowing(router).executeCommand(with("flag2"), with(any(String.class)), with(any(JSONArray.class)));
                then(tested.is("flag2"));
                allowing(router).executeCommand(with(any(String.class)), with(any(String.class)), with(any(JSONArray.class)));
            }
        });
        this.instance = new EUDEMediator();
        this.instance.setTestMocks(pull_service, push_service, router, contextFollower);
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
        assertEquals(0, result.size());
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
     * Test of executeCommand method, of class EUDEMediator.
     */
    @Test
    public void testExecuteCommand() {
        System.out.println("executeCommand");
        String objectId = "";
        String methodName = "";
        JSONArray args = new JSONArray();
        GenericCommand expResult = null;
        GenericCommand result = instance.executeCommand(objectId, methodName, args);
        assertEquals(expResult, result);
    }

    /**
     * Test of addNodeListening method, of class EUDEMediator.
     *
     * @throws org.json.JSONException
     * @throws SpokNodeException
     */
    @Test
    public void testAddNodeListening() throws JSONException, SpokNodeException {
        System.out.println("addNodeListening");
        JSONObject ruleJSON = new JSONObject();
        ruleJSON.put("sourceType", "test");
        ruleJSON.put("sourceId", "test");
        ruleJSON.put("eventName", "test");
        ruleJSON.put("eventValue", "test");

        NodeEvent n = new NodeEvent(ruleJSON, null);
        instance.addNodeListening(n);
    }

    /**
     * Test of removeNodeListening method, of class EUDEMediator.
     *
     * @throws org.json.JSONException
     * @throws SpokNodeException
     */
    @Test
    public void testRemoveNodeListening() throws JSONException, SpokNodeException {
        System.out.println("removeNodeListening");
        JSONObject ruleJSON = new JSONObject();
        ruleJSON.put("sourceType", "test");
        ruleJSON.put("sourceId", "test");
        ruleJSON.put("eventName", "test");
        ruleJSON.put("eventValue", "test");

        NodeEvent n = new NodeEvent(ruleJSON, null);
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
        EndEvent e = new EndEvent(new NodeProgram(this.instance));
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
    public void testActions() throws IOException, FileNotFoundException, JSONException, InterruptedException {
        System.out.println("Actions");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/testActions.json")));
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
    public void testPrograms() throws IOException, FileNotFoundException, JSONException, InterruptedException {
        System.out.println("Programs");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/testIf.json")));
        System.out.println(instance.getNodeProgram("testIf").getExpertProgramScript());

        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/testPrograms.json")));
        System.out.println(instance.getNodeProgram("testPrograms").getExpertProgramScript());

        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/testFail_1.json")));
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
     * To test whether reading real program is working
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @throws JSONException
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testWhen() throws IOException, FileNotFoundException, JSONException, InterruptedException {
        System.out.println("When");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/testWhen.json")));
        boolean callProgram = instance.callProgram("TestWhen");
        Assert.assertTrue(callProgram);
        contextFollower.notifAll("1");
        synchroniser.waitUntil(tested.is("Yes"), 500);
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        System.out.println("become no");
        tested.become("no");
        contextFollower.notifAll("2");
        synchroniser.waitUntil(tested.is("Yes"), 500);
        tested.become("no");
        contextFollower.notifAll("3");
        synchroniser.waitUntil(tested.is("Yes"), 500);

    }

    /**
     * To test whether reading real program is working
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @throws JSONException
     * @throws java.lang.InterruptedException
     */
    //@Test
    public void testPgm() throws Exception {
        System.out.println("Pgm calling TestWhen");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/pgm.json")));
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/testWhen.json")));
        Assert.assertTrue(instance.callProgram("pgm"));
        Assert.assertFalse(instance.isProgramActive("TestWhen"));
        Assert.assertTrue(instance.isProgramActive("pgm"));
        contextFollower.notifAll("1");
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        Assert.assertTrue(instance.isProgramActive("pgm"));
        Assert.assertTrue(tested.isNot("yes").isActive());
        contextFollower.notifAll("2");
        synchroniser.waitUntil(tested.is("Yes"), 500);
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        Assert.assertTrue(instance.isProgramActive("pgm"));

    }

    //@Test
    public void testStopAndStart() throws Exception {
        System.out.println("Stop And Start");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/testWhen.json")));
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
        contextFollower.notifAll("1");
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        contextFollower.notifAll("2");
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
//        Assert.fail("Fin");

    }

    @Test
    public void testWhenImbricated() throws Exception {
        System.out.println("Stop And Start");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/testWhenImb.json")));
        System.out.println("Start");
        Assert.assertTrue(instance.callProgram("whenImb"));
        contextFollower.notifAll("1");
        contextFollower.notifAll("2");
        synchroniser.waitUntil(tested.is("Yes"), 200);

    }

    /**
     * Class to make some tests on the events
     */
    public class ContextFollowerTest implements ContextFollowerSpec {

        private final ConcurrentLinkedQueue<CoreListener> list = new ConcurrentLinkedQueue<CoreListener>();

        @Override
        public void addListener(CoreListener coreListener) {
            list.add(coreListener);
            System.out.println("Listener added: " + coreListener.getObjectId());
        }

        @Override
        public void deleteListener(CoreListener coreListener) {
            System.out.println("removing listener: " + coreListener.getObjectId());
            list.remove(coreListener);
        }

        public void notifAll(String msg) {
            System.out.println("NotifAll Start " + msg);
            ConcurrentLinkedQueue<CoreListener> buf = new ConcurrentLinkedQueue<CoreListener>();
            for (CoreListener l : list) {
                buf.add(l);
            }
            for (CoreListener l1 : buf) {
                l1.notifyEvent();
            }
            System.out.println("NotifAll End " + msg);

        }

    }
}
