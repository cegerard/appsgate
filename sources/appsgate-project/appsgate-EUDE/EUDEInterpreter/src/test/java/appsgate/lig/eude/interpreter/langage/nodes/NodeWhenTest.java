/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.Collection;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeWhenTest extends NodeTest {

    public NodeWhenTest() {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ruleJSON.put("events", (Collection) null);
        ruleJSON.put("seqRulesThen", emptySeqRules);
        this.instance = new NodeWhen(this.ruleJSON, null);

    }

}
