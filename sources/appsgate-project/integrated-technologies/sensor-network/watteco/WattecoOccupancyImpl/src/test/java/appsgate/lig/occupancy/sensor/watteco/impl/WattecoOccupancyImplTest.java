package appsgate.lig.occupancy.sensor.watteco.impl;

import appsgate.lig.core.object.spec.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class WattecoOccupancyImplTest extends CoreObjectBehaviorTest {

    public WattecoOccupancyImplTest() {
        super(new WattecoOccupancyImpl(), "");
    }

    @Test
    public void tests() {
        Assert.assertEquals("", this.testMethod());
    }

}
