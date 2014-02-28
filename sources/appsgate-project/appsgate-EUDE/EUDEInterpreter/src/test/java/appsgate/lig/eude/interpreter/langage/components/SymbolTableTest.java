/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.langage.nodes.NodeVariableDefinition;
import appsgate.lig.eude.interpreter.langage.nodes.NodeFunctionDefinition;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jr
 */
public class SymbolTableTest {

    public SymbolTableTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of addVariable method, of class SymbolTable.
     */
    @Test
    public void testAddVariable() {
        System.out.println("addVariable");
        String varName = "v1";
        SymbolTable instance = new SymbolTable(null);
        NodeVariableDefinition expResult = new NodeVariableDefinition(varName, null);
        Assert.assertNotNull(expResult);
        NodeVariableDefinition result = instance.addVariable(varName, null);
                Assert.assertNotNull(result);

        assertTrue(expResult.equals(result));
    }

    /**
     * Test of getVariableKey method, of class SymbolTable.
     */
    @Test
    public void testGetVariableKey() {
        System.out.println("getVariableKey");
        NodeVariableDefinition l = null;
        SymbolTable instance = new SymbolTable(null);
        String result = instance.getVariableKey(l);
        assertNull(result);
    }

    /**
     * Test of getVariableByKey method, of class SymbolTable.
     */
    @Test
    public void testGetVariableByKey() {
        System.out.println("getVariableByKey");
        String key = "";
        SymbolTable instance = new SymbolTable(null);
        NodeVariableDefinition expResult = null;
        NodeVariableDefinition result = instance.getVariableByKey(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of addFunction method, of class SymbolTable.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testAddFunction() throws Exception {
        System.out.println("addFunction");
        String functionName = "";
        NodeFunctionDefinition f = null;
        SymbolTable instance = new SymbolTable(null);
        NodeFunctionDefinition expResult = null;
        NodeFunctionDefinition result = instance.addFunction(functionName, f);
        assertEquals(expResult, result);
    }

    /**
     * Test of getExpertProgramDecl method, of class SymbolTable.
     */
    @Test
    public void testGetExpertProgramDecl() {
        System.out.println("getExpertProgramDecl");
        SymbolTable instance = new SymbolTable(null);
        String expResult = "";
        String result = instance.getExpertProgramDecl();
        assertEquals(expResult, result);
    }

}
