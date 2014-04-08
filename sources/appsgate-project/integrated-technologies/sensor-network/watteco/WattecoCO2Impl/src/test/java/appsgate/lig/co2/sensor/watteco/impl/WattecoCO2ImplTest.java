package appsgate.lig.co2.sensor.watteco.impl;

import appsgate.lig.core.tests.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class WattecoCO2ImplTest extends CoreObjectBehaviorTest {

    public WattecoCO2ImplTest() {
        super(new WattecoCO2Impl(), "");
    }
    @Test
    public void tests() {
        Assert.assertEquals(0, this.testMethod());
    }


}
