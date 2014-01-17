package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.impl.TestUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
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
            
            this.instance = new NodeFunction(ruleJSON, null);
        } catch (SpokNodeException ex) {
            Logger.getLogger(NodeFunctionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(NodeFunctionTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void testBuildNodeFromJson() throws Exception {
        NodeFunctionDefinition defNode = new NodeFunctionDefinition(TestUtilities.loadFileJSON("src/test/resources/testFunction.json"), null);
        assertNotNull(defNode);
        JSONArray p = new JSONArray("[{'type':'variable', 'value':'12'}, {'type':'boolean', 'value':'true'}, {'type':'string', 'value':'c'}]");
        NodeFunction func = new NodeFunction("test", defNode, p);
        assertNotNull(func);
        System.out.println(func.getExpertProgramScript());
        Assert.assertEquals(null, func.call());
    }

    @Test
    @Override
    public void testCall() throws Exception {
        System.out.println("call");
        JSONObject result = this.instance.call();
        // No function definition exists for "test" function
        assertNotNull(result);
        assertEquals("SpokExecutionException", result.getString("exceptionType"));
    }
    
    @Test
    @Override
    public void testGetSymbolTable() {
        assertNotNull(this.instance.getSymbolTable());
    }
}
