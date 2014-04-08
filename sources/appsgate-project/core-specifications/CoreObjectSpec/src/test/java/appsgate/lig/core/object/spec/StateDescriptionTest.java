package appsgate.lig.core.object.spec;


import java.util.Map;
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
            new StateDescription(null);
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
