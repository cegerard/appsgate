package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram.RUNNING_STATE;
import appsgate.lig.router.spec.GenericCommand;
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
    private final String targetType;
    /**
     * the id of the target of the action
     */
    private final String targetId;
    /**
     * the name of the method to apply
     */
    private final String methodName;
    /**
     * The args of the action
     */
    private JSONArray args;
    /**
     * the command once it has been retrieve from interpreter
     */
    private GenericCommand command;

    /**
     * Default constructor
     *
     * @param interpreter points to the interpreter
     * @param ruleJSON the JSON Object
     * @throws NodeException if the interpretation of JSON fails
     */
    public NodeAction(EUDEInterpreterImpl interpreter, JSONObject ruleJSON)
            throws NodeException {
        super(interpreter);

        targetType = getJSONString(ruleJSON, "targetType");
        targetId = getJSONString(ruleJSON, "targetId");
        methodName = getJSONString(ruleJSON, "methodName");
        if (ruleJSON.has("args")) {
            try {
                args = ruleJSON.getJSONArray("args");
            } catch (JSONException ex) {
                LOGGER.warn("An Exception has been thrown, args for this node has been set to empty array");
            }
        } else {
            args = new JSONArray();
        }

        command = null;
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
            // get the runnable from the interpreter
            command = executeCommand(targetId, methodName, args);
            if (command == null) {
                LOGGER.error("Command not found {}, for {}", methodName, targetId);
            } else {
                LOGGER.debug("Run command: {}", command.toString());
                command.run();
            }
        } else if (targetType.equals("program")) {

            NodeProgram p =  getNodeProgram(targetId);

            if (p != null) {
                LOGGER.debug("Program running state {}", p.getRunningState());
                if (methodName.contentEquals("callProgram") && p.getRunningState() != RUNNING_STATE.STARTED) {
                    // listen to the end of the program
                    p.addEndEventListener(this);
                    // launch the program
                    callProgram(targetId);
                } else if (methodName.contentEquals("stopProgram") && p.getRunningState() == RUNNING_STATE.STARTED) {
                    //stop the running program
                    stopProgram(targetId);
                } else {
                    LOGGER.warn("Cannot run {} on program {}", methodName, targetId);
                }

            } else {
                LOGGER.error("Program not found: {}", targetId);
            }
        } else {
            LOGGER.warn("Action type ({}) not supported", targetType);
        }

        setStarted(false);
        fireEndEvent(new EndEvent(this));
        return null;
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
    public void stop() {
        if (isStarted() && targetType.equals("program") && !isStopping()) {
            setStopping(true);
            NodeProgram p = getNodeProgram(targetId);
            p.stop();
            setStarted(false);
            setStopping(false);
        }
    }
    
    @Override
    public String toString() {
        return "[Node Action: " + methodName + " on " + targetId + "]";
        
    }

}
