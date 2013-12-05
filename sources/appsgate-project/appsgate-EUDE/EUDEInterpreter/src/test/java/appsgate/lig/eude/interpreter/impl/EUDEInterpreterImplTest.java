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
import appsgate.lig.eude.interpreter.langage.nodes.NodeException;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.router.spec.GenericCommand;
import appsgate.lig.router.spec.RouterApAMSpec;
import java.io.DataInputStream;
import java.io.FileInputStream;
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
public class EUDEInterpreterImplTest {

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
    private EUDEInterpreterImpl instance;
    private JSONObject programJSON;

    public EUDEInterpreterImplTest() {
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
        this.instance = new EUDEInterpreterImpl();
        this.instance.setTestMocks(pull_service, push_service, router, contextFollower);
        programJSON = new JSONObject();
        programJSON.put("id", "test");

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of newInst method, of class EUDEInterpreterImpl.
     */
    @Test
    public void testNewInst() {
        System.out.println("newInst");
        instance.newInst();
    }

    /**
     * Test of deleteInst method, of class EUDEInterpreterImpl.
     */
    @Test
    public void testDeleteInst() {
        System.out.println("deleteInst");
        instance.deleteInst();
    }

    /**
     * Test of addProgram method, of class EUDEInterpreterImpl.
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
     * Test of removeProgram method, of class EUDEInterpreterImpl.
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
     * Test of update method, of class EUDEInterpreterImpl.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        boolean expResult = false;
        boolean result = instance.update(this.programJSON);
        assertEquals(expResult, result);
    }

    /**
     * Test of callProgram method, of class EUDEInterpreterImpl.
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
     * Test of stopProgram method, of class EUDEInterpreterImpl.
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
     * Test of pauseProgram method, of class EUDEInterpreterImpl.
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
     * Test of getListPrograms method, of class EUDEInterpreterImpl.
     */
    @Test
    public void testGetListPrograms() {
        System.out.println("getListPrograms");
        HashMap<String, JSONObject> result = instance.getListPrograms();
        assertEquals(0, result.size());
    }

    /**
     * Test of isProgramActive method, of class EUDEInterpreterImpl.
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
     * Test of getNodeProgram method, of class EUDEInterpreterImpl.
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
     * Test of executeCommand method, of class EUDEInterpreterImpl.
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
     * Test of addNodeListening method, of class EUDEInterpreterImpl.
     *
     * @throws org.json.JSONException
     * @throws appsgate.lig.eude.interpreter.langage.nodes.NodeException
     */
    @Test
    public void testAddNodeListening() throws JSONException, NodeException {
        System.out.println("addNodeListening");
        JSONObject ruleJSON = new JSONObject();
        ruleJSON.put("sourceType", "test");
        ruleJSON.put("sourceId", "test");
        ruleJSON.put("eventName", "test");
        ruleJSON.put("eventValue", "test");

        NodeEvent n = new NodeEvent(this.instance, ruleJSON);
        instance.addNodeListening(n);
    }

    /**
     * Test of removeNodeListening method, of class EUDEInterpreterImpl.
     *
     * @throws org.json.JSONException
     * @throws appsgate.lig.eude.interpreter.langage.nodes.NodeException
     */
    @Test
    public void testRemoveNodeListening() throws JSONException, NodeException {
        System.out.println("removeNodeListening");
        JSONObject ruleJSON = new JSONObject();
        ruleJSON.put("sourceType", "test");
        ruleJSON.put("sourceId", "test");
        ruleJSON.put("eventName", "test");
        ruleJSON.put("eventValue", "test");

        NodeEvent n = new NodeEvent(this.instance, ruleJSON);
        instance.removeNodeListening(n);
    }

    /**
     * Test of notifyChanges method, of class EUDEInterpreterImpl.
     */
    @Test
    public void testNotifyChanges() {
        System.out.println("notifyChanges");
        NotificationMsg notif = new ProgramStateNotificationMsg("test", "test", null);
        NotificationMsg result = instance.notifyChanges(notif);
        assertNotNull(result);
    }

    /**
     * Test of endEventFired method, of class EUDEInterpreterImpl.
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
     * Test of startEventFired method, of class EUDEInterpreterImpl.
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
        Assert.assertTrue(instance.addProgram(loadFileJSON("src/test/resources/testActions.json")));
        Assert.assertTrue(instance.callProgram("testActions"));
        synchroniser.waitUntil(tested.is("Yes"), 500);
        Assert.assertFalse(instance.isProgramActive("testActions"));

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
        Assert.assertTrue(instance.addProgram(loadFileJSON("src/test/resources/testIf.json")));
        Assert.assertTrue(instance.addProgram(loadFileJSON("src/test/resources/testPrograms.json")));
        Assert.assertTrue(instance.addProgram(loadFileJSON("src/test/resources/testFail.json")));
        Assert.assertTrue(instance.addProgram(loadFileJSON("src/test/resources/testFail_1.json")));
        Assert.assertTrue(instance.callProgram("testPrograms"));
        Assert.assertTrue(instance.callProgram("program-373"));
        Assert.assertTrue(instance.callProgram("program-4050"));
        synchroniser.waitUntil(tested.is("Yes"), 500);
        Assert.assertFalse(instance.isProgramActive("testPrograms"));
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
        Assert.assertTrue(instance.addProgram(loadFileJSON("src/test/resources/testWhen.json")));
        Assert.assertTrue(instance.callProgram("TestWhen"));
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
    @Test
    public void testPgm() throws IOException, FileNotFoundException, JSONException, InterruptedException {
        System.out.println("Pgm");
        Assert.assertTrue(instance.addProgram(loadFileJSON("src/test/resources/pgm.json")));
        Assert.assertTrue(instance.addProgram(loadFileJSON("src/test/resources/testIf.json")));
        Assert.assertTrue(instance.addProgram(loadFileJSON("src/test/resources/testWhen.json")));
        Assert.assertTrue(instance.callProgram("TestWhen"));
        Assert.assertTrue(instance.callProgram("pgm"));
        Assert.assertTrue(instance.callProgram("testIF"));
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        Assert.assertFalse(instance.isProgramActive("pgm"));
        Assert.assertTrue(instance.isProgramActive("testIF"));
    }

    /**
     * Load a file and return its content
     *
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject loadFileJSON(String filename) throws FileNotFoundException, IOException, JSONException {
        FileInputStream fis = new FileInputStream(filename);
        DataInputStream dis = new DataInputStream(fis);

        byte[] buf = new byte[dis.available()];
        dis.readFully(buf);

        String fileContent = "";
        for (byte b : buf) {
            fileContent += (char) b;
        }

        dis.close();
        fis.close();

        return new JSONObject(fileContent);
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
