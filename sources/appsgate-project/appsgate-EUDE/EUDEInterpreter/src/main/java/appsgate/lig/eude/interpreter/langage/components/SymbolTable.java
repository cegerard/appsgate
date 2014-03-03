package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.langage.nodes.NodeVariableDefinition;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokSymbolTableException;
import appsgate.lig.eude.interpreter.langage.nodes.Node;
import appsgate.lig.eude.interpreter.langage.nodes.NodeFunctionDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 * Class to store the variable
 *
 * @author jr
 */
public final class SymbolTable {

    // Logger
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SymbolTable.class);

    private final HashMap<String, NodeVariableDefinition> variables;
    private final HashMap<String, NodeFunctionDefinition> functions;
    private final List<String> varNames;
    private final Node parent;

    /**
     * Constructor
     *
     * @param p
     */
    public SymbolTable(Node p) {
        variables = new HashMap<String, NodeVariableDefinition>();
        functions = new HashMap<String, NodeFunctionDefinition>();
        varNames = new ArrayList<String>();
        parent = p;
    }

    /**
     * Constructor
     *
     * @param jsonArray
     * @param parent
     * @throws SpokException
     */
    public SymbolTable(JSONArray jsonArray, Node parent) throws SpokException {
        this(parent);
        this.buildFromJson(jsonArray);
    }

    /**
     *
     * @param jsonArray
     * @throws SpokException
     */
    public void buildFromJson(JSONArray jsonArray) throws SpokException {
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject vJson;
                try {
                    vJson = jsonArray.getJSONObject(i);
                    String varName = vJson.optString("id");
                    if (varName != null) {
                        if (variables.get(varName) != null) {
                            throw new SpokSymbolTableException("The variable name has already been used in the same scope: " + varName, null);
                        }
                        addVariable(varName, vJson.getJSONObject("value"));
                    } else {
                        String functName = vJson.optString("func_name");
                        if (functName != null) {
                            if (functions.get(functName) != null) {
                                throw new SpokSymbolTableException("The func_name has already been used in the same scope: " + functName, null);
                            }
                            addFunction(functName, new NodeFunctionDefinition(vJson, null));
                        }
                    }
                } catch (JSONException ex) {
                    LOGGER.error("Reading a Json array and not finding item {}", i);
                }
            }
        }
    }

    /**
     *
     * @param varName
     * @param value
     * @return the new variable created
     */
    public NodeVariableDefinition addVariable(String varName, JSONObject value) {
        NodeVariableDefinition var;
        try {
            if (value == null) {
                var = new NodeVariableDefinition(varName, null, this.parent);
            } else {
                var = new NodeVariableDefinition(varName, value, this.parent);
            }
        } catch (SpokException ex) {
            LOGGER.error("Unable to create a variable of name {} with value {}", varName, value);
            LOGGER.debug(ex.getMessage());
            return null;
        }
        variables.put(varName, var);
        varNames.add(varName);
        return var;

    }

    /**
     *
     * @param l
     * @return
     */
    public String getVariableKey(NodeVariableDefinition l) {
        for (String k : variables.keySet()) {
            if (variables.get(k).equals(l)) {
                return k;
            }
        }
        return null;
    }

    /**
     *
     * @param key
     * @return
     */
    public NodeVariableDefinition getVariableByKey(String key) {
        return (NodeVariableDefinition) variables.get(key);
    }

    /**
     *
     * @param key
     * @return
     */
    public NodeFunctionDefinition getFunctionByKey(String key) {
        return functions.get(key);
    }

    /**
     * Method to add a function in the Symbol Table
     *
     * @param functionName the name of the function
     * @param f the function
     * @return the function added in the Symbol table
     * @throws SpokSymbolTableException if function name is already in use
     */
    public NodeFunctionDefinition addFunction(String functionName, NodeFunctionDefinition f)
            throws SpokSymbolTableException {
        if (functions.get(functionName) != null) {
            throw new SpokSymbolTableException("Function name already exists for this context", null);
        }
        functions.put(functionName, f);
        return f;
    }

    /**
     *
     * @return
     */
    public String getExpertProgramDecl() {
        String ret = "";
        for (String k : variables.keySet()) {
            ret += k + " = " + variables.get(k).getExpertProgramDecl() + "\n";
        }

        for (String k : functions.keySet()) {
            ret += functions.get(k).getExpertProgramScript() + "\n";
        }

        return ret;
    }

    /**
     *
     * @return the list of variables
     */
    public List<String> getVarList() {
        return varNames;
    }

    /**
     *
     * @return the JSON description of the symbol table
     */
    public JSONArray getJSONDescription() {
        JSONArray a = new JSONArray();
        int i = 0;
        try {
            for (String name : varNames) {
                JSONObject v = variables.get(name).getJSONDescription();
                a.put(i++, v);
            }
            for (NodeFunctionDefinition f : functions.values()) {
                JSONObject json = f.getJSONDescription();
                json.put("func_name", f.getName());
                a.put(i++, json);
            }
        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return a;
    }

}
