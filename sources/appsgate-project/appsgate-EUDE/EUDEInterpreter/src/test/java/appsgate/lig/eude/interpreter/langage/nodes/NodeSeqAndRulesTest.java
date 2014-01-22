/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeSeqAndRulesTest extends NodeTest {

    public NodeSeqAndRulesTest() {
    }

    @Before
    @Override
    public void setUp() {
        try {
            this.instance = new NodeSeqAndRules(new JSONArray(), null);
        } catch (SpokNodeException ex) {
            Logger.getLogger(NodeSeqAndRulesTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SpokException ex) {
            Logger.getLogger(NodeSeqAndRulesTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    @Override
    public void testGetJSONDescription() throws JSONException {
    }

}
