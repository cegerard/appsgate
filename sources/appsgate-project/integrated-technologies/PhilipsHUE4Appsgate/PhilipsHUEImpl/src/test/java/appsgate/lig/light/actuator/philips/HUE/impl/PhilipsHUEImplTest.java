package appsgate.lig.light.actuator.philips.HUE.impl;

import appsgate.lig.core.tests.CoreObjectBehaviorTest;
import org.junit.Assert;

import org.junit.Test;

/**
 *
 * @author jr
 */
public class PhilipsHUEImplTest extends CoreObjectBehaviorTest {

    public PhilipsHUEImplTest() {
        super(new PhilipsHUEImpl(), "");
    }

    @Test
    public void tests() {
        Assert.assertEquals("Mistakes ", 0, this.testMethod());
    }

}
