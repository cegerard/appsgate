/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import org.json.JSONArray;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeExpBoolTest extends NodeTest {

    private NodeExpBool expBoolTest;

    public NodeExpBoolTest() {
    }

    @Before
    @Override
    public void setUp() {
        JSONArray array = new JSONArray();
        try {
            this.expBoolTest = new NodeExpBool(null, array, null);
            this.instance = this.expBoolTest;
        } catch (NodeException ex) {
            System.out.println(ex.getMessage());
        }

    }

}
