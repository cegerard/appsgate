package appsgate.lig.ehmi.trace;

import org.junit.Assert;
import org.json.JSONObject;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class TraceTest {
    
    public TraceTest() {
    }

    @Test
    public void testAddBoolean() throws Exception{
        JSONObject o = Trace.addString(new JSONObject(), "true");
        Assert.assertEquals(1, o.length());
        Assert.assertTrue(o.has("boolean"));
        Assert.assertTrue(o.getBoolean("boolean"));
    }
    @Test
    public void testAddJSONObject() throws Exception {
        JSONObject o = Trace.addString(new JSONObject(), "{'toto' : 1, 'txt' : 'some'}");
        Assert.assertEquals(2, o.length());
        Assert.assertEquals(1, o.getInt("toto"));
        Assert.assertEquals("some", o.getString("txt"));
    }
    @Test
    public void testAddString() throws Exception {
        JSONObject o = Trace.addString(new JSONObject(), "coucou");
        Assert.assertEquals(1, o.length());
        Assert.assertTrue(o.has("text"));
        Assert.assertEquals("coucou", o.getString("text"));
    }
    
}
