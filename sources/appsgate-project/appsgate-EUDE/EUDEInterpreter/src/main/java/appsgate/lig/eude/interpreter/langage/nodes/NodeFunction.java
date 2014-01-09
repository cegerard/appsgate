package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokFunction;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
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
     *
     */
    private SpokFunction function;

    /**
     * Initialize the program from a JSON object
     *
     * @param interpreter
     * @param programJSON Abstract tree of the program in JSON
     * @param parent
     * @throws appsgate.lig.eude.interpreter.langage.nodes.NodeException
     */
    public NodeFunction(EUDEInterpreterImpl interpreter, JSONObject programJSON, Node parent)
            throws NodeException {
        super(interpreter, parent);
        name = getJSONString(programJSON, "name");
    }

    /**
     * Launch the interpretation of the rules
     *
     * @return integer
     */
    @Override
    public Integer call() {
        LOGGER.info("The function {} has been called.", this);
        return 1;
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
        return name + "()";
    }

    @Override
    protected void collectVariables(SymbolTable s) {
    }

}
