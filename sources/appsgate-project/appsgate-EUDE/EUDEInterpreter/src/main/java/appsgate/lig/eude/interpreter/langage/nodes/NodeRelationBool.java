package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeProgram.class);
    
    private final String operator;
    private Object leftValue;
    private NodeAction leftNodeAction;
    private final String leftReturnType;
    private Object rightValue;
    private NodeAction rightNodeAction;
    private final String rightReturnType;
    private Boolean result;

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

    /**
     * Default constructor
     *
     * @param interpreter Pointer on the interpreter
     * @param relationBoolJSON JSON representation of the node
     * @throws appsgate.lig.eude.interpreter.langage.nodes.NodeException
     */
    public NodeRelationBool(EUDEInterpreterImpl interpreter, JSONObject relationBoolJSON) throws NodeException {
        super(interpreter);

        // operator
        operator = getJSONString(relationBoolJSON, "operator");
        
        JSONObject operand;

        // left operand
        operand = getJSONObject(relationBoolJSON, "leftOperand");
        if (operand.has("deviceId")) {
            leftNodeAction = new NodeAction(interpreter, operand);
            leftReturnType = getJSONString(operand, "returnType");
            leftValue = null;
        } else {
            leftReturnType = getJSONString(operand, "type");
            String valueJSON = getJSONString(operand, "value");
            
            if (leftReturnType.equals("number")) {
                leftValue = new Double(valueJSON);
            } else if (leftReturnType.equals("boolean")) {
                leftValue = Boolean.valueOf(valueJSON);
            } else if (leftReturnType.equals("string")) {
                leftValue = valueJSON;
            }
        }

        // right operand
        operand = getJSONObject(relationBoolJSON, "rightOperand");
        if (operand.has("deviceId")) {
            rightNodeAction = new NodeAction(interpreter, operand);
            rightReturnType = getJSONString(operand, "returnType");
            rightValue = null;
        } else {
            rightReturnType = getJSONString(operand, "type");
            String valueJSON = getJSONString(operand, "value");
            
            if (rightReturnType.equals("number")) {
                rightValue = new Double(valueJSON);
            } else if (rightReturnType.equals("boolean")) {
                rightValue = Boolean.valueOf(valueJSON);
            } else if (rightReturnType.equals("string")) {
                rightValue = valueJSON;
            }
        }
        
        result = null;
    }

    /**
     * Parse a JSON operand
     *
     * @param operand JSON object representing an operand of a boolean relation
     * @param value
     * @throws JSONException
     */
    private void parseOperand(JSONObject operand, NodeAction nodeAction, Object value) throws JSONException, NodeException {
        if (operand.has("deviceId")) {
            nodeAction = new NodeAction(interpreter, operand);
            value = null;
        } else {
            String type = operand.getString("type");
            String valueJSON = operand.getString("value");
            
            if (type.equals("number")) {
                value = Double.parseDouble(valueJSON);
            } else if (type.equals("boolean")) {
                value = Boolean.parseBoolean(valueJSON);
            } else if (type.equals("string")) {
                value = valueJSON;
            }
            nodeAction = null;
        }
    }
    
    @Override
    public void stop() {
        if (started) {
            stopping = true;
            if (leftNodeAction != null) {
                leftNodeAction.removeEndEventListener(this);
                leftNodeAction.stop();
            } else {
                rightNodeAction.removeEndEventListener(this);
                rightNodeAction.stop();
            }
            started = false;
            stopping = false;
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
        started = true;

        // if the both operands are direct value, compute the final result and fire the end event
        if (leftNodeAction == null && rightNodeAction == null) {
            computeResult();
            started = false;
            fireEndEvent(new EndEvent(this));
            return null;
        }

        // interpret the left operand first if possible
        if (leftNodeAction != null) {
            leftNodeAction.addEndEventListener(this);
            //pool.submit(leftNodeAction);
            leftNodeAction.call();
        } else {
            rightNodeAction.addEndEventListener(this);
            //pool.submit(rightNodeAction);
            rightNodeAction.call();
        }
        
        return null;
    }

    /**
     * Compute the final result according to the operator
     */
    private void computeResult() {
        if (operator.equals("==")) {
            result = (leftValue.equals(rightValue));
        } else if (operator.equals("!=")) {
            result = !(leftValue.equals(rightValue));
        } else if (operator.equals(">=")) {
            if (leftReturnType.equals("number") && rightReturnType.equals("number")) {
                result = (((Double) leftValue) >= ((Double) rightValue));
            } else {
                result = false;
            }
        } else if (operator.equals("<=")) {
            if (leftReturnType.equals("number") && rightReturnType.equals("number")) {
                System.out.println(leftValue.toString() + " <= " + rightValue.toString());
                result = (((Double) leftValue) <= ((Double) rightValue));
            } else {
                result = false;
            }
        }
    }

    /**
     * Called when an operand is done.
     *
     * @param e
     */
    @Override
    public void endEventFired(EndEvent e) {
        NodeAction n = (NodeAction) e.getSource();
        n.removeEndEventListener(this);
        
        
        
        if (n == leftNodeAction) {
            // cast the value to the correct type
            if (leftReturnType.equals("number")) {
                leftValue = new Double((n.getResult().toString()));
            } else if (leftReturnType.equals("boolean")) {
                leftValue = Boolean.valueOf(n.getResult().toString());
            } else if (leftReturnType.equals("string")) {
                leftValue = (String) n.getResult();
            }

            // if the right operand is not a direct value, launch its interpretation...
            if (rightNodeAction != null) {
                rightNodeAction.addEndEventListener(this);
                rightNodeAction.call();
                //pool.submit(rightNodeAction);
                // ... compute the final result and fire the end event otherwise
            } else {
                computeResult();
                started = false;
                fireEndEvent(new EndEvent(this));
            }
        } else {
            // cast the value to the correct type
            if (rightReturnType.equals("number")) {
                rightValue = new Double(n.getResult().toString());
            } else if (rightReturnType.equals("boolean")) {
                rightValue = Boolean.valueOf(n.getResult().toString());
            } else if (rightReturnType.equals("string")) {
                rightValue = (String) n.getResult();
            }

            // compute the final result and fire the end result
            computeResult();
            started = false;
            fireEndEvent(new EndEvent(this));
        }
    }
}
