package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.references.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.components.SpokParser;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import appsgate.lig.context.dependency.graph.ReferenceDescription;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node for the if
 *
 * // <nodeIf> ::= if (expBool) then <seqAndRules> else <seqAndRules>
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since June 20, 2013
 * @version 1.0.0
 */
public class NodeIf extends Node {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeIf.class);

    /**
     * node representing the boolean expression
     */
    private Node expBool;
    /**
     * sequence of nodes to interpret if the boolean expression is true
     */
    private Node seqRulesTrue;
    /**
     * sequence of nodes to interpret if the boolean expression is false
     */
    private Node seqRulesFalse;

    /**
     * Default constructor. Instantiate a node if
     *
     * @param ruleIfJSON JSON representation of the node
     * @param parent the parent of the node
     * @throws SpokNodeException
     */
    public NodeIf(JSONObject ruleIfJSON, Node parent) throws SpokNodeException {
        super(parent, ruleIfJSON);
        try {
            this.expBool = Builder.buildFromJSON(getJSONObject(ruleIfJSON, "expBool"), this);
        } catch (SpokNodeException ex) {
            throw new SpokNodeException(this, "NodeIf.expBool", ex);
        }
        try {
            this.seqRulesTrue = Builder.buildFromJSON(getJSONObject(ruleIfJSON, "seqRulesTrue"), this);
        } catch (SpokNodeException ex) {
            throw new SpokNodeException(this, "NodeIf.seqRulesTrue", ex);
        }
        if (ruleIfJSON.has("seqRulesFalse")) {
            try {
                this.seqRulesFalse = Builder.buildFromJSON(getJSONObject(ruleIfJSON, "seqRulesFalse"), this);
            } catch (SpokNodeException ex) {
                throw new SpokNodeException(this, "NodeIf.seqRulesFalse", ex);
            }
        } else {
            LOGGER.trace("No else block for this node");
        }

    }

    /**
     * private Constructor to allow copy function
     *
     * @param parent the parent node
     */
    private NodeIf(Node parent) {
        super(parent);
    }

    /**
     * Catch the end events for the boolean expression and the branches true and
     * false Launch the appropriate branch according to the result of the
     * boolean expression Fire an EndEvent if everything is completed
     *
     * @param e EndEvent fired by the children
     */
    @Override
    public void endEventFired(EndEvent e) {
        Node nodeEnded = (Node) e.getSource();

        // the true branch or the false one has completed - nothing to do more
        if (nodeEnded == expBool) {
            evaluateResult();
        } else {
            setStarted(false);
            fireEndEvent(new EndEvent(this));
        }

    }

    /**
     * Method that call and listen a node checking if the node is not null
     *
     * @param n the a node to call
     * @return the result of call, or null
     */
    protected JSONObject listenAndCall(Node n) {
        if (n != null) {
            n.addEndEventListener(this);
            return n.call();
        } else {
            LOGGER.debug("No node to call");
            setStarted(false);
            fireEndEvent(new EndEvent(this));
            return null;
        }
    }

    /**
     * Launch the interpretation of the node, basically the evaluation of the
     * boolean expression
     *
     * @return
     */
    @Override
    public JSONObject call() {
        fireStartEvent(new StartEvent(this));
        setStarted(true);
        if (expBool instanceof NodeState) {
            expBool.call();
            evaluateResult();
            return null;
        }
        expBool.addEndEventListener(this);
        expBool.call();

        return null;
    }

    @Override
    public void specificStop() {
        expBool.removeEndEventListener(this);
        expBool.stop();
        seqRulesTrue.removeEndEventListener(this);
        seqRulesTrue.stop();
        seqRulesFalse.removeEndEventListener(this);
        seqRulesFalse.stop();
    }

    @Override
    public String getTypeSpec() {
        if (seqRulesFalse != null) {
            return "[node If:" + expBool.toString() + " THEN " + seqRulesTrue.toString() + " ELSE " + seqRulesFalse.toString() + "]";
        }
        return "[node If:" + expBool.toString() + " THEN " + seqRulesTrue.toString() + "]";
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
        try {
            o.put("type", "if");
            o.put("expBool", expBool.getJSONDescription());
            o.put("seqRulesTrue", seqRulesTrue.getJSONDescription());
            o.put("seqRulesFalse", seqRulesTrue.getJSONDescription());
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;

    }

    @Override
    public String getExpertProgramScript() {
        if (seqRulesFalse != null) {
            return "if " + expBool.getExpertProgramScript() + "\nthen " + seqRulesTrue.getExpertProgramScript() + "\n else " + seqRulesFalse.getExpertProgramScript() + "\n";
        } else {
            return "if " + expBool.getExpertProgramScript() + "\nthen " + seqRulesTrue.getExpertProgramScript() + "\n";
        }
    }

    @Override
    protected Node copy(Node parent) {
        NodeIf ret = new NodeIf(parent);
        ret.expBool = expBool.copy(ret);
        ret.seqRulesFalse = seqRulesFalse.copy(ret);
        ret.seqRulesTrue = seqRulesTrue.copy(ret);
        return ret;

    }

    /**
     * Method being called once the boolean expression is evaluated
     */
    private void evaluateResult() {
        Boolean booleanResult;
        try {
            if (expBool instanceof ICanBeEvaluated) {
                booleanResult = SpokParser.getBooleanResult(((ICanBeEvaluated) expBool).getResult());
            } else {
                LOGGER.error("Boolean expression can not be evaluated: {}", this.expBool);
                setStarted(false);
                fireEndEvent(new EndEvent(this));
                return;
            }
        } catch (SpokTypeException ex) {
            LOGGER.error("An exception has been raised during evaluation of node {}", this.expBool);
            LOGGER.debug(ex.getValue());
            setStarted(false);
            fireEndEvent(new EndEvent(this));
            return;

        }
        // if this is the boolean expression...
        if (booleanResult) {// launch the "true" branch if expBool returned true...
            listenAndCall(seqRulesTrue);
        } else {
            listenAndCall(seqRulesFalse);
        }
    }
    
    @Override
    protected void buildReferences(ReferenceTable table, ReferenceDescription d) {
        if (this.expBool != null) {
            expBool.buildReferences(table, null);
        }
        if (this.seqRulesTrue != null) {
            seqRulesTrue.buildReferences(table, null);
        }
        if (this.seqRulesFalse != null) {
            seqRulesFalse.buildReferences(table, null);
        }
    }

}
