package appsgate.lig.eude.interpreter.langage.nodes;

import org.json.JSONException;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeReturnTest extends NodeTest {

    public NodeReturnTest() throws JSONException {
        this.ruleJSON.put("type", "return");
    }

    @Before
    public void setUp() throws Exception {
        // A NodeReturn must have a function as a parent
        
        this.instance = new NodeReturn(new NodeFunction("test", null, null), "test");
    }

}
