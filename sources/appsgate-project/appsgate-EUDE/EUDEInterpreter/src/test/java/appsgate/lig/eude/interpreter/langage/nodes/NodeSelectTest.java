package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeSelectTest extends NodeTest {

    public NodeSelectTest() {
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {
            ruleJSON.put("name", "test");
            ruleJSON.put("seqRules", new JSONArray());
            ruleJSON.put("seqDefinitions", new JSONArray());
            this.instance = new NodeSelect(null);
        } catch (JSONException ex) {
            Logger.getLogger(NodeSelectTest.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

}
