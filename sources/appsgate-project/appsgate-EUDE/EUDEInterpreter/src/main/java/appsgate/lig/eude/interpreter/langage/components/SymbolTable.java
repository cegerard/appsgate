package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokSymbolTableException;
import appsgate.lig.eude.interpreter.langage.nodes.NodeFunctionDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

public final class SymbolTable {

    // Logger
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SymbolTable.class.getName());

    private final HashMap<String, SpokVariable> variables;
    private final HashMap<String, NodeFunctionDefinition> functions;
    private final List<String> varNames;

    /**
     * Constructor
     */
    public SymbolTable() {
        variables = new HashMap<String, SpokVariable>();
        functions = new HashMap<String, NodeFunctionDefinition>();
        varNames = new ArrayList<String>();
    }

    /**
     * Constructor
     *
     * @param jsonArray
     * @throws SpokException
     */
    public SymbolTable(JSONArray jsonArray) throws SpokException {
        this();
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
                    String varName = vJson.optString("var_name");
                    if (varName != null) {
                        if (variables.get(varName) != null) {
                            throw new SpokSymbolTableException("The var_name has already been used in the same scope: " + varName, null);
                        }
                        addVariable(varName, new SpokVariable(vJson));
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
            addVariable(keyVal, e);
        }
        return e;
    }

    /**
     *
     * @param varName
     * @param var
     * @return the new variable created
     */
    public SpokVariable addVariable(String varName, SpokVariable var) {
        variables.put(varName, var);
        varNames.add(varName);
        return var;

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

    public List<String> getVarList() {
        return varNames;
    }

}
