package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.eude.interpreter.langage.exceptions.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.spec.ProgramCommandNotification;

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
public class NodeAction extends Node implements ICanBeEvaluated {

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
        super(parent, ruleJSON);
        target = getDevice(ruleJSON, "target");
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
     * @param parent the parent node
     * @param id the id of the node
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
        LOGGER.trace("Action call [{}]!", methodName);
        fireStartEvent(new StartEvent(this));
        setStarted(true);
        target.addEndEventListener(this);
        setProgramProcessing();

        return target.call();
    }

    /**
     *
     */
    private void callAction() {
        try {
            if (target.getType().equalsIgnoreCase("device") || target.getType().equalsIgnoreCase("service")) {
                callDeviceAction(target.getValue());
            } else if (target.getType().equalsIgnoreCase("programcall")) {
                callProgramAction(target.getValue());
            } else if (target.getType().equalsIgnoreCase("list")) {
                callListAction(((ICanBeEvaluated) target).getResult());
            } else if (target.getType().equalsIgnoreCase("variable")) {
                NodeVariableDefinition var = getVariableByName(target.getValue());
                callVariableAction(var, target.getValue());
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
        LOGGER.debug("Device action {} on {}", methodName, target);
        ProgramCommandNotification notif;
        if (returnType.isEmpty()) {
            notif = getProgramLineNotification(null, target, "Acting on a device", ProgramCommandNotification.Type.WRITE);
        } else {
            notif = getProgramLineNotification(null, target, "Reading from", ProgramCommandNotification.Type.READ);
        }

        command = getMediator().executeCommand(target, methodName, args, notif);
    }

    /**
     * Method to run a program action
     *
     * @param target
     * @throws SpokException
     */
    private void callProgramAction(String target) throws SpokException {
        LOGGER.debug("Program [{}] action {} on {}", new Object[]{getProgramName(), methodName, target});

        NodeProgram p = getMediator().getNodeProgram(target);
        if (p != null) {
            if (methodName.contentEquals("callProgram") && p.canRun()) {
                // launch the program
                getMediator().callProgram(target, args);
            } else if (methodName.contentEquals("stopProgram") && p.isRunning()) {
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
    private void callListAction(INodeList n) throws SpokException {
        LOGGER.debug("Call List action");
        // Node n =  list.getNodeValue();
        if (n != null) {
            List<NodeValue> elements = n.getElements();
            int i = 0;
            for (NodeValue v : elements) {
                callVariableAction(v, "listElement " + i);
            }
        }
    }

    /**
     *
     * @param v
     * @param target
     */
    private void callVariableAction(SpokObject v, String target) throws SpokException {
        if (v == null) {
            LOGGER.error("Unable to find variable: " + target);
            return;
        }
        if (v.getType().equalsIgnoreCase("variable")) {
            NodeVariableDefinition var = getVariableByName(v.getValue());
            callVariableAction(var, v.getValue());
        } else if (v.getType().equalsIgnoreCase("device")) {
            callDeviceAction(v.getValue());
        } else if (v.getType().equalsIgnoreCase("list")) {
            callListAction((INodeList) v);
        } else {
            LOGGER.warn("Action has not been executed: {}", this);
            LOGGER.debug("The type was: {}", v.getType());
        }

    }

    /**
     *
     * @return an object containing the return of a command, null if no command
     * has been passed
     */
    @Override
    public NodeValue getResult() {
        if (command != null) {
            Object ret = command.getReturn();
            if (ret != null) {
                LOGGER.trace("Return value: " + ret.toString());
                return new NodeValue(returnType, ret.toString(), this);
            } else {
                LOGGER.debug("No return value");
                return null;
            }

        } else {
            LOGGER.warn("There is no command to get a result.");
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
    public String getTypeSpec() {
        return "Action: " + methodName + " on " + target.getValue();

    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
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

    @Override
    public String getResultType() {
        if (this.returnType == null) {
            LOGGER.debug("this action has no return type");
        }
        return this.returnType;
    }

    @Override
    protected void buildReferences(ReferenceTable r) {
        if (target != null) {
            target.buildReferences(r);
        }
    }

}
