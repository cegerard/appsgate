/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.context.agregator;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class LibraryTest {

    public LibraryTest() {
    }

    @Test
    public void testCreate() {
        Library lib = new Library();
        Assert.assertNotNull(lib);
    }

    @Test
    public void testGetEvents() throws JSONException {
        Library lib = new Library();
        JSONObject eventsFromState = lib.getEventsFromState("test", "isOn");
        Assert.assertNotNull(eventsFromState);
    }

}
