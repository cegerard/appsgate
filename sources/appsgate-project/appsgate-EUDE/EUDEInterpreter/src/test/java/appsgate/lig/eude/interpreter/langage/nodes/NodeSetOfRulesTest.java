/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeSetOfRulesTest extends NodeTest {

    public NodeSetOfRulesTest() throws JSONException {
        ruleJSON.put("type", "setOfRules");
        ruleJSON.put("rules", new JSONArray());

    }

    @Before
    public void setUp() throws Exception {
        this.instance = new NodeSetOfRules(ruleJSON, null);
    }

}
