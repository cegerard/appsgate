/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.context.agregator.spec;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class StateDescriptionTest {

    public StateDescriptionTest() {
    }

    @Test
    public void testStateNull() {
        try {
            StateDescription st = new StateDescription(null);
            Assert.fail("An exception should have been raised");
        } catch (JSONException ex) {
            Assert.assertNotNull(ex);
        }
    }

    @Test
    public void testState() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("name", "test");
        o.put("setter", (Map) null);
        o.put("startEvent", (Map) null);
        o.put("endEvent", (Map) null);
        o.put("stateName", "t");
        o.put("stateValue", "true");
        StateDescription st = new StateDescription(o);
        Assert.assertNotNull(st);
    }

    @Test
    public void testNoName() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("setter", (Map) null);
        o.put("startEvent", (Map) null);
        o.put("endEvent", (Map) null);
        o.put("stateName", "t");
        o.put("stateValue", "true");
        StateDescription st;
        try {
            st = new StateDescription(o);
            Assert.fail("An exception should have been raised");

        } catch (JSONException ex) {
            Assert.assertNotNull(ex);

        }
    }

    @Test
    public void testWrong() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("name", "test");
        o.put("startEvent", (Map) null);
        o.put("endEvent", (Map) null);
        o.put("stateValue", "true");
        StateDescription st = new StateDescription(o);
        Assert.assertNotNull(st);
        Assert.assertNotNull(st.getEndEvent());
        Assert.assertNull(st.getSetter());
        Assert.assertEquals("true", st.getStateValue());
        Assert.assertTrue(st.getStateName().isEmpty());
    }
}
