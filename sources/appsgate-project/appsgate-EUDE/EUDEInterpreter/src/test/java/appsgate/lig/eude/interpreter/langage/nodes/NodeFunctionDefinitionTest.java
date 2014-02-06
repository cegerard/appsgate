/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.TestUtilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jr
 */
public class NodeFunctionDefinitionTest extends NodeTest {

    private NodeFunctionDefinition node;

    public NodeFunctionDefinitionTest() throws JSONException {
        super();
        ruleJSON.put("type", "functionDefinition");
        ruleJSON.put("id", "test");
        ruleJSON.put("seqRules", emptySeqRules);
        ruleJSON.put("seqDefinitions", new JSONArray());

    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.node = new NodeFunctionDefinition(ruleJSON, null);
        this.instance = this.node;

    }

    @Override
    public void testGetSymbolTable() {
        assertNotNull(this.node.getSymbolTable());
    }

    @Test
    public void testBuildNodeFromJson() throws Exception {
        NodeFunctionDefinition defNode = new NodeFunctionDefinition(TestUtilities.loadFileJSON("src/test/resources/testFunction.json"), null);
        assertNotNull(defNode);
        System.out.println(defNode.getExpertProgramScript());
        Node code = defNode.getCode(instance);
        Assert.assertNotNull(code);
    }

    @Test
    public void testGetCode() throws Exception {
        assertNotNull(this.node.getCode(instance));
    }

}
