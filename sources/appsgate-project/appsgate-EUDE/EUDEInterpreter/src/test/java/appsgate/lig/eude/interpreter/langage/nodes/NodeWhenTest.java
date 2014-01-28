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
public class NodeWhenTest extends NodeTest {
    
    public NodeWhenTest() {
    }
    
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        JSONObject events = new JSONObject();
        events.put("type", "events");
        events.put("events", new JSONArray());
        events.put("duration", 0);
        events.put("nbEventToOccur", 0);
        ruleJSON.put("events", events);
        ruleJSON.put("seqRulesThen", emptySeqRules);
        this.instance = new NodeWhen(this.ruleJSON, null);
        
    }
    
}
