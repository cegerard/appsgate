/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeEventsSequenceTest extends NodeTest {

    public NodeEventsSequenceTest() throws Exception {
        super();
        JSONObject desc = new JSONObject();
        desc.put("id", "12");
        ruleJSON.put("type", "eventsSequence");
        ruleJSON.put("events", new JSONArray());
        ruleJSON.put("duration", 0);

    }

    @Before
    public void setUp() throws Exception {
        this.instance = new NodeEventsSequence(this.ruleJSON, null);
    }
}
