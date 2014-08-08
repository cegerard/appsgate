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
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.jmock.Expectations;
import static org.jmock.Expectations.any;
import static org.jmock.Expectations.returnValue;
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
public class ReferenceTest {

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
    public void testBuildReferences() throws IOException, FileNotFoundException, JSONException  {
        System.out.println("References");
        instance.addProgram(TestUtilities.loadFileJSON("src/test/resources/prog/pgm.json"));
        NodeProgram p = instance.getNodeProgram("pgm");
        Assert.assertNotNull(p);
        ReferenceTable references = p.getReferences();
        Assert.assertNotNull(references);
        Set<String> devicesId = references.getDevicesId();
        Assert.assertEquals(1, devicesId.size());
        Set<String> programsId = references.getProgramsId();
        Assert.assertEquals(1, programsId.size());
        ReferenceTable.STATUS ret = references.checkReferences();
        Assert.assertEquals(ReferenceTable.STATUS.INVALID, ret);
    }
}
