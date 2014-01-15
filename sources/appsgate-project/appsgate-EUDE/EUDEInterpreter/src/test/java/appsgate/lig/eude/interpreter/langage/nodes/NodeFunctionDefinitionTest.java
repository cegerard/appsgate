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
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeFunctionDefinitionTest extends NodeTest {

    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {
            ruleJSON.put("name", "test");
            ruleJSON.put("seqRules", new JSONArray());
            ruleJSON.put("seqDefinitions", new JSONArray());
            this.instance = new NodeFunctionDefinition(interpreter, ruleJSON, null);
        } catch (NodeException ex) {
            Logger.getLogger(NodeFunctionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(NodeFunctionTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void testGetSymbolTable() {
        assertNotNull(this.instance.getSymbolTable());
    }
    
    @Test
    public void testBuildNodeFromJson() throws Exception {
        NodeFunctionDefinition defNode = new NodeFunctionDefinition(null, TestUtilities.loadFileJSON("src/test/resources/testFunction.json"), null);
        assertNotNull(defNode);
        System.out.println(defNode.getExpertProgramScript());
    }

}
