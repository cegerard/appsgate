package appsgate.lig.eude.interpreter.langage.nodes;

import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeVariableAssignationTest extends NodeTest {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.instance = new NodeVariableAssignation(null);
    }

}
