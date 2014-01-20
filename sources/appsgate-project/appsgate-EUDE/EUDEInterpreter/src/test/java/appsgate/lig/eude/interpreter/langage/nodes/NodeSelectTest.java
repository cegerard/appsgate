package appsgate.lig.eude.interpreter.langage.nodes;

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
        this.instance = new NodeSelect(null);
    }

}
