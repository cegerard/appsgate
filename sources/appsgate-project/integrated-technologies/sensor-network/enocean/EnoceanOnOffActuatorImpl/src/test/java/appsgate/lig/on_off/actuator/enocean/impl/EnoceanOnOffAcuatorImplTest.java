package appsgate.lig.on_off.actuator.enocean.impl;

import appsgate.lig.core.tests.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class EnoceanOnOffAcuatorImplTest extends CoreObjectBehaviorTest {

    public EnoceanOnOffAcuatorImplTest() {
        super(new EnoceanOnOffAcuatorImpl(), "");
    }
    @Test
    public void tests() {
        Assert.assertEquals(0, this.testMethod());
    }


}
