/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeLists extends Node implements INodeList, ICanBeEvaluated {

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
    private Node left;
    private INodeList leftList;
    /**
     *
     */
    private Node right;
    private INodeList rightList;

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
     * @param p the parent node
     * @param id the id of the node
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
        super(parent, o);
        op = Operator.get(getJSONString(o, "operator"));
        if (op == null) {
            LOGGER.error("Unkown operator {}", getJSONString(o, "operator"));
            throw new SpokNodeException(this, "NodeLists", "operator", null);
        }
        left = Builder.nodeOrNull(o.optJSONObject("left"), this);
        if (left == null) {
            LOGGER.error("No left list");
            throw new SpokNodeException(this, "NodeLists", "left", null);
        }
        if (!(left instanceof INodeList)) {
            LOGGER.error("left node is not a list");
            throw new SpokNodeException(this, "NodeLists", "left", null);
        }
        leftList = (INodeList) left;
        right = Builder.nodeOrNull(o.optJSONObject("right"), this);
        if (right == null) {
            LOGGER.error("no right list");
            throw new SpokNodeException(this, "NodeLists", "right", null);
        }
        if (!(right instanceof INodeList)) {
            LOGGER.error("right node is not a list");
            throw new SpokNodeException(this, "NodeLists", "right", null);
        }
        rightList = (INodeList) right;

    }

    @Override
    protected void specificStop() {
        left.stop();
        right.stop();
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        nbDone = 0;
        left.addEndEventListener(this);
        JSONObject callResult = left.call();
        if (callResult != null) {
            LOGGER.debug("unable to get a result from left wing interrupting the call");
            return callResult;
        }
        right.addEndEventListener(this);
        return right.call();
    }

    @Override
    public String getExpertProgramScript() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Node copy(Node parent) {
        NodeLists n = new NodeLists(parent);
        n.left = left.copy(n);
        n.right = right.copy(n);
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
        JSONObject o = super.getJSONDescription();
        try {
            o.put("type", "lists");
            o.put("left", left.getJSONDescription());
            o.put("right", right.getJSONDescription());
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

    @Override
    public String getResultType() {
        return this.getType();
    }

    /**
     * Method that compute the combination of the two lists depending on the
     * operator
     */
    private void computeResult() {
        switch (op) {
            case UNION:
                result = ListUtils.sum(leftList.getElements(), rightList.getElements());
                break;
            case INTERSECTION:
                result = ListUtils.intersection(leftList.getElements(), rightList.getElements());
                break;
            case NOT_IN:
                result = ListUtils.subtract(leftList.getElements(), rightList.getElements());
                break;
            default:
                LOGGER.warn("Unknown operator");
        }
    }

    @Override
    public String getTypeSpec() {
        return "lists (" + left + ") " + op + " (" + right + ")";
    }

    @Override
    public NodeValue getResult() {
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
        try {
            return new NodeValue(o, this);

        } catch (SpokNodeException ex) {
            LOGGER.error("Unable to build the NodeValue");
            return null;
        }
    }

    @Override
    public List<NodeValue> getElements() {
        return result;

    }
    @Override
    protected void buildReferences(ReferenceTable table, HashMap<String,String> args) {
        if (this.left != null) {
            left.buildReferences(table, null);
        }
        if (this.right != null) {
            right.buildReferences(table, null);
        }
    }

}
