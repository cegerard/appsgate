package appsgate.lig.light.actuator.philips.HUE.impl;

import appsgate.lig.chmi.spec.CHMIProxySpec;
import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.core.tests.CoreObjectBehaviorTest;
import appsgate.lig.ehmi.spec.EHMIProxyMock;
import appsgate.lig.proxy.PhilipsHUE.interfaces.PhilipsHUEServices;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;

import org.junit.Test;

/**
 *
 * @author jr
 */
public class PhilipsHUEImplTest extends CoreObjectBehaviorTest {
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
    
    private PhilipsHUEServices p;
    
    @Before
    public void setUp() throws Exception {
        final JSONObject o = new JSONObject();
        o.put("state", new JSONObject());
        p = context.mock(PhilipsHUEServices.class);
        context.checking(new Expectations() {
            {
                allowing(p).getLightState(with(any(String.class)), with(any(String.class)));
                will(returnValue(o));
                allowing(p).getLightState(null, null);
                will(returnValue(o));
                allowing(p).setAttribute(with(aNull(String.class)), with(aNull(String.class)), with(any(JSONObject.class)));
                will(returnValue(true));
            }
        });
        this.lamp.setBridge(p);
    }
    /**
     * 
     */
    private final PhilipsHUEImpl lamp;

    /**
     * 
     */
    public PhilipsHUEImplTest() {
        super(new PhilipsHUEImpl(), "");
        lamp = new PhilipsHUEImpl();
    }

    @Test
    public void tests() {
        Assert.assertEquals("Mistakes ", 0, this.testMethod());
    }
    
    @Test
    public void testgetHTMLColor() {
        lamp.setBlue();
        String c = lamp.getHTMLColor();
        Assert.assertEquals("#B50000", c);
    }

}
