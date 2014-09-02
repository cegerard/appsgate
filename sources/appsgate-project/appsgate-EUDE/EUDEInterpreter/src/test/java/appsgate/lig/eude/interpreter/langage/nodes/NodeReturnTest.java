package appsgate.lig.eude.interpreter.langage.nodes;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeReturnTest extends NodeTest {

    public NodeReturnTest() throws JSONException {

        this.ruleJSON.put("id", "test");
        this.ruleJSON.put("type", "return");
    }

    @Before
    public void setUp() throws Exception {
        // A NodeReturn must have a function as a parent
        
        this.instance = new NodeReturn(new NodeFunction(ruleJSON,programNode));
    }

    @Test
    @Override
    public void testGetJSONDescription() {
        // do nothing
    }
}
