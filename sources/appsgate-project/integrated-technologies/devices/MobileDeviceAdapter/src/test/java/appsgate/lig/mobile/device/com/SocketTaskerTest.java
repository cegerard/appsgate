/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.mobile.device.com;

import org.junit.Test;
import org.junit.Assert;

/**
 *
 * @author jr
 */
public class SocketTaskerTest {
    
    public SocketTaskerTest() {
    }

    @Test
    public void testSomeMethod() {
        SocketTasker s = new SocketTasker();
        Assert.assertNotNull("Socket Tasker has not been created", s);
        s.connect();
        s.identifyAndSubscribe();
        //Assert.assertTrue("socket is not connected", s.isConnected());
        
    }
    
}
