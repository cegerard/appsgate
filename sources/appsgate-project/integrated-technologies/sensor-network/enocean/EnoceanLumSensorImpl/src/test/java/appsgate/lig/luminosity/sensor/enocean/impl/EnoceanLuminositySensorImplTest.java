package appsgate.lig.luminosity.sensor.enocean.impl;

import appsgate.lig.core.tests.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class EnoceanLuminositySensorImplTest extends CoreObjectBehaviorTest {

    public EnoceanLuminositySensorImplTest() {
        super(new EnoceanLuminositySensorImpl(), "");
    }

    @Test
    public void tests() {
        Assert.assertEquals(0, this.testMethod());
    }

}
