package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.components.SpokVariable;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram.RUNNING_STATE;
import appsgate.lig.router.spec.GenericCommand;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node for the actions
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since May 22, 2013
 * @version 1.0.0
 *
 */
public class NodeAction extends Node {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeAction.class.getName());

    /**
     * the type of the target of the action ('device' or 'program')
     */
    private String targetType;
    /**
     * the id of the target of the action
     */
    private String targetId;
    /**
     * the name of the method to apply
     */
    private String methodName;
    /**
     * The args of the action
     */
    private JSONArray args = new JSONArray();
    /**
     * the command once it has been retrieve from interpreter
     */
    private GenericCommand command = null;

    /**
     * Default constructor
     *
     * @param interpreter points to the interpreter
     * @param ruleJSON the JSON Object
     * @param parent
     * @throws NodeException if the interpretation of JSON fails
     */
    public NodeAction(EUDEInterpreterImpl interpreter, JSONObject ruleJSON, Node parent)
            throws NodeException {
        super(interpreter, parent);

        targetType = getJSONString(ruleJSON, "targetType");
        targetId = getJSONString(ruleJSON, "targetId");
        methodName = getJSONString(ruleJSON, "methodName");
        if (ruleJSON.has("args")) {
            try {
                args = ruleJSON.getJSONArray("args");
            } catch (JSONException ex) {
                LOGGER.warn("An Exception has been thrown, args is not set");
            }
        }

    }

    private NodeAction(EUDEInterpreterImpl interpreter, Node parent) {
        super(interpreter, parent);
    }

    @Override
    public void endEventFired(EndEvent e) {
        setStarted(false);
        fireEndEvent(new EndEvent(this));
    }

    @Override
    public Integer call() {
        LOGGER.debug("##### Action call [{}]!", methodName);
        fireStartEvent(new StartEvent(this));
        setStarted(true);
        if (targetType.equals("device")) {
            callDeviceAction(targetId);
        } else if (targetType.equals("program")) {
            callProgramAction(targetId);
        } else if (targetType.equals("list")) {
            callListAction(targetId);
        } else {
            LOGGER.warn("Action type ({}) not supported", targetType);
        }

        setStarted(false);
        fireEndEvent(new EndEvent(this));
        return null;
    }

    /**
     * Method that run a device action
     */
    private void callDeviceAction(String target) {
        // get the runnable from the interpreter
        command = getInterpreter().executeCommand(target, methodName, args);
        if (command == null) {
            LOGGER.error("Command not found {}, for {}", methodName, target);
        } else {
            LOGGER.debug("Run command: {}", command.toString());
            command.run();
        }
    }

    /**
     * Method to run a program action
     */
    private void callProgramAction(String target) {
        NodeProgram p = getInterpreter().getNodeProgram(target);

        if (p != null) {
            LOGGER.debug("Program running state {}", p.getRunningState());
            if (methodName.contentEquals("callProgram") && p.getRunningState() != RUNNING_STATE.STARTED) {
                // listen to the end of the program
                p.addEndEventListener(this);
                // launch the program
                getInterpreter().callProgram(target);
            } else if (methodName.contentEquals("stopProgram") && p.getRunningState() == RUNNING_STATE.STARTED) {
                //stop the running program
                getInterpreter().stopProgram(target);
            } else {
                LOGGER.warn("Cannot run {} on program {}", methodName, target);
            }

        } else {
            LOGGER.error("Program not found: {}", targetId);
        }
    }

    /**
     * 
     * @param target 
     */
    private void callListAction(String target) {
                LOGGER.debug("Call List action");

        SpokVariable list = getVariableByName(target);
        if (list == null) {
            LOGGER.error("No such variable found in the symbol table");
            return;
        }
        List<SpokVariable> elements = list.getElements();
        for (SpokVariable v : elements) {
            
            callVariableAction(v, target);
        }
    }

    /**
     * 
     * @param v
     * @param target 
     */
    private void callVariableAction(SpokVariable v, String target) {
        LOGGER.debug("Call Variable action: {}", v.getName());
        if (v.getType().equals("variable")) {
            if (v.getName().equals(target)) {
                LOGGER.warn("Stopping cause there was a recursive loop");
                return;
            }
            callVariableAction(getVariableByName(v.getName()), v.getName());
        } else if (v.getType().equals("device")) {
            callDeviceAction(v.getName());
        } else if (v.getType().equals("list")) {
            callListAction(v.getName());
        }

    }

    /**
     *
     * @return an object containing the return of a command, null if no command
     * has been passed
     */
    public Object getResult() {
        if (command != null) {
            return command.getReturn();
        } else {
            return null;
        }
    }

    @Override
    public void specificStop() {
        if (targetType.equals("program")) {
            NodeProgram p = getInterpreter().getNodeProgram(targetId);
            p.stop();
            setStopping(false);
        } else {
            LOGGER.warn("Trying to stop an action ({}) which is not a program", this);
        }
    }

    @Override
    public String toString() {
        return "[Node Action: " + methodName + " on " + targetId + "]";

    }

    @Override
    public String getExpertProgramScript() {
        String ret;
        ret = this.getElementKey(targetId, targetType) + ".";
        String cmd = "";
        if (this.command != null) {
            cmd = this.command.toString();
        }
        return ret + this.methodName + "(\"" + cmd + "\");";

    }

    @Override
    protected void collectVariables(SymbolTable s) {
        s.addAnonymousVariable(targetId, targetType);
    }
    @Override
    Node copy(Node parent) {
        NodeAction ret = new NodeAction(getInterpreter(), parent);
        ret.setSymbolTable(this.getSymbolTable());
        try {
            ret.args = new JSONArray(args);
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(NodeAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        ret.methodName = methodName;
        ret.targetId = targetId;
        ret.targetType = targetType;
        ret.command = command;
        return ret;

    }

}
