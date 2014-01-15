package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import java.util.logging.Level;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeProgram.class);

    /**
     * Sequence of rules to interpret
     */
    private final String name;

    /**
     * The Definition of the function
     */
    private final NodeFunctionDefinition functionDef;

    private NodeSeqRules rules = null;

    /**
     *
     */
    private final JSONArray params;

    private NodeFunction(Node parent, String n, NodeFunctionDefinition def, JSONArray par) {
        super(parent);
        name = n;
        functionDef = def;
        params = par;

    }

    /**
     * Initialize the program from a JSON object
     *
     * @param interpreter
     * @param programJSON Abstract tree of the program in JSON
     * @param parent
     * @throws NodeException
     */
    public NodeFunction(EUDEInterpreterImpl interpreter, JSONObject programJSON, Node parent)
            throws NodeException {
        super(parent);
        name = getJSONString(programJSON, "id");
        functionDef = this.getFunctionByName(name);
        params = programJSON.optJSONArray("params");
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
    public Integer call() {
        LOGGER.debug("The function {} has been called.", this);
        if (functionDef != null) {
            rules = functionDef.getCode(params, this);
            rules.call();
        }
        return null;
    }

    @Override
    public void stop() {
        LOGGER.info("The function {} has been stoped.", this);
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
        LOGGER.debug("The end event ({}) has been catched by {}", e.getSource(), this);
    }

    @Override
    public String toString() {
        return "[Node Function : " + name + "]";
    }

    /**
     * @return the script of a program, more readable than the json structure
     */
    @Override
    public String getExpertProgramScript() {
        return name + "(" + this.getStringParams() + ")\n";
    }

    @Override
    protected void collectVariables(SymbolTable s) {
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
            copyParams = new JSONArray(params);
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(NodeFunction.class.getName()).log(Level.SEVERE, null, ex);
        }
        NodeFunction ret = new NodeFunction(parent, name, functionDef, copyParams);
        return ret;
    }
}
