package appsgate.lig.smartplug.sensor.watteco.impl;

import appsgate.lig.core.object.spec.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class WattecoSmartPlugImplTest extends CoreObjectBehaviorTest {

    public WattecoSmartPlugImplTest() {
        super(new WattecoSmartPlugImpl(), "");
    }
    @Test
    public void tests() {
        Assert.assertEquals("", this.testMethod());
    }


}
