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

    private final String targetType;
    private final String targetId;
    private final String methodName;
    private final JSONArray args;
    private GenericCommand command;

    /**
     * Default constructor
     *
     * @param interpreter
     * @param ruleJSON
     * @constructor
     * @throws JSONException
     */
    public NodeAction(EUDEInterpreterImpl interpreter, JSONObject ruleJSON) throws JSONException {
        super(interpreter);

        targetType = ruleJSON.getString("targetType");
        targetId = ruleJSON.getString("targetId");
        methodName = ruleJSON.getString("methodName");
        args = ruleJSON.getJSONArray("args");

        command = null;
    }

    @Override
    public void endEventFired(EndEvent e) {
        LOGGER.debug("##### End event received!");
        ((Node) e.getSource()).removeEndEventListener(this);
        started = false;
        fireEndEvent(new EndEvent(this));
    }

    @Override
    public Integer call() {
        LOGGER.debug("##### Action call [{}]!", methodName);
        fireStartEvent(new StartEvent(this));
        started = true;
        if (targetType.equals("device")) {
            // get the runnable from the interpreter
            command = interpreter.executeCommand(targetId, methodName, args);
            if (command == null) {
                LOGGER.error("Command not found {}, for {}", methodName, targetId);
            } else {
                LOGGER.debug("Run command: {}", command.toString());
                command.run();
            }
        } else if (targetType.equals("program")) {

            NodeProgram p = (NodeProgram) interpreter.getNodeProgram(targetId);

            if (p != null) {
                LOGGER.debug("Program running state {}", p.getRunningState());
                if (methodName.contentEquals("callProgram") && p.getRunningState() != RUNNING_STATE.STARTED) {
                    // listen to the end of the program
                    p.addEndEventListener(this);
                    // launch the program
                    interpreter.callProgram(targetId);
                } else if (methodName.contentEquals("stopProgram") && p.getRunningState() == RUNNING_STATE.STARTED) {
                    //stop the running program
                    interpreter.stopProgram(targetId);
                } else {
                    LOGGER.warn("Cannot run {} on program {}", methodName, targetId);
                }

            } else {
                LOGGER.error("Program not found: {}", targetId);
            }
        } else {
            LOGGER.warn("Action type ({}) not supported", targetType);
        }

        started = false;
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
        if (started && targetType.equals("program") && !stopping) {
            stopping = true;
            NodeProgram p = (NodeProgram) interpreter.getNodeProgram(targetId);
            p.stop();
            started = false;
            stopping = false;
        }
    }

}
