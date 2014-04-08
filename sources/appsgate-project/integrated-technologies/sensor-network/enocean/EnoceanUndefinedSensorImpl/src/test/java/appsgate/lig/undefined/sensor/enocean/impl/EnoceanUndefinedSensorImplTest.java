package appsgate.lig.undefined.sensor.enocean.impl;

import appsgate.lig.core.tests.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class EnoceanUndefinedSensorImplTest extends CoreObjectBehaviorTest {

    public EnoceanUndefinedSensorImplTest() {
        super(new EnoceanUndefinedSensorImpl(), "");
    }
    @Test
    public void tests() {
        Assert.assertEquals(0, this.testMethod());
    }


}
