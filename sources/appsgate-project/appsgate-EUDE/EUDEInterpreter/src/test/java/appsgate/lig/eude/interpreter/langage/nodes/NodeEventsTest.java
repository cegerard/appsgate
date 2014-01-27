/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import org.json.JSONArray;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeEventsTest extends NodeTest {

    public NodeEventsTest() {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ruleJSON.put("type", "events");
        ruleJSON.put("events", new JSONArray());
        this.instance = new NodeEvents(this.ruleJSON, null);

    }

    @Test
    public void testSomeMethod() {
    }

}
