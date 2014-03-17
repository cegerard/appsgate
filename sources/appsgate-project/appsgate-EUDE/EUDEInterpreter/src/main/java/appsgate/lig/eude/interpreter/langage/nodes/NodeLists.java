/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import java.util.List;
import static org.apache.commons.collections4.ListUtils.intersection;
import static org.apache.commons.collections4.ListUtils.subtract;
import static org.apache.commons.collections4.ListUtils.sum;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeLists extends Node {

    /**
     * The logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeLists.class.getName());

    /**
     * Different operator that are supported by the language
     */
    public static enum Operator {

        UNION("U"), INTERSECTION("T"), NOT_IN("N");

        private final String val;

        Operator(String s) {
            this.val = s;
        }

        public String getVal() {
            return val;
        }

        public final static Operator get(String s) {
            for (Operator o : Operator.values()) {
                if (o.val.equalsIgnoreCase(s)) {
                    return o;
                }
            }
            return null;
        }
    }

    /**
     *
     */
    private Node leftList;
    /**
     *
     */
    private Node rightList;

    /**
     *
     */
    private Operator op;

    /**
     *
     */
    private int nbDone;

    private List<NodeValue> result;

    /**
     * private constructor to allow copy
     *
     * @param p
     */
    private NodeLists(Node p) {
        super(p);
    }

    /**
     * Constructor
     *
     * @param o
     * @param parent
     * @throws SpokException
     */
    public NodeLists(JSONObject o, Node parent) throws SpokException {
        super(parent);
        op = Operator.get(getJSONString(o, "operator"));
        if (op == null) {
            LOGGER.error("Unkown operator {}", getJSONString(o, "operator"));
            throw new SpokNodeException("NodeLists", "operator", null);
        }
        leftList = Builder.nodeOrNull(o.optJSONObject("left"), this);
        if (leftList == null) {
            LOGGER.error("");
            throw new SpokNodeException("NodeLists", "left", null);
        }
        rightList = Builder.nodeOrNull(o.optJSONObject("right"), this);
        if (rightList == null) {
            LOGGER.error("");
            throw new SpokNodeException("NodeLists", "right", null);
        }
    }

    @Override
    protected void specificStop() {
        leftList.stop();
        rightList.stop();
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        nbDone = 0;
        leftList.addEndEventListener(this);
        JSONObject callResult = leftList.call();
        if (callResult != null) {
            LOGGER.debug("unable to get a result from left wing interrupting the call");
            return callResult;
        }
        rightList.addEndEventListener(this);
        return rightList.call();
    }

    @Override
    public String getExpertProgramScript() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Node copy(Node parent) {
        NodeLists n = new NodeLists(parent);
        n.leftList = leftList.copy(n);
        n.rightList = rightList.copy(n);
        n.op = op;
        return n;
    }

    @Override
    public void endEventFired(EndEvent e) {
        Node n = (Node) e.getSource();
        nbDone++;
        if (nbDone >= 2) {
            computeResult();
            fireEndEvent(new EndEvent(this));
        }
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", "lists");
            o.put("left", leftList.getJSONDescription());
            o.put("right", rightList.getJSONDescription());
            o.put("operator", op.getVal());
        } catch (JSONException e) {
            // Never thrown since we now the key to put
        }
        return o;
    }

    @Override
    public String getType() {
        return "list";
    }

    /**
     * Method that compute the combination of the two lists depending on the
     * operator
     */
    private void computeResult() {
        switch (op) {
            case UNION:
                result = sum(leftList.getElements(), rightList.getElements());
                break;
            case INTERSECTION:
                result = intersection(leftList.getElements(), rightList.getElements());
                break;
            case NOT_IN:
                result = subtract(leftList.getElements(), rightList.getElements());
                break;
            default:
                LOGGER.warn("Unknown operator");
        }
    }

    @Override
    public String toString() {
        return "[Node lists (" + leftList + ") " + op + " (" + rightList + ")]";
    }

    @Override
    public NodeValue getResult() throws SpokException {
        if (result == null) {
            return null;
        }
        JSONObject o = new JSONObject();
        try {
            o.put("type", "list");
            JSONArray r = new JSONArray();
            for (NodeValue v : result) {
                r.put(v.getJSONDescription());
            }
            o.put("value", r);
        } catch (JSONException ex) {
        }
        return new NodeValue(o, this);
    }

    @Override
    public List<NodeValue> getElements() {
        return result;

    }

}
