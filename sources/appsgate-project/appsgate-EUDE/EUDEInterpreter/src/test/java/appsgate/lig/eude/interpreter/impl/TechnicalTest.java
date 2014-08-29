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
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;

import java.io.File;
import java.io.FilenameFilter;
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
    private CHMIProxySpec chmiProxy;
    private EUDEInterpreter instance;
    private EHMIProxyMock ehmiProxy;

    private final File[] listFiles;

    public TechnicalTest() {

        final File folder = new File("src/test/resources/techniques/");
        listFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        });

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

        final GenericCommand gc = context.mock(GenericCommand.class);

        tested = context.states("NotYet");
        context.checking(new Expectations() {
            {
                allowing(pull_service).testDB();
                will(returnValue(true));

                allowing(push_service).testDB();
                will(returnValue(true));
                allowing(pull_service).pullLastObjectVersion(with(any(String.class)));
                will(returnValue(null));
                allowing(push_service).pushData_change(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
                allowing(push_service).pushData_add(with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
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
                will(returnValue("2"));

                allowing(chmiProxy).executeCommand(with(any(String.class)), with(any(String.class)), with(any(JSONArray.class)));
                allowing(chmiProxy).getDevices();
                will(returnValue(deviceList));

            }
        });
        this.instance = new EUDEInterpreter();
        this.instance.setTestMocks(pull_service, push_service, ehmiProxy);

    }

    @Test
    public void testReadProgram() throws Exception {
        for (File f : listFiles) {
            System.out.println("Reading " + f.getName());
            JSONObject o = TestUtilities.loadFileJSON(f.getPath());
            Assert.assertNotNull(o);
            NodeProgram p = new NodeProgram(instance, o, null);
            Assert.assertNotNull(p);
        }
    }

    /**
     * To test if reading real program is working
     *
     * @throws Exception
     */
    @Test
    public void test1() throws Exception {
        System.out.println("Test 1");
        Assert.assertTrue(instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/techniques/t01.json")));
    }


}
