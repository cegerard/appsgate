/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
import junit.framework.Assert;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeSeqAndBoolTest extends NodeTest {

    private NodeSeqAndBool seqTest;

    public NodeSeqAndBoolTest() {
    }

    @Before
    @Override
    public void setUp() {
        try {
            this.seqTest = new NodeSeqAndBool(new JSONArray(), null);
            this.instance = this.seqTest;
        } catch (NodeException ex) {
            System.out.println(ex.getMessage());
        }

    }

    /**
     * Test of getResult method, of class NodeSeqAndBool.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetResult() throws Exception {
        System.out.println("getResult");
        try {
            Boolean result = this.seqTest.getResult();
            Assert.fail("An exception is supposed to have been raised, instead a result has been returned: " + result);
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

}
