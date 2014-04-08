package appsgate.lig.keycard.sensor.enocean.impl;

import appsgate.lig.core.tests.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class EnoceanKeyCardSensorImplTest extends CoreObjectBehaviorTest {

    public EnoceanKeyCardSensorImplTest() {
        super(new EnoceanKeyCardSensorImpl(), "");
    }

    @Test
    public void tests() {
        Assert.assertEquals(0, this.testMethod());
    }

}
