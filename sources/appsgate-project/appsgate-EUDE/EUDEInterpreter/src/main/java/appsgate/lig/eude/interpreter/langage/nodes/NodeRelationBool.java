package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node for the boolean relations
 *
 * <relationBool> ::= <term> <opComparison> <term>
 * <term> ::= NodeAction | number | string | boolean
 * <term> can be a NodeAction in the case where the value is provided by a
 * device. In this case, a method call has to be performed
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since June 19, 2013
 * @version 1.0.0
 */
public class NodeRelationBool extends Node {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeRelationBool.class);

    /**
     * The operator of the boolean operation
     */
    private final String operator;
    /**
     * the left operation
     */
    private Object leftValue;
    /**
     * the node action of the left branch
     */
    private NodeAction leftNodeAction = null;
    /**
     * the return type of the left branch
     */
    private final String leftReturnType;
    /**
     * the right operation
     */
    private Object rightValue;
    /**
     * the node action of the right branch
     */
    private NodeAction rightNodeAction = null;
    /**
     * the return type of the right branch
     */
    private final String rightReturnType;
    /**
     * The result value
     */
    private Boolean result;

    /**
     * Default constructor
     *
     * @param interpreter Pointer on the interpreter
     * @param relationBoolJSON JSON representation of the node
     * @param parent
     * @throws appsgate.lig.eude.interpreter.langage.nodes.NodeException
     */
    public NodeRelationBool(EUDEInterpreterImpl interpreter, JSONObject relationBoolJSON, Node parent) throws NodeException {
        super(interpreter, parent);

        // operator
        operator = getJSONString(relationBoolJSON, "operator");

        JSONObject operand;

        // left operand
        operand = getJSONObject(relationBoolJSON, "leftOperand");
        if (operand.has("targetId")) {
            leftNodeAction = new NodeAction(interpreter, operand, this);
            leftReturnType = getJSONString(operand, "returnType");
            leftValue = null;
        } else {
            leftReturnType = getJSONString(operand, "type");
            leftValue = parseValue(getJSONString(operand, "value"), leftReturnType);

        }

        // right operand
        operand = getJSONObject(relationBoolJSON, "rightOperand");
        if (operand.has("targetId")) {
            rightNodeAction = new NodeAction(interpreter, operand, this);
            rightReturnType = getJSONString(operand, "returnType");
            rightValue = null;
        } else {
            rightReturnType = getJSONString(operand, "type");
            rightValue = parseValue(getJSONString(operand, "value"), rightReturnType);
        }

        result = null;
    }

    @Override
    public void specificStop() {
        if (leftNodeAction != null) {
            leftNodeAction.removeEndEventListener(this);
            leftNodeAction.stop();
        } else {
            rightNodeAction.removeEndEventListener(this);
            rightNodeAction.stop();
        }
    }

    /**
     * Launch the interpretation of the node.
     *
     * @return
     */
    @Override
    public Integer call() {
        // fire the start event
        fireStartEvent(new StartEvent((this)));
        setStarted(true);

        // if the both operands are direct value, compute the final result and fire the end event
        if (leftNodeAction == null && rightNodeAction == null) {
            result = computeResult();
            setStarted(false);
            fireEndEvent(new EndEvent(this));
            return null;
        }

        // interpret the left operand first if possible
        if (leftNodeAction != null) {
            leftNodeAction.addEndEventListener(this);
            leftNodeAction.call();
        } else {
            rightNodeAction.addEndEventListener(this);
            rightNodeAction.call();
        }

        return null;
    }

    /**
     * Compute the final result according to the operator
     */
    private boolean computeResult() {
        if (leftValue == null) {
            return false;
        }
        if (operator.equals("==")) {
            return (leftValue.equals(rightValue));
        }
        if (operator.equals("!=")) {
            return !(leftValue.equals(rightValue));
        }
        // Numerical comparison
        if (leftReturnType.equals("number") && rightReturnType.equals("number")) {
            if (operator.equals(">=")) {
                return (((Double) leftValue) >= ((Double) rightValue));
            } else if (operator.equals("<=")) {
                return (((Double) leftValue) <= ((Double) rightValue));
            }
        }
        return false;
    }

    /**
     * Called when an operand is done.
     *
     * @param e
     */
    @Override
    public void endEventFired(EndEvent e) {
        NodeAction n = (NodeAction) e.getSource();

        if (n == leftNodeAction) {
            // cast the value to the correct type
            leftValue = parseValue(n.getResult(), leftReturnType);

            // if the right operand is not a direct value, launch its interpretation...
            if (rightNodeAction != null) {
                rightNodeAction.addEndEventListener(this);
                rightNodeAction.call();
                return;
            }
        } else {
            // cast the value to the correct type
            rightValue = parseValue(n.getResult(), rightReturnType);

        }
        // compute the final result and fire the end result
        result = computeResult();
        setStarted(false);
        fireEndEvent(new EndEvent(this));

    }

    /**
     *
     * @param obj the object to parse
     * @param type the type of object to obtain
     * @return the new value, null if no type has been recognized or if the obj
     * is null
     */
    private Object parseValue(Object obj, String type) {
        if (obj == null) {
            LOGGER.warn("A null value has been parsed");
            return null;
        }
        if (type.equals("number")) {
            return new Double((obj.toString()));
        } else if (type.equals("boolean")) {
            return Boolean.valueOf(obj.toString());
        } else if (type.equals("string")) {
            return (String) obj;
        }
        LOGGER.warn("A null value has been parsed");
        return null;
    }

    /**
     * Getter for the result of the boolean relation
     *
     * @return true or false according to the result of the boolean relation
     * @throws java.lang.Exception
     */
    public boolean getResult() throws Exception {
        if (result != null) {
            return result;
        }
        throw new Exception("result has not been computed yet");
    }

    @Override
    public String toString() {
        return "[Node RelationBool: '" + leftReturnType + "'" + operator + "'" + rightReturnType + "]";
    }

    @Override
    public String getExpertProgramScript() {
        return "(" + getScript(leftReturnType, leftNodeAction, leftValue) + operator + getScript(rightReturnType, rightNodeAction, rightValue) + ")";
    }

    private String getScript(String retType, NodeAction action, Object val) {
        if (action != null) {
            return action.getExpertProgramScript();
        }
        if (retType.equalsIgnoreCase("number")) {
            return val.toString();
        }
        if (retType.equalsIgnoreCase("boolean")) {
            if ((Boolean) val) {
                return "true";
            } else {
                return "false";
            }
        } else {
            return "\"" + val.toString() + "\"";
        }

    }

    @Override
    protected void collectVariables(SymbolTable s) {
    }

}
