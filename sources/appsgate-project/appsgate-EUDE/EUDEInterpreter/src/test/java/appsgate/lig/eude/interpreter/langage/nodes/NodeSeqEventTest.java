/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeSeqEventTest extends NodeTest {

    public NodeSeqEventTest() {
    }

    @Before
    @Override
    public void setUp() {
        try {
            this.instance = new NodeSeqEvent(null, new JSONArray());
        } catch (JSONException ex) {
            Logger.getLogger(NodeSeqEventTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
