/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.temperature.sensor.enocean.impl;

import appsgate.lig.core.object.spec.CoreObjectBehaviorTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class EnoceanTemperatureSensorImplTest extends CoreObjectBehaviorTest {

    public EnoceanTemperatureSensorImplTest() {
        super(new EnoceanTemperatureSensorImpl(), "");
    }
        @Test
    public void tests() {
        Assert.assertEquals("", this.testMethod());
    }



}
