package appsgate.lig.plug.actuator_sensors.enocean.impl;

import appsgate.lig.core.tests.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class EnoceanPlugAcuatorSensorImplTest extends CoreObjectBehaviorTest {

    public EnoceanPlugAcuatorSensorImplTest() {
        super(new EnoceanPlugAcuatorSensorImpl(), "");
    }

    @Test
    public void tests() {
        Assert.assertEquals(0, this.testMethod());
    }

}
