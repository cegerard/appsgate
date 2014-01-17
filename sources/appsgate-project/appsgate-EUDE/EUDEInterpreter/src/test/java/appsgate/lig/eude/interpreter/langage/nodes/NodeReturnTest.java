/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeReturnTest extends NodeTest {

    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {
            // A NodeReturn must have a function as a parent
            this.instance = new NodeReturn(new NodeFunction("test", null, null));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
