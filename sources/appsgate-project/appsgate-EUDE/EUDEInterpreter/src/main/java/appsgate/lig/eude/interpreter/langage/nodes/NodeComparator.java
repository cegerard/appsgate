/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.components.SpokParser;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeComparator extends Node implements ICanBeEvaluated {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeComparator.class);

    /**
     * Different operator that are supported by the language
     */
    public static enum BinaryComparator {

        EQUALS("=="), NOT_EQUALS("!="), MORE_THAN(">"), LESS_THAN("<");

        private final String val;

        BinaryComparator(String s) {
            this.val = s;
        }

        public String getVal() {
            return val;
        }

        public final static BinaryComparator get(String s) {
            for (BinaryComparator o : BinaryComparator.values()) {
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
    private BinaryComparator comparator = null;

    private int cptEvent = 0;

    public NodeComparator(Node p) {
        super(p);
    }

    public NodeComparator(JSONObject o, Node parent) throws SpokNodeException {
        super(parent, o);
        comparator = BinaryComparator.get(getJSONString(o, "comparator"));
        if (comparator == null) {
            LOGGER.debug("Unknown comparator: {}", getJSONString(o, "comparator"));
            throw new SpokNodeException(this, "Comparator.unknown", null);
        }
        try {
            leftNode = Builder.buildFromJSON(o.optJSONObject("leftOperand"), this);
        } catch (SpokNodeException ex) {
            LOGGER.error("Missing left operand");
            throw new SpokNodeException(this, "Comparator.leftOperand.missing", ex);
        }
        if (!(leftNode instanceof ICanBeEvaluated)) {
            LOGGER.error("Left operand does not return a value");
            throw new SpokNodeException(this, "Comparator.leftOperand.noReturn", null);
        }
        left = (ICanBeEvaluated) leftNode;
        try {
            rightNode = Builder.buildFromJSON(o.optJSONObject("rightOperand"), this);
        } catch (SpokNodeException ex) {
            LOGGER.debug("Missing right operand");
            throw new SpokNodeException(this, "Comparator.rightOperand.missing", null);
        }
        if (!(rightNode instanceof ICanBeEvaluated)) {
            LOGGER.error("right operand does not return a value");
            throw new SpokNodeException(this, "Comparator.rightOperand.noReturn", null);
        }
        right = (ICanBeEvaluated) rightNode;
        if (!left.getResultType().equalsIgnoreCase(right.getResultType())) {
            LOGGER.debug("Two types mismatch {}, {}", left.getResultType(), right.getResultType());
            throw new SpokNodeException(this, "Comparator.typeMismatch", null);
        }

    }

    @Override
    protected void specificStop() {
        leftNode.stop();
        rightNode.stop();
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        cptEvent = 0;
        leftNode.addEndEventListener(this);
        rightNode.addEndEventListener(this);
        leftNode.call();
        rightNode.call();
        return null;

    }

    @Override
    public String getExpertProgramScript() {
        return "(" + leftNode.getExpertProgramScript() + comparator.getVal() + rightNode.getExpertProgramScript() + ")";
    }

    @Override
    protected Node copy(Node parent) {
        NodeComparator ret = new NodeComparator(parent);
        ret.comparator = comparator;
        ret.leftNode = leftNode.copy(ret);
        ret.left = (ICanBeEvaluated) ret.leftNode;
        ret.rightNode = rightNode.copy(ret);
        ret.right = (ICanBeEvaluated) ret.rightNode;
        return ret;
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject ret = super.getJSONDescription();
        try {
            ret.put("type", "booleanExpression");
            ret.put("comparator", comparator);
            ret.put("leftOperand", leftNode.getJSONDescription());
            ret.put("rightOperand", rightNode.getJSONDescription());

        } catch (JSONException ex) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return ret;
    }

    @Override
    public void endEventFired(EndEvent e) {
        cptEvent++;
        if (cptEvent >= 2) {
            fireEndEvent(new EndEvent((this)));
        }

    }

    @Override
    public NodeValue getResult() {
        Boolean result = false;
        try {
            switch (comparator) {
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
                default:
                    throw new AssertionError(comparator.name());

            }

        } catch (SpokTypeException ex) {
            LOGGER.error("Unable to parse the result");
            return null;
        }

        return new NodeValue("boolean", result.toString(), null);
    }

    @Override
    public String getResultType() {
        return "boolean";
    }

    @Override
    protected void buildReferences(ReferenceTable table, HashMap<String, String> args) {
        if (leftNode != null) {
            leftNode.buildReferences(table, null);
        }
        if (rightNode != null) {
            rightNode.buildReferences(table, null);
        }
    }

    @Override
    public String getTypeSpec() {
        return "Comparator";
    }

}
