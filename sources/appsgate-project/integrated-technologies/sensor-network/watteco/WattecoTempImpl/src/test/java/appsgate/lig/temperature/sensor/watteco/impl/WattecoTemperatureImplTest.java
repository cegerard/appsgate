package appsgate.lig.temperature.sensor.watteco.impl;

import appsgate.lig.core.object.spec.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class WattecoTemperatureImplTest extends CoreObjectBehaviorTest {

    public WattecoTemperatureImplTest() {
        super(new WattecoTemperatureImpl(), "");
    }

    @Test
    public void tests() {
        Assert.assertEquals("", this.testMethod());
    }

}
