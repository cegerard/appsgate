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
public class SocketIOHandlerTest {

    public SocketIOHandlerTest() {
    }

    @Test
    public void testSomeMethod() {
        SocketIOHandler socket = new SocketIOHandler();
        Assert.assertTrue(socket.sendPost("Debug", "testDirect"));
    }

}
