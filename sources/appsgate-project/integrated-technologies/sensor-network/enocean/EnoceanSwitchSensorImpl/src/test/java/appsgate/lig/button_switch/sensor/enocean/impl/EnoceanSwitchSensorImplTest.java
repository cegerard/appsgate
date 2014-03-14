package appsgate.lig.button_switch.sensor.enocean.impl;

import appsgate.lig.core.object.spec.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class EnoceanSwitchSensorImplTest extends CoreObjectBehaviorTest{
    
    public EnoceanSwitchSensorImplTest() {
        super(new EnoceanSwitchSensorImpl(), "");
    }
        @Test
    public void tests() {
        Assert.assertEquals("", this.testMethod());
    }


}
