/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.Collection;
import org.json.JSONException;
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
    public void setUp() {
        super.setUp();
        try {
            ruleJSON.put("events", (Collection) null);
            ruleJSON.put("seqRulesThen", (Collection) null);
            this.instance = new NodeWhen(null, this.ruleJSON);
        } catch (JSONException ex) {
            System.out.println("JSON Ex : " + ex.getMessage());
        } catch (NodeException ex) {
            System.out.println(ex.getMessage());
        }

    }

}
