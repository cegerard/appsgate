package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import java.util.Collection;
import org.json.JSONException;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeActionTest extends NodeTest {

    private NodeAction actionTest;

    public NodeActionTest() {
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {
            ruleJSON.put("targetType", "test");
            ruleJSON.put("targetId", "test");
            ruleJSON.put("methodName", "test");
            ruleJSON.put("args", (Collection) null);
        } catch (JSONException ex) {
            System.out.println("JsonEx");
        }

        try {

            this.actionTest = new NodeAction(ruleJSON, null);
            this.instance = this.actionTest;
        } catch (SpokNodeException ex) {
            System.out.println("JSon Exception: " + ex);
        }
    }

    /**
     * Test of getResult method, of class NodeAction.
     */
    @Test
    @Override
    public void testGetResult() {
        System.out.println("getResult");
        Object expResult = null;
        Object result = this.actionTest.getResult();
        assertEquals(expResult, result);
    }

}
