package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.components.SpokVariable;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node Function that contains the definition of a function
 *
 * @author JR Courtois
 */
public class NodeFunction extends Node {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFunction.class);

    /**
     * The name of the function
     */
    private final String name;

    /**
     * The Definition of the function
     */
    private final NodeFunctionDefinition functionDef;

    /**
     * The node sequence to execute
     */
    private NodeSeqRules rules = null;

    /**
     * The params passed to the function
     */
    private final JSONArray params;

    /**
     * The result associated with the function
     */
    private SpokObject result = null;

    /**
     * Constructor
     *
     * @param parent the
     * @param n
     * @param def
     * @param par
     */
    private NodeFunction(Node parent, String n, NodeFunctionDefinition def, JSONArray par) {
        super(parent);
        name = n;
        functionDef = def;
        params = par;
        setSymbolTable(new SymbolTable());

    }

    /**
     * Initialize the program from a JSON object
     *
     * @param programJSON Abstract tree of the program in JSON
     * @param parent the node to which it is attached
     * @throws SpokNodeException
     */
    public NodeFunction(JSONObject programJSON, Node parent)
            throws SpokNodeException {
        super(parent);
        name = getJSONString(programJSON, "id");
        functionDef = this.getFunctionByName(name);
        params = programJSON.optJSONArray("params");
        setSymbolTable(new SymbolTable());

    }

    /**
     * Constructor to make some tests
     *
     * @param n
     * @param def
     * @param p
     */
    public NodeFunction(String n, NodeFunctionDefinition def, JSONArray p) {
        this(null, n, def, p);
    }

    /**
     * Launch the interpretation of the rules
     *
     * @return integer
     */
    @Override
    public JSONObject call() {
        if (functionDef != null) {
            LOGGER.debug("The function {} has been called.", this);
            try {
                rules = functionDef.getCode(this);
                initSymbolTable();
                rules.call();
            } catch (SpokException ex) {
                LOGGER.error("Unable to copy this function: {}", this);
                return ex.getJSONDescription();
            }
        } else {
            LOGGER.error("Unable to find the definition of this function: {}", this);
            SpokExecutionException ex = new SpokExecutionException("Unable to find the definition of this function");
            return ex.getJSONDescription();
        }
        return null;
    }

    @Override
    public void stop() throws SpokException {
        LOGGER.info("The function {} has been stoped.", this);
        if (rules != null) {
            rules.stop();
        }
    }

    @Override
    protected void specificStop() {
    }

    @Override
    public void startEventFired(StartEvent e) {
        LOGGER.debug("The start event ({}) has been catched by {}", e.getSource(), this);
    }

    @Override
    public void endEventFired(EndEvent e) {
        Node source = (Node) e.getSource();
        LOGGER.debug("The end event ({}) has been catched by {}", source, this);
        try {
            this.result = source.getResult();
        } catch (SpokException ex) {
            this.result = ex;
        }
        fireEndEvent(new EndEvent(this));
    }

    @Override
    public String toString() {
        return "[Node Function : " + name + "]";
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", name);
            o.put("params", params);
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
    }

    /**
     * @return the script of a program, more readable than the json structure
     */
    @Override
    public String getExpertProgramScript() {
        return name + "(" + this.getStringParams() + ")\n";
    }

    /**
     *
     * @return the string representing the params to put in the expert program
     */
    private String getStringParams() {
        if (params == null || params.length() == 0) {
            return "";
        }

        try {
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (; i < params.length() - 1; i++) {
                builder.append(getStringParam(params.getJSONObject(i))).append(",");
            }
            builder.append(getStringParam(params.getJSONObject(i)));

            return builder.toString();
        } catch (JSONException ex) {
            LOGGER.error("Error in parsing function params for function: {}", this.name);
            return "...";
        }

    }

    /**
     *
     * @param o the param to add to the parameters string
     * @return the string corresponding to the type of the param
     */
    private String getStringParam(JSONObject o) {
        String type = o.optString("type");
        if (type.compareToIgnoreCase("int") == 0) {
            return Integer.toString(o.optInt("value"));
        }
        if (type.compareToIgnoreCase("boolean") == 0) {
            if (o.optBoolean("value")) {
                return "true";
            } else {
                return "false";
            }
        }
        if (type.compareToIgnoreCase("string") == 0) {
            return '"' + o.optString("value") + '"';
        }
        if (type.compareToIgnoreCase("variable") == 0) {
            return o.optString("value");
        }
        return type;

    }

    @Override
    public Node copy(Node parent) {
        JSONArray copyParams = null;
        try {
            if (params != null) {
                copyParams = new JSONArray(params.toString());
            }
        } catch (JSONException ex) {
        }
        NodeFunction ret = new NodeFunction(parent, name, functionDef, copyParams);
        return ret;
    }

    /**
     * Method that copy the Symbol table of the function definition to the
     * Symbol Table of this node
     *
     * @throws SpokException
     */
    private void initSymbolTable() throws SpokException {
        SymbolTable symbolTable = functionDef.getSymbolTable();
        List<String> varList = symbolTable.getVarList();
        int i = 0;
        for (String v_name : varList) {
            JSONObject jsonVariable = (JSONObject) params.opt(i);
            SpokVariable v = new SpokVariable(v_name, jsonVariable);
            this.getSymbolTable().addVariable(v_name, v);
        }
    }

    @Override
    public SpokObject getResult() {
        return this.result;
    }
}
