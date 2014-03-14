package appsgate.lig.contact.sensor.enocean.impl;

import appsgate.lig.core.object.spec.CoreObjectBehaviorTest;
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
        Assert.assertEquals("", this.testMethod());
    }


}
