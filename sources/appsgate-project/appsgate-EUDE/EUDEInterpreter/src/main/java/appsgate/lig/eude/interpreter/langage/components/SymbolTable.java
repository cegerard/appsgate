package appsgate.lig.eude.interpreter.langage.components;

import appsgate.lig.eude.interpreter.langage.nodes.NodeException;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SymbolTable {

    private final HashMap<String, Variable> symbols;

    /**
     * Constructor
     */
    public SymbolTable() {
        symbols = new HashMap<String, Variable>();
    }

    /**
     *
     * @param jsonArray
     * @throws NodeException
     */
    public SymbolTable(JSONArray jsonArray) throws NodeException {
        this();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject vJson;
            try {
                vJson = jsonArray.getJSONObject(i);
                symbols.put(vJson.getString("var_name"), new Variable(vJson.getString("id"), vJson.getString("type")));
            } catch (JSONException ex) {
                throw new NodeException("Symbol Table", "item " + i, ex);
            }
        }
    }

    /**
     *
     * @param id
     * @param type
     */
    public void add(String id, String type) {
        Variable e = new Variable(id, type);
        if (this.getVariableKey(e) == null) {
            String keyVal = type.substring(0, 3) + "_" + symbols.size();
            symbols.put(keyVal, e);
        }
    }

    public void addElement(String varName, String id, String type) {
        symbols.put(varName, new Variable(id, type));
    }

    public String getVariableKey(Variable l) {
        for (String k : symbols.keySet()) {
            if (symbols.get(k).equals(l)) {
                return k;
            }
        }
        return null;
    }

    public String getVariableKey(String id, String type) {
        return getVariableKey(new Variable(id, type));
    }

    public Variable getVariableByKey(String key) {
        return symbols.get(key);
    }

    public String getExpertProgramDecl() {
        String ret = "";
        for (String k : symbols.keySet()) {
            ret += k + " = " + symbols.get(k).getExpertProgramDecl() + "\n";
        }
        return ret;
    }

}
