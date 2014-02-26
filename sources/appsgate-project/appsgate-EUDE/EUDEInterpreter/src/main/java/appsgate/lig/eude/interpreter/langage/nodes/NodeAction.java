package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokVariable;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram.RUNNING_STATE;
import appsgate.lig.router.spec.GenericCommand;
import java.util.List;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeAction.class);

    /**
     * the type of the target of the action ('device' or 'program')
     */
    private Node target;
    /**
     * the name of the method to apply
     */
    private String methodName;
    /**
     * The args of the action
     */
    private JSONArray args;
    /**
     * the command once it has been retrieve from interpreter
     */
    private GenericCommand command = null;

    private String returnType = null;

    /**
     * Default constructor
     *
     * @param ruleJSON the JSON Object
     * @param parent
     * @throws SpokNodeException if the interpretation of JSON fails
     */
    public NodeAction(JSONObject ruleJSON, Node parent)
            throws SpokNodeException {
        super(parent);
        try {
            target = Builder.buildFromJSON(getJSONObject(ruleJSON, "target"), this);
        } catch (SpokTypeException ex) {
            LOGGER.error("Unable to build the target of the action node");
            throw new SpokNodeException("NodeAction", "value", ex);
        }
        methodName = ruleJSON.optString("methodName");
        args = ruleJSON.optJSONArray("args");
        if (args == null) {
            args = new JSONArray();
        }
        returnType = ruleJSON.optString("returnType");

    }

    /**
     * Private constructor to allow copy function
     *
     * @param parent
     */
    private NodeAction(Node parent) {
        super(parent);
    }

    @Override
    public void endEventFired(EndEvent e) {
        callAction();
        setStarted(false);
        fireEndEvent(new EndEvent(this));
    }

    @Override
    public JSONObject call() {
        LOGGER.debug("##### Action call [{}]!", methodName);
        fireStartEvent(new StartEvent(this));
        setStarted(true);
        target.addEndEventListener(this);
        
        return target.call();
    }

    /**
     * 
     */
    private void callAction() {
        try {
            if (target.getType().equals("device")) {
                callDeviceAction(target.getValue());
            } else if (target.getType().equals("programcall")) {
                callProgramAction(target.getValue());
            } else if (target.getType().equals("list")) {
                callListAction((SpokVariable)target.getResult());
            } else if (target.getType().equals("variable")) {
                callVariableAction(getVariableByName(target.getValue()), target.getValue());
            } else {
                LOGGER.error("Action type ({}) not supported", target.getType());
            }
        } catch (SpokException e) {
            LOGGER.error("Error at execution: " + e);
        }

    }

    /**
     * Method that run a device action
     *
     * @param target
     * @throws SpokException
     */
    private void callDeviceAction(String target) throws SpokException {
        // get the runnable from the interpreter
        command = getMediator().executeCommand(target, methodName, args);
        if (command == null) {
            LOGGER.error("Command not found {}, for {}", methodName, target);
        } else {
            LOGGER.debug("Run command: {}", command.toString());
            command.run();
        }
    }

    /**
     * Method to run a program action
     *
     * @param target
     * @throws SpokException
     */
    private void callProgramAction(String target) throws SpokException {
        NodeProgram p = getMediator().getNodeProgram(target);

        if (p != null) {
            LOGGER.debug("Program running state {}", p.getRunningState());
            if (methodName.contentEquals("callProgram") && p.getRunningState() != RUNNING_STATE.STARTED) {
                // listen to the end of the program
                p.addEndEventListener(this);
                // launch the program
                getMediator().callProgram(target);
            } else if (methodName.contentEquals("stopProgram") && p.getRunningState() == RUNNING_STATE.STARTED) {
                //stop the running program
                getMediator().stopProgram(target);
            } else {
                LOGGER.warn("Cannot run {} on program {}", methodName, target);
            }

        } else {
            LOGGER.error("Program not found: {}", this.target.getValue());
        }
    }

    /**
     * Method to run a list of action
     *
     * @param target
     * @throws SpokException
     */
    private void callListAction(SpokVariable list) throws SpokException {
        LOGGER.debug("Call List action");

        List<SpokVariable> elements = list.getElements();
        for (SpokVariable v : elements) {
            callVariableAction(v, "");
        }
    }

    /**
     *
     * @param v
     * @param target
     */
    private void callVariableAction(SpokVariable v, String target) throws SpokException {
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
            callListAction(v);
        }

    }

    /**
     *
     * @return an object containing the return of a command, null if no command
     * has been passed
     */
    @Override
    public SpokObject getResult() {
        if (command != null) {
            return new SpokVariable(returnType, command.getReturn());
        } else {
            return null;
        }
    }

    @Override
    public void specificStop() {
        if (target.getType().equals("programCall")) {
            NodeProgram p = null;
            try {
                p = getMediator().getNodeProgram(target.getValue());
            } catch (SpokExecutionException ex) {
                LOGGER.warn("The mediator has not been found");
            }
            if (p != null) {
                p.stop();
            }
            setStopping(false);
        } else {
            LOGGER.warn("Trying to stop an action ({}) which is not a program", this);
        }
    }

    @Override
    public String toString() {
        return "[Node Action: " + methodName + " on " + target.getValue() + "]";

    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", "action");
            o.put("target", target.getJSONDescription());
            o.put("methodName", methodName);
            o.put("args", args);
            o.put("returnType", returnType);
        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;

    }

    @Override
    public String getExpertProgramScript() {
        String ret;
        ret = target.getExpertProgramScript() + ".";
        String cmd = "";
        if (this.command != null) {
            cmd = this.command.toString();
        }
        return ret + this.methodName + "(\"" + cmd + "\");";

    }

    @Override
    protected Node copy(Node parent) {
        NodeAction ret = new NodeAction(parent);
        try {
            ret.args = new JSONArray(args.toString());
        } catch (JSONException ex) {
        }
        ret.returnType = returnType;
        ret.methodName = methodName;
        ret.target = target.copy(parent);
        ret.command = command;
        return ret;

    }

}
