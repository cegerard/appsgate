/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEMediator;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import java.util.Iterator;
import junit.framework.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONArray;

/**
 *
 * @author jr
 */
public abstract class NodeTest {

    protected Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    protected Node instance;
    protected JSONObject ruleJSON;

    protected EUDEMediator mediator;
    protected NodeProgram programNode;

    protected JSONObject emptySeqRules;

    /**
     * Constructor
     */
    public NodeTest() {
        this.ruleJSON = new JSONObject();

        this.mediator = context.mock(EUDEMediator.class);
        programNode = new NodeProgram(mediator);
        this.emptySeqRules = new JSONObject();
        try {
            emptySeqRules.put("type", "instructions");
            emptySeqRules.put("rules", new JSONArray());
        } catch (JSONException jSONException) {
        }
    }

    @Before
    public void setUp() throws Exception {
        this.instance = null;

    }

    /**
     * Getter
     *
     * @return the ruleJSON
     */
    public JSONObject getRuleJSON() {
        return ruleJSON;
    }

    /**
     * Test of stop method
     *
     * @throws Exception
     */
    @Test
    public void testStop() throws Exception {
        printTestName("stop");
        this.instance.stop();
    }

    /**
     * Test of call method.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCall() throws Exception {
        printTestName("call");
        JSONObject expResult = null;
        JSONObject result = this.instance.call();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSymbolTable method, of class Node.
     */
    @Test
    public void testGetSymbolTable() {
        printTestName("getSymbolTable");
        SymbolTable expResult = null;
        SymbolTable result = this.instance.getSymbolTable();
        assertEquals(expResult, result);
    }


    @Test
    public void testGetResult() throws Exception {
        printTestName("GetResult");
        assertNull("result should be null in the default case", this.instance.getResult());
    }

    @Test
    public void testGetJSONDescription() throws JSONException {
        printTestName("GetJSONDescription");
        try {
            JSONObject jsonDescription = this.instance.getJSONDescription();
            Assert.assertNotNull(jsonDescription.getString("type"));
            Assert.assertTrue("Two Json Object should be equals", compareTo(this.ruleJSON, jsonDescription));
        } catch (UnsupportedOperationException e) {
            fail("It is supposed to be supported now");
        }
    }

    @Test
    public void testCopy() throws JSONException {
        printTestName("Copy");

        Node copy = this.instance.copy(null);
        assertNotNull(copy);
        Assert.assertTrue("Two copies should have same json description", compareTo(this.instance.getJSONDescription(), copy.getJSONDescription()));
    }

    @Test
    public void testIsATypeInJsonDesc() {
        try {
            Assert.assertFalse(this.ruleJSON.getString("type").isEmpty());
        } catch (JSONException ex) {
            Assert.fail("The type is not defined");
        }
    }

    /**
     * Method that compare two JSON Objects and tell where they differ
     *
     * @param orig
     * @param copy
     * @return
     * @throws JSONException
     */
    private boolean compareTo(JSONObject orig, JSONObject copy) throws JSONException {
        Boolean ret = true;
        Iterator k = orig.keys();
        while (k.hasNext()) {
            String key = (String) k.next();
            if (!copy.has(key)) {
                ret = false;
                System.out.println(key + " is in original, but not in copy");
            } else {
                if (orig.get(key).getClass() == JSONObject.class && copy.get(key).getClass() == JSONObject.class) {
                    return compareTo((JSONObject) orig.get(key), (JSONObject) copy.get(key));
                }
                if (!orig.get(key).toString().equals(copy.get(key).toString())) {
                    ret = false;
                    System.out.println("For key (" + key + "), contents is not equal:\n"
                            + orig.get(key).getClass().getSimpleName() + ":" + orig.get(key).toString() + "\n"
                            + copy.get(key).getClass().getSimpleName() + ":" + copy.getString(key).toString());
                }
            }
        }
        k = copy.keys();
        while (k.hasNext()) {
            String key = (String) k.next();
            if (!orig.has(key)) {
                ret = false;
                System.out.println(key + " is in copy, but not in original");
            } else {
                if (orig.get(key).getClass() == JSONObject.class && copy.get(key).getClass() == JSONObject.class) {
                    return compareTo((JSONObject) orig.get(key), (JSONObject) copy.get(key));
                }
                if (!orig.get(key).toString().equals(copy.get(key).toString())) {
                    ret = false;
                    System.out.println("For key (" + key + "), contents is not equal:\n"
                            + orig.get(key).getClass().getSimpleName() + ":" + orig.get(key).toString() + "\n"
                            + copy.get(key).getClass().getSimpleName() + ":" + copy.getString(key).toString());
                }
            }
        }

        return ret;
    }

    protected void printTestName(String testName) {
        System.out.println(testName + " - " + this.getClass().getSimpleName());
    }

}
