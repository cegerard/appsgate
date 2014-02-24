package appsgate.components.grammar.test;

import appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec;
import appsgate.lig.co2.sensor.spec.CoreCO2SensorSpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class GrammarTest {

    @Test
    public void testColorLightSpecGrammar() throws Exception {
        testStream(CoreColorLightSpec.class);
    }

    @Test
    public void testCO2SpecGrammar() throws Exception {
        testStream(CoreCO2SensorSpec.class);
    }

    /**
     * @param c 
     * @throws Exception 
     */
    private void testStream(Class c) throws Exception {
        String className = c.getSimpleName();
        System.out.println("==== Testing grammar for " + className + " =====");
        InputStream stream = c.getResourceAsStream("grammar.json");
        Assert.assertNotNull("Unable to read the file",stream);
        JSONObject o = loadJSONStream(stream);
        Assert.assertNotNull(o);
        Assert.assertNotNull(className + " should have state", o.optJSONArray("states"));
        Assert.assertNotNull(className + " should have properties", o.optJSONArray("properties"));
        Assert.assertFalse(className + " should have typename", o.optString("typename").isEmpty());
        Assert.assertFalse(className + " should have friendly name", o.optString("friendlyName").isEmpty());
    }

    /**
     *
     * @param in the stream
     * @return a json object
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject loadJSONStream(InputStream in) throws IOException, JSONException {
        InputStreamReader is = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(is);
        String read = br.readLine();

        while (read != null) {
            //System.out.println(read);
            sb.append(read);
            read = br.readLine();

        }

        return new JSONObject(sb.toString());

    }
}
