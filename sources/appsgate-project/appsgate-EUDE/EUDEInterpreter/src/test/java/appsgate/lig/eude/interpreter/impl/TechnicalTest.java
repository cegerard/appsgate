/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.impl;

import appsgate.lig.context.proxy.spec.ContextProxyMock;
import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.router.spec.GenericCommand;
import appsgate.lig.router.spec.RouterApAMSpec;
import java.util.ArrayList;
import java.util.Map;

import org.jmock.Expectations;

import static org.jmock.Expectations.any;

import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 *
 * @author jr
 */
public class TechnicalTest {

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
    private ContextProxyMock contextProxy;

    public TechnicalTest() throws Exception{
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

        final GenericCommand gc = context.mock(GenericCommand.class);

        tested = context.states("NotYet");
        context.checking(new Expectations() {
            {
                allowing(pull_service).pullLastObjectVersion(with(any(String.class)));
                will(returnValue(null));
                allowing(push_service).pushData_change(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
                allowing(push_service).pushData_add(with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
                will(returnValue(true));
                allowing(push_service).pushData_remove(with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
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

    }

    /**
     * To test whether reading real program is working
     *
     * @throws Exception
     */
    @Test
    public void test1() throws Exception {
        System.out.println("Test 1");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/techniques/t1.json")));
    }
    /**
     * To test whether reading real program is working
     *
     * @throws Exception
     */
    @Test
    public void test2() throws Exception {
        System.out.println("Test 2");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/techniques/t2.json")));
    }
    /**
     * To test whether reading real program is working
     *
     * @throws Exception
     */
    @Test
    public void test3() throws Exception {
        System.out.println("Test 3");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/techniques/t3.json")));
    }
    /**
     * To test whether reading real program is working
     *
     * @throws Exception
     */
    @Test
    public void test4() throws Exception {
        System.out.println("Test 4");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/techniques/t4.json")));
    }
    /**
     * To test whether reading real program is working
     *
     * @throws Exception
     */
    @Test
    public void test5() throws Exception {
        System.out.println("Test 5");
            Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/techniques/t5.json")));
    }

}
