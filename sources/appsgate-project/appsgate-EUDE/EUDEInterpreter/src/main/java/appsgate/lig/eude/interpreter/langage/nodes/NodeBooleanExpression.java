/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokParser;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeBooleanExpression extends Node implements INodeFunction {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeBooleanExpression.class);

    /**
     * Different operator that are supported by the language
     */
    public static enum BinaryOperator {

        EQUALS("=="), NOT_EQUALS("!="), MORE_THAN(">"), LESS_THAN("<"), OR("||"), AND("&&"), NOT("!");

        private final String val;

        BinaryOperator(String s) {
            this.val = s;
        }

        public String getVal() {
            return val;
        }

        public final static BinaryOperator get(String s) {
            for (BinaryOperator o : BinaryOperator.values()) {
                if (o.val.equalsIgnoreCase(s)) {
                    return o;
                }
            }
            return null;
        }
    }

    /**
     * The left node to evaluate
     */
    private INodeFunction left = null;
    private Node leftNode;

    /**
     * The right node to evaluate
     */
    private INodeFunction right = null;
    private Node rightNode;
    /**
     * the operator of the expression
     */
    private BinaryOperator operator = null;

    /**
     * private constructor to allow the copy function
     *
     * @param p
     */
    private NodeBooleanExpression(Node p, String id) {
        super(p, id);
    }

    /**
     *
     * @param o
     * @param parent
     * @throws SpokNodeException
     */
    public NodeBooleanExpression(JSONObject o, Node parent) throws SpokNodeException {
        super(parent, o.optString("iid"));
        operator = BinaryOperator.get(getJSONString(o, "operator"));
        if (operator == null) {
            LOGGER.debug("Unknown operator: {}", getJSONString(o, "operator"));
            throw new SpokNodeException("BooleanExpression", "operator", null);
        }
        try {
            leftNode = Builder.buildFromJSON(o.optJSONObject("leftOperand"), this);
            if (!(leftNode instanceof INodeFunction)) {
                LOGGER.error("Left operand does not return a value");
                throw new SpokNodeException("BooleanExpression", "leftOperand", null);
            }
            left = (INodeFunction) leftNode;
        } catch (SpokTypeException ex) {
            LOGGER.error("Missing left operand");
            throw new SpokNodeException("BooleanExpression", "leftOperand", ex);
        }
        try {
            rightNode = Builder.buildFromJSON(o.optJSONObject("rightOperand"), this);
            if (!(rightNode instanceof INodeFunction)) {
                LOGGER.error("right operand does not return a value");
                throw new SpokNodeException("BooleanExpression", "rightOperand", null);
            }
            right = (INodeFunction) rightNode;
        } catch (SpokTypeException ex) {
            if (needTwoOperands(operator)) {
                LOGGER.debug("Missing right operand");
                throw new SpokNodeException("BooleanExpression", "rightOperand", null);
            }
        }

    }

    @Override
    protected void specificStop() {
        if (left != null) {
            leftNode.stop();
        }
        if (right != null) {
            rightNode.stop();
        }
    }

    @Override
    public JSONObject call() {
        setStarted(true);

        if (left == null) {
            LOGGER.error("A left operand is null: unable to evaluate this node");
            SpokExecutionException ex = new SpokExecutionException("There were no left branch to evaluate");
            return ex.getJSONDescription();
        }
        leftNode.addEndEventListener(this);
        return leftNode.call();
    }

    @Override
    public String getExpertProgramScript() {
        return "(" + leftNode.getExpertProgramScript() + operator.getVal() + rightNode.getExpertProgramScript() + ")";
    }

    @Override
    protected Node copy(Node parent) {
        NodeBooleanExpression ret = new NodeBooleanExpression(parent, getIID());
        ret.operator = operator;
        ret.leftNode = leftNode.copy(ret);
        ret.left = (INodeFunction) ret.leftNode;
        ret.rightNode = rightNode.copy(ret);
        ret.right = (INodeFunction) ret.rightNode;
        return ret;
    }

    @Override
    public void endEventFired(EndEvent e
    ) {
        Node n = (Node) e.getSource();
        if (n == left) {
            leftNodeEvaluated();
            return;
        }
        if (n == right) {
            rightNodeEvaluated();
            return;
        }
        LOGGER.warn("An end event has been fired: " + e);
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject ret = super.getJSONDescription();
        try {
            ret.put("type", "booleanExpression");
            ret.put("operator", operator);
            ret.put("leftOperand", leftNode.getJSONDescription());
            ret.put("rightOperand", rightNode.getJSONDescription());

        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return ret;
    }

    @Override
    public NodeValue getResult() throws SpokExecutionException {
        Boolean result = false;
        try {
            switch (operator) {
                case EQUALS:
                    result = SpokParser.equals(left.getResult(), right.getResult());
                    break;
                case NOT_EQUALS:
                    result = !SpokParser.equals(left.getResult(), right.getResult());
                    break;
                case MORE_THAN:
                    result = SpokParser.getNumericResult(left.getResult()) > SpokParser.getNumericResult(right.getResult());
                    break;
                case LESS_THAN:
                    result = SpokParser.getNumericResult(left.getResult()) < SpokParser.getNumericResult(right.getResult());
                    break;
                case OR:
                    result = SpokParser.getBooleanResult(left.getResult()) || SpokParser.getBooleanResult(right.getResult());
                    break;
                case AND:
                    result = SpokParser.getBooleanResult(left.getResult()) && SpokParser.getBooleanResult(right.getResult());
                    break;
                case NOT:
                    result = !SpokParser.getBooleanResult(left.getResult());
                    break;
                default:
                    throw new AssertionError(operator.name());

            }

        } catch (SpokTypeException ex) {
            LOGGER.error("Unable to parse the result");
            throw new SpokExecutionException("The type are not those excepted");
        }

        return new NodeValue("boolean", result.toString(), null);
    }

    /**
     *
     */
    private void leftNodeEvaluated() {
        switch (operator) {
            case EQUALS:
            case NOT_EQUALS:
            case MORE_THAN:
            case LESS_THAN:
            case OR:
            case AND:
                if (right != null) {
                    rightNode.addEndEventListener(this);
                    rightNode.call();
                } else {
                    LOGGER.error("should evaluate the other node, but it is null");

                }
                break;
            case NOT:
                LOGGER.debug("Not node evaluated.");
                fireEndEvent(new EndEvent(this));
                break;
            default:
                throw new AssertionError(operator.name());
        }
    }

    private void rightNodeEvaluated() {
        LOGGER.debug("Everything has been evaluated");
        fireEndEvent(new EndEvent(this));
    }

    /**
     * Method to check whether the node is well formed
     *
     * @param operator
     * @return
     */
    private boolean needTwoOperands(BinaryOperator operator) {
        switch (operator) {
            case EQUALS:
            case NOT_EQUALS:
            case MORE_THAN:
            case LESS_THAN:
            case OR:
            case AND:
                return true;
            case NOT:
                return false;
            default:
                throw new AssertionError(operator.name());

        }
    }

}
