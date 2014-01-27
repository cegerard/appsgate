package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.Collection;
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
    public void setUp() throws Exception {
        super.setUp();
        ruleJSON.put("type", "action");
        ruleJSON.put("targetType", "test");
        ruleJSON.put("targetId", "test");
        ruleJSON.put("methodName", "test");
        ruleJSON.put("args", (Collection) null);
        ruleJSON.put("returnType", "");

        this.actionTest = new NodeAction(ruleJSON, null);
        this.instance = this.actionTest;
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
