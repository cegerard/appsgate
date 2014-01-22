package appsgate.lig.eude.interpreter.langage.nodes;

import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeReturnTest extends NodeTest {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // A NodeReturn must have a function as a parent
        this.instance = new NodeReturn(new NodeFunction("test", null, null));
    }

}
