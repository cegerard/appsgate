package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.junit.Before;
import static org.junit.Assert.*;

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
            this.actionTest = new NodeAction(ruleJSON, null);
            this.instance = this.actionTest;
        } catch (SpokNodeException ex) {
            System.out.println("JSon Exception: " + ex);
        }
    }

    /**
     * Test of getResult method, of class NodeAction.
     */
    @org.junit.Test
    public void testGetResult() {
        System.out.println("getResult");
        Object expResult = null;
        Object result = this.actionTest.getResult();
        assertEquals(expResult, result);
    }

}
