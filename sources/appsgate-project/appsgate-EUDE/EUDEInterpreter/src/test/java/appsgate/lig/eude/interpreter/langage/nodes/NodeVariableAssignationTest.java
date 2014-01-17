package appsgate.lig.eude.interpreter.langage.nodes;

import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeVariableAssignationTest extends NodeTest {

    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {

            this.instance = new NodeVariableAssignation(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
