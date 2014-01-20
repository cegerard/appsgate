/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeSeqRulesTest extends NodeTest {

    public NodeSeqRulesTest() {
    }

    @Before
    @Override
    public void setUp() {
        try {
            this.instance = new NodeSeqRules(new JSONArray(), null);
        } catch (SpokNodeException ex) {
            System.out.println("JSON ex: " + ex.getMessage());
        }

    }

    @Test
    @Override
    public void testGetJSONDescription() throws JSONException {
    }

}
