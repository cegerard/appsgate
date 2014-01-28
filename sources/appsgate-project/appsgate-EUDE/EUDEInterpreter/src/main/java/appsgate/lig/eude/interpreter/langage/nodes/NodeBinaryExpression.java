/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokObject;
import appsgate.lig.eude.interpreter.langage.components.SpokParser;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeBinaryExpression extends Node {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);

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
    }

    /**
     * The left node to evaluate
     */
    private Node left = null;
    /**
     * The right node to evaluate
     */
    private Node right = null;
    /**
     * the operator of the expression
     */
    private BinaryOperator operator = null;

    /**
     * private constructor to allow the copy function
     *
     * @param p
     */
    private NodeBinaryExpression(Node p) {
        super(p);
    }

    /**
     *
     * @param o
     * @param parent
     * @throws SpokNodeException
     */
    public NodeBinaryExpression(JSONObject o, Node parent) throws SpokException {
        super(parent);
        operator = BinaryOperator.valueOf(getJSONString(o, "operator"));
        if (o.has("leftOperand")) {
            left = Builder.BuildNodeFromJSON(o.optJSONObject("leftOperand"), this);
        } else {
            throw new SpokNodeException("BinaryExpression", "leftOperand", null);
        }
        if (o.has("rightOperand")) {
            right = Builder.BuildNodeFromJSON(o.optJSONObject("rightOperand"), this);
        } else {
            if (needTwoOperands(operator)) {
                throw new SpokNodeException("BinaryExpression", "rightOperand", null);
            }
        }

    }

    @Override
    protected void specificStop() {
        if (left != null) {
            left.stop();
        }
        if (right != null) {
            right.stop();
        }
    }

    @Override
    public JSONObject call() {
        if (left != null) {
            left.call();
            left.addEndEventListener(this);
        }
        return null;
    }

    @Override
    public String getExpertProgramScript() {
        return "(" + left.getExpertProgramScript() + operator.getVal() + right.getExpertProgramScript() + ")";
    }

    @Override
    protected Node copy(Node parent) {
        NodeBinaryExpression ret = new NodeBinaryExpression(parent);
        ret.operator = operator;
        ret.left = left.copy(ret);
        ret.right = right.copy(ret);
        return ret;
    }

    @Override
    public void endEventFired(EndEvent e) {
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
        JSONObject ret = new JSONObject();
        try {
            ret.put("type", "binaryExpression");
            ret.put("operator", operator);
            ret.put("leftOperand", left.getJSONDescription());
            ret.put("rightOperand", left.getJSONDescription());

        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return ret;
    }

    @Override
    public SpokObject getResult() throws SpokException {
        Boolean result = false;
        switch (operator) {
            case EQUALS:
                result = SpokParser.equals(left.getResult(), right.getResult());
                break;
            case NOT_EQUALS:
                result = ! SpokParser.equals(left.getResult(), right.getResult());
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
                    right.addEndEventListener(this);
                    right.call();
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
