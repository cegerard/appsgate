package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import org.json.JSONException;
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
    private String operator = null;
    /**
     * the left operation
     */
    private Object leftValue = null;
    /**
     * the node action of the left branch
     */
    private NodeAction leftNodeAction = null;
    /**
     * the return type of the left branch
     */
    private String leftReturnType = null;
    /**
     * the right operation
     */
    private Object rightValue = null;
    /**
     * the node action of the right branch
     */
    private NodeAction rightNodeAction = null;
    /**
     * the return type of the right branch
     */
    private String rightReturnType = null;
    /**
     * The result value
     */
    private Boolean result = null;

    /**
     * Default constructor
     *
     * @param relationBoolJSON JSON representation of the node
     * @param parent
     * @throws SpokNodeException
     */
    public NodeRelationBool(JSONObject relationBoolJSON, Node parent) throws SpokNodeException {
        super(parent);

        // operator
        operator = getJSONString(relationBoolJSON, "operator");

        JSONObject operand;

        // left operand
        operand = getJSONObject(relationBoolJSON, "leftOperand");
        if (operand.has("targetId")) {
            leftNodeAction = new NodeAction(operand, this);
            leftReturnType = getJSONString(operand, "returnType");
            leftValue = null;
        } else {
            leftReturnType = getJSONString(operand, "type");
            leftValue = parseValue(operand, leftReturnType);

        }

        // right operand
        operand = getJSONObject(relationBoolJSON, "rightOperand");
        if (operand.has("targetId")) {
            rightNodeAction = new NodeAction(operand, this);
            rightReturnType = getJSONString(operand, "returnType");
            rightValue = null;
        } else {
            rightReturnType = getJSONString(operand, "type");
            rightValue = parseValue(operand, rightReturnType);
        }

        result = null;
    }

    private NodeRelationBool(Node parent) {
        super(parent);
    }

    @Override
    public void specificStop() throws SpokException {
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
    public JSONObject call() {
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
            if (n.getResult() != null) {
                leftValue = parseValue(n.getResult().getJSONDescription(), leftReturnType);
            }
            // if the right operand is not a direct value, launch its interpretation...
            if (rightNodeAction != null) {
                rightNodeAction.addEndEventListener(this);
                rightNodeAction.call();
                return;
            }
        } else {
            // cast the value to the correct type
            if (n.getResult() != null) {
                rightValue = parseValue(n.getResult().getJSONDescription(), rightReturnType);
            }

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
    private Object parseValue(JSONObject obj, String type) {
        if (obj == null) {
            LOGGER.warn("A null value has been parsed");
            return null;
        }
        if (type.equals("number")) {
            return obj.optDouble("value");
        } else if (type.equals("boolean")) {
            return obj.optBoolean("value");
        } else if (type.equals("string")) {
            return obj.optString("value");
        }
        LOGGER.warn("Type unknown ({}), null has been returned", type);
        return null;
    }

    /**
     * Getter for the result of the boolean relation
     *
     * @return true or false according to the result of the boolean relation
     * @throws java.lang.Exception
     */
    public boolean getBooleanResult() throws Exception {
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
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("operator", operator);

            JSONObject leftOperand;
            if (leftNodeAction != null) {
                leftOperand = leftNodeAction.getJSONDescription();

            } else {
                leftOperand = new JSONObject();

                leftOperand.put("type", leftReturnType);
                leftOperand.put(leftReturnType, leftValue);
            }
            o.put("leftOperand", leftOperand);
            JSONObject rightOperand;
            if (rightNodeAction != null) {
                rightOperand = rightNodeAction.getJSONDescription();

            } else {
                rightOperand = new JSONObject();

                rightOperand.put("type", rightReturnType);
                rightOperand.put(rightReturnType, rightValue);
            }
            o.put("rightOperand", rightOperand);

        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;

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
    protected Node copy(Node parent) {
        NodeRelationBool ret = new NodeRelationBool(parent);
        ret.setSymbolTable(this.getSymbolTable());
        if (leftNodeAction != null) {
            ret.leftNodeAction = (NodeAction) leftNodeAction.copy(ret);
        }
        ret.leftReturnType = leftReturnType;
        ret.leftValue = leftValue;
        if (rightNodeAction != null) {
            ret.rightNodeAction = (NodeAction) rightNodeAction.copy(ret);
        }
        ret.rightReturnType = rightReturnType;
        ret.rightValue = rightValue;
        ret.operator = operator;
        ret.result = result;
        return ret;

    }
}
