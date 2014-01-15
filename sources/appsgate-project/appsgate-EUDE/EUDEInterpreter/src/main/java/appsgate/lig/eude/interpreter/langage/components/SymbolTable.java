package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
import appsgate.lig.eude.interpreter.langage.nodes.NodeFunctionDefinition;
import java.util.HashMap;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

public final class SymbolTable {

    // Logger
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SymbolTable.class.getName());

    private final HashMap<String, SpokVariable> variables;
    private final HashMap<String, NodeFunctionDefinition> functions;

    /**
     * Constructor
     */
    public SymbolTable() {
        variables = new HashMap<String, SpokVariable>();
        functions = new HashMap<String, NodeFunctionDefinition>();
    }

    /**
     * Constructor
     *
     * @param jsonArray
     * @throws NodeException
     */
    public SymbolTable(JSONArray jsonArray) throws NodeException {
        this();
        this.buildFromJson(jsonArray);
    }

    /**
     *
     * @param jsonArray
     * @throws NodeException
     */
    public void buildFromJson(JSONArray jsonArray) throws NodeException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject vJson;
            try {
                vJson = jsonArray.getJSONObject(i);
                String varName = vJson.optString("var_name");
                if (varName != null) {
                    if (variables.get(varName) != null) {
                        throw new NodeException("Symbol Table", "The var_name has already been used in the same scope: " + varName, null);
                    }
                    variables.put(varName, new SpokVariable(vJson));
                } else {
                    String functName = vJson.optString("func_name");
                    if (functName != null) {
                        if (functions.get(functName) != null) {
                            throw new NodeException("Symbol Table", "The func_name has already been used in the same scope: " + functName, null);
                        }
                        functions.put(functName, new NodeFunctionDefinition(vJson, null));
                    }
                }
            } catch (JSONException ex) {
                LOGGER.error("Reading a Json array and not finding item {}", i);
            }
        }

    }

    /**
     * Method to add a Variable in the SymbolTable
     *
     * @param id the id of the type referenced in the variable
     * @param type the type referenced by the variable
     * @return the SpokVariable created
     */
    public SpokVariable addAnonymousVariable(String id, String type) {
        SpokVariable e = new SpokVariable(id, type);
        if (this.getVariableKey(e) == null) {
            String keyVal = type.substring(0, 3) + "_" + variables.size();
            variables.put(keyVal, e);
        }
        return e;
    }

    /**
     *
     * @param varName
     * @param id
     * @param type
     * @return the new variable created
     */
    public SpokVariable addVariable(String varName, String id, String type) {
        SpokVariable v = new SpokVariable(id, type);
        variables.put(varName, v);
        return v;
    }

    /**
     *
     * @param l
     * @return
     */
    public String getVariableKey(SpokVariable l) {
        for (String k : variables.keySet()) {
            if (variables.get(k).equals(l)) {
                return k;
            }
        }
        return null;
    }

    /**
     * Method that find the key of a variable given his type and id
     *
     * @param id
     * @param type
     * @return
     */
    public String getAnonymousVariableKey(String id, String type) {
        return getVariableKey(new SpokVariable(id, type));
    }

    /**
     *
     * @param key
     * @return
     */
    public SpokVariable getVariableByKey(String key) {
        return variables.get(key);
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
     * @throws NodeException if function name is already in use
     */
    public NodeFunctionDefinition addFunction(String functionName, NodeFunctionDefinition f)
            throws NodeException {
        if (functions.get(functionName) != null) {
            throw new NodeException("Symbol Table", "Function name already exists for this context", null);
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

    public Set<String> getVarList() {
        return variables.keySet();
    }

}
