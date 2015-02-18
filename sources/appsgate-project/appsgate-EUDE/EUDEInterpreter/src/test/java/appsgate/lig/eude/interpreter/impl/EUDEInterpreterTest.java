/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.impl;

import appsgate.lig.chmi.spec.CHMIProxySpec;
import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.ehmi.spec.EHMIProxyMock;
import appsgate.lig.ehmi.spec.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.nodes.NodeEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.nodes.NodeActionTest;
import appsgate.lig.eude.interpreter.langage.nodes.NodeEventTest;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.eude.interpreter.spec.ProgramStateNotification;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class EUDEInterpreterTest {

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
    private CHMIProxySpec chmiProxy;
    private EUDEInterpreter instance;
    private final JSONObject programJSON;
    private EHMIProxyMock ehmiProxy;
    private final String programId = "pgm";

    public EUDEInterpreterTest() throws Exception {
        programJSON = TestUtilities.loadFileJSON("src/test/resources/prog/pgm.json");
    }

    @Before
    public void setUp() throws Exception {
        this.pull_service = context.mock(DataBasePullService.class);
        this.push_service = context.mock(DataBasePushService.class);
        this.chmiProxy = context.mock(CHMIProxySpec.class);
        this.ehmiProxy = new EHMIProxyMock("src/test/resources/jsonLibs/toto.json");
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
                allowing(pull_service).testDB();
                will(returnValue(true));

                allowing(push_service).testDB();
                will(returnValue(true));

                //contextHistory_push.pushData_change(this.getClass().getSimpleName(), "interpreter", "start", "stop", getProgramsDesc());


                allowing(pull_service).pullLastObjectVersion(with(any(String.class)));
                will(returnValue(null));
                allowing(push_service).pushData_change(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));

                allowing(push_service).pushData_add(with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
                will(returnValue(true));
                allowing(push_service).pushData_add(with(any(String.class)), with(any(String.class)), with(any(String.class)),  with(aNull(ArrayList.class)) );
                will(returnValue(true));
                allowing(push_service).pushData_remove(with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
                will(returnValue(true));
                allowing(chmiProxy).executeCommand(with("test"), with("testState"), with(any(JSONArray.class)));
                will(returnValue(gc));
                allowing(chmiProxy).executeCommand(with("test"), with(any(String.class)), with(any(JSONArray.class)));
                then(tested.is("Yes"));
                allowing(chmiProxy).executeCommand(with("flag1"), with(any(String.class)), with(any(JSONArray.class)));
                then(tested.is("flag1"));
                allowing(chmiProxy).executeCommand(with("flag2"), with(any(String.class)), with(any(JSONArray.class)));
                then(tested.is("flag2"));

                allowing(gc).run();
                allowing(gc).getReturn();
                will(returnValue(new Long(2)));

                allowing(chmiProxy).executeCommand(with("clock"), with("getCurrentTimeInMillis"), with(any(JSONArray.class)));

                allowing(chmiProxy).executeCommand(with(any(String.class)), with(any(String.class)), with(any(JSONArray.class)));
                will(returnValue(gc));
                allowing(chmiProxy).getDevicesDescription();
                will(returnValue(deviceList));

            }
        });
        this.instance = new EUDEInterpreter();
        this.instance.setTestMocks(pull_service, push_service, ehmiProxy);

    }

    /**
     * Test of newInst method, of class EUDEInterpreter.
     */
    @Test
    public void testNewInst() {
        System.out.println("newInst");
        instance.newInst();
    }

    /**
     * Test of deleteInst method, of class EUDEInterpreter.
     */
    @Test
    public void testDeleteInst() {
        System.out.println("deleteInst");
        instance.addProgram(programJSON);

        instance.deleteInst();
    }

    /**
     * Test of addProgram method, of class EUDEInterpreter.
     *
     * @throws org.json.JSONException
     */
    @Test
    public void testAddProgram() throws JSONException {
        System.out.println("addProgram");
        System.out.println("Empty program : "+programJSON.toString());
        boolean result = instance.addProgram(programJSON);
        System.out.println("result : "+result);

        instance.getNodeProgram(programId);
        Assert.assertTrue("Program should be added", result);
    }

    /**
     * Test of removeProgram method, of class EUDEInterpreter.
     *
     * @throws Exception
     */
    @Test
    public void testRemoveProgram() throws Exception {
        System.out.println("removeProgram");
        boolean result = instance.removeProgram("NoTEst");
        Assert.assertFalse("The program does not exist so there is no removing", result);
        instance.addProgram(programJSON);
        boolean remove = instance.removeProgram(programId);
        Assert.assertTrue("Program should be removed", remove);

    }

    /**
     * Test of callProgram method, of class EUDEInterpreter.
     */
    @Test
    public void testCallProgram() {
        System.out.println("callProgram");
        boolean result = instance.callProgram("noTest");
        Assert.assertFalse("No test have no program to call", result);
        instance.addProgram(programJSON);
        result = instance.callProgram(programId);
        Assert.assertTrue("Program should be called", result);
    }

    /**
     * Test of stopProgram method, of class EUDEInterpreter.
     */
    @Test
    public void testStopProgram() {
        System.out.println("stopProgram");
        boolean result = instance.stopProgram(programId);
        Assert.assertFalse("The program has not been called", result);
    }

    /**
     * Test of getListPrograms method, of class EUDEInterpreter.
     */
    @Test
    public void testGetListPrograms() {
        System.out.println("getListPrograms");
        HashMap<String, JSONObject> result = instance.getListPrograms();
        Assert.assertEquals(0, result.size());
    }

    /**
     * Test of isProgramActive method, of class EUDEInterpreter.
     */
    @Test
    public void testIsProgramActive() {
        System.out.println("isProgramActive");
        boolean expResult = false;
        boolean result = instance.isProgramActive(programId);
        Assert.assertFalse("No program has been activated now", result);
    }

    /**
     * Test of getNodeProgram method, of class EUDEInterpreter.
     */
    @Test
    public void testGetNodeProgram() {
        System.out.println("getNodeProgram");
        NodeProgram expResult = null;
        NodeProgram result = instance.getNodeProgram(programId);
        Assert.assertEquals("No test has been added to the mediator", expResult, result);
    }

    /**
     * Test of addNodeListening method, of class EUDEInterpreter.
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
     * Test of removeNodeListening method, of class EUDEInterpreter.
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
     * Test of notifyChanges method, of class EUDEInterpreter.
     */
    @Test
    public void testNotifyChanges() {
        System.out.println("notifyChanges");
        NotificationMsg notif = new ProgramStateNotification( "test", null, "test", null);
        NotificationMsg result = instance.notifyChanges(notif);
        Assert.assertNotNull(result);
    }

    /**
     * Test of endEventFired method, of class EUDEInterpreter.
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
     * Test of startEventFired method, of class EUDEInterpreter.
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
//    @Test
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
//    @Test
    public void testPrograms() throws Exception {
        System.out.println("Programs");

        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testPrograms.json")));
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testIf.json")));
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testFail_1.json")));

        
        System.out.println("*******************testPrograms*************************");
        System.out.println(instance.getNodeProgram("testPrograms").getExpertProgramScript());
        System.out.println("********************************************************");
        Assert.assertTrue(instance.callProgram("testPrograms"));

        System.out.println("********************testIf************************");
        System.out.println(instance.getNodeProgram("testIf").getExpertProgramScript());
        System.out.println("********************************************************");
        Assert.assertTrue(instance.callProgram("testIf"));

        System.out.println("********************program-4050************************");
        System.out.println(instance.getNodeProgram("program-4050").getExpertProgramScript());
        System.out.println("********************************************************");
        Assert.assertTrue(instance.callProgram("program-4050"));
        synchroniser.waitUntil(tested.is("Yes"), 500);
        
        //Assert.assertFalse(instance.isProgramActive("testPrograms"));
        Assert.assertFalse(instance.isProgramActive("testIf"));
        //Assert.assertTrue(instance.isProgramActive("program-4050"));
    }

    /**
     * To test whether when node is working
     *
     * @throws Exception
     */
//    @Test
    public void testWhen() throws Exception {
        System.out.println("When");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testWhen.json")));
        boolean callProgram = instance.callProgram("TestWhen");
        Assert.assertTrue(callProgram);
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        NodeProgram p = instance.getNodeProgram("TestWhen");
        Assert.assertNotNull(p);
        ehmiProxy.notifAll("1");
        synchroniser.waitUntil(tested.is("Yes"), 500);
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        System.out.println("become no");
        tested.become("no");
        ehmiProxy.notifAll("2");
        synchroniser.waitUntil(tested.is("Yes"), 500);
        tested.become("no");
        ehmiProxy.notifAll("3");
        synchroniser.waitUntil(tested.is("Yes"), 500);
        p.stop();
        Assert.assertEquals("Program should be deployed", NodeProgram.PROGRAM_STATE.DEPLOYED, p.getState());

    }

    /**
     * To test how the while node is working
     *
     * @throws Exception
     */
//    @Test
    public void testWhile() throws Exception {
        System.out.println("While test");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testWhile.json")));
        Assert.assertTrue(instance.callProgram("TestWhile"));
        Assert.assertTrue(instance.isProgramActive("TestWhile"));
        NodeProgram p = instance.getNodeProgram("TestWhile");
        Assert.assertNotNull(p);

        ehmiProxy.notifAll("1");
        synchroniser.waitUntil(tested.is("flag1"), 500);

        ehmiProxy.notifAll("2");
        synchroniser.waitUntil(tested.is("flag2"), 500);
        Assert.assertEquals("Program should be deployed", NodeProgram.PROGRAM_STATE.DEPLOYED, p.getState());
        p.stop();
        Assert.assertEquals("Program should be deployed", NodeProgram.PROGRAM_STATE.DEPLOYED, p.getState());

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
        ehmiProxy.notifAll("1");
    }

    @Test
    public void testSelect() throws Exception {
        System.out.println("Select");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/select.json")));
        boolean p = instance.callProgram("select-prog");
        Assert.assertTrue(p);

    }

//    @Test
    public void testWait() throws Exception {
        System.out.println("Wait");
        tested.become("before");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/wait.json")));
        boolean p = instance.callProgram("waitTest");
        Assert.assertTrue(p);
        synchroniser.waitUntil(tested.is("before"), 100);
        ehmiProxy.notifAll("time");
        synchroniser.waitUntil(tested.is("flag2"), 1200);
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
        ehmiProxy.notifAll("1");
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        Assert.assertTrue(instance.isProgramActive("pgm"));
        Assert.assertTrue(tested.isNot("yes").isActive());
        ehmiProxy.notifAll("2");
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
        ehmiProxy.notifAll("1");
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
        ehmiProxy.notifAll("2");
        Assert.assertTrue(instance.isProgramActive("TestWhen"));
//        Assert.fail("Fin");

    }

//    @Test
    public void testWhenImbricated() throws Exception {
        System.out.println("Stop And Start");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/testWhenImb.json")));
        System.out.println("Start");
        Assert.assertTrue(instance.callProgram("whenImb"));
        ehmiProxy.notifAll("1");
        ehmiProxy.notifAll("2");
        synchroniser.waitUntil(tested.is("Yes"), 200);

    }

    @Test
    public void testImbricatedPrograms() throws Exception {
        System.out.println("Imbricated");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/imbricated1.json")));
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/imbricated2.json")));
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/imbricated3.json")));
        Assert.assertFalse(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/imbricated3.json")));
        NodeProgram nodeProgram = instance.getNodeProgram("imb3");
        Assert.assertEquals("root.imb1.imb2", nodeProgram.getPath());
    }

}
