package appsgate.lig.light.actuator.philips.HUE.impl;
import appsgate.lig.core.object.spec.CoreObjectBehaviorTest;
import org.junit.Assert;

import org.junit.Test;

/**
 *
 * @author jr
 */
public class PhilipsHUEImplTest extends CoreObjectBehaviorTest{
    
    public PhilipsHUEImplTest() {
        super(new PhilipsHUEImpl(), "");
    }
    
    @Test
    public void tests() {
        Assert.assertEquals("", this.testMethod());
    }

    
}
