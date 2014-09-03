/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
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
public class NodeBooleanExpression extends Node implements ICanBeEvaluated {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeBooleanExpression.class);


    /**
     * Different operator that are supported by the language
     */
    public static enum BinaryOperator {

        OR("||"), AND("&&"), NOT("!"), TRUE("");

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
    private ICanBeEvaluated left = null;
    private Node leftNode;

    /**
     * The right node to evaluate
     */
    private ICanBeEvaluated right = null;
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
    private NodeBooleanExpression(Node p) {
        super(p);
    }

    /**
     *
     * @param o
     * @param parent
     * @throws SpokNodeException
     */
    public NodeBooleanExpression(JSONObject o, Node parent) throws SpokNodeException {
        super(parent, o);
        operator = BinaryOperator.get(getJSONString(o, "operator"));
        if (operator == null) {
            LOGGER.debug("Unknown operator: {}", getJSONString(o, "operator"));
            throw new SpokNodeException("BooleanExpression", "operator", null);
        }
        try {
            leftNode = Builder.buildFromJSON(o.optJSONObject("leftOperand"), this);
            if (!(leftNode instanceof ICanBeEvaluated)) {
                LOGGER.error("Left operand does not return a value");
                throw new SpokNodeException("BooleanExpression", "leftOperand", null);
            }
            left = (ICanBeEvaluated) leftNode;
        } catch (SpokTypeException ex) {
            LOGGER.error("Missing left operand");
            throw new SpokNodeException("BooleanExpression", "leftOperand", ex);
        }
        try {
            rightNode = Builder.buildFromJSON(o.optJSONObject("rightOperand"), this);
            if (!(rightNode instanceof ICanBeEvaluated)) {
                LOGGER.error("right operand does not return a value");
                throw new SpokNodeException("BooleanExpression", "rightOperand", null);
            }
            right = (ICanBeEvaluated) rightNode;
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
        evaluateNode(leftNode);
        return null;
    }

    @Override
    public String getExpertProgramScript() {
        if (rightNode != null) {
            return "(" + leftNode.getExpertProgramScript() + operator.getVal() + rightNode.getExpertProgramScript() + ")";
        } else {
            return "(" + operator.getVal() + leftNode.getExpertProgramScript() + ")";
        }
    }

    @Override
    protected Node copy(Node parent) {
        NodeBooleanExpression ret = new NodeBooleanExpression(parent);
        ret.operator = operator;
        ret.leftNode = leftNode.copy(ret);
        ret.left = (ICanBeEvaluated) ret.leftNode;
        if (rightNode != null) {
            ret.rightNode = rightNode.copy(ret);
            ret.right = (ICanBeEvaluated) ret.rightNode;
        } else {
            ret.rightNode = null;
        }
        return ret;
    }

    @Override
    public void endEventFired(EndEvent e) {
        nodeEvaluated((Node) e.getSource());

    }

    /**
     *
     */
    private void nodeEvaluated(Node n) {
        if (n == right) {
            fireEndEvent(new EndEvent(this));
            return;
        }
        switch (operator) {
            case NOT:
            case TRUE:
                fireEndEvent(new EndEvent(this));
                break;
            case OR:
            case AND:
                if (right != null) {
                    evaluateNode(rightNode);
                } else {
                    LOGGER.error("should evaluate the other node, but it is null");
                }
                break;
            default:
                throw new AssertionError(operator.name());
        }
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject ret = super.getJSONDescription();
        try {
            ret.put("type", "booleanExpression");
            ret.put("operator", operator);
            ret.put("leftOperand", leftNode.getJSONDescription());
            if (rightNode != null) {
                ret.put("rightOperand", rightNode.getJSONDescription());
            }

        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return ret;
    }

    @Override
    public NodeValue getResult() {
        Boolean result = false;
        try {
            switch (operator) {
                case OR:
                    result = SpokParser.getBooleanResult(left.getResult()) || SpokParser.getBooleanResult(right.getResult());
                    break;
                case AND:
                    result = SpokParser.getBooleanResult(left.getResult()) && SpokParser.getBooleanResult(right.getResult());
                    break;
                case NOT:
                    result = !SpokParser.getBooleanResult(left.getResult());
                    break;
                case TRUE:
                    result = SpokParser.getBooleanResult(left.getResult());
                    break;
                default:
                    throw new AssertionError(operator.name());

            }

        } catch (SpokTypeException ex) {
            LOGGER.error("Unable to parse the result");
            return null;
        }

        return new NodeValue("boolean", result.toString(), null);
    }

    /**
     *
     * @param node
     */
    private void evaluateNode(Node node) {
        if (node instanceof NodeState) {
            node.call();
            nodeEvaluated(node);
        } else {
            node.call();
            node.addEndEventListener(this);
        }
    }

    /**
     * Method to check whether the node is well formed
     *
     * @param operator
     * @return
     */
    private boolean needTwoOperands(BinaryOperator operator) {
        switch (operator) {
            case OR:
            case AND:
                return true;
            case NOT:
            case TRUE:
                return false;
            default:
                throw new AssertionError(operator.name());

        }
    }

    @Override
    public String getResultType() {
        return "boolean";
    }
    
    @Override
    protected void buildReferences(ReferenceTable table) {
        if (leftNode != null) {
            leftNode.buildReferences(table);
        }
        if (rightNode != null) {
            rightNode.buildReferences(table);
        }
    }

}
