/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
import appsgate.lig.eude.interpreter.impl.TestUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author jr
 */
public class NodeFunctionTest extends NodeTest {


    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {
            ruleJSON.put("id", "test");
            //ruleJSON.put("params", new JSONArray());
            
            this.instance = new NodeFunction(interpreter, ruleJSON, null);
        } catch (NodeException ex) {
            Logger.getLogger(NodeFunctionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(NodeFunctionTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void testBuildNodeFromJson() throws Exception {
        NodeFunctionDefinition defNode = new NodeFunctionDefinition(TestUtilities.loadFileJSON("src/test/resources/testFunction.json"), null);
        assertNotNull(defNode);
        System.out.println(defNode.getExpertProgramScript());
        JSONArray p = new JSONArray("[{'type':'variable', 'value':'12'}, {'type':'boolean', 'value':'true'}, {'type':'string', 'value':'c'}]");
        NodeFunction func = new NodeFunction("test", defNode, p);
        assertNotNull(func);
        System.out.println(func.getExpertProgramScript());
    }

    @Test
    public void test() throws Exception {

    }
}
