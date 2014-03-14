package appsgate.lig.plug.actuator_sensors.enocean.impl;

import appsgate.lig.core.object.spec.CoreObjectBehaviorTest;
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
        Assert.assertEquals("", this.testMethod());
    }

}
