package appsgate.lig.contact.sensor.enocean.impl;

import appsgate.lig.core.tests.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class EnoceanContactSensorImplTest extends CoreObjectBehaviorTest {

    public EnoceanContactSensorImplTest() {
        super(new EnoceanContactSensorImpl(), "");
    }
    @Test
    public void tests() {
        Assert.assertEquals(0, this.testMethod());
    }


}
