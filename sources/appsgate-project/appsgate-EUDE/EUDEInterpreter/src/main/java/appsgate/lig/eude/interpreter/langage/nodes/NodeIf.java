package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokParser;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeIf.class.getName());

    /**
     * node representing the boolean expression
     */
//    private NodeExpBool expBool
    private Node expBool;
    /**
     * sequence of nodes to interpret if the boolean expression is true
     */
    private NodeSeqRules seqRulesTrue;
    /**
     * sequence of nodes to interpret if the boolean expression is false
     */
    private NodeSeqRules seqRulesFalse;

    /**
     * Default constructor. Instantiate a node if
     *
     * @param ruleIfJSON JSON representation of the node
     * @param parent
     * @throws SpokNodeException
     */
    public NodeIf(JSONObject ruleIfJSON, Node parent) throws SpokException {
        super(parent);

//        this.expBool = new NodeExpBool(getJSONArray(ruleIfJSON, "expBool"), this);
        this.expBool = NodeBuilder.BuildNodeFromJSON(ruleIfJSON.optJSONObject("expBool"), this);
        this.seqRulesTrue = new NodeSeqRules(getJSONArray(ruleIfJSON, "seqRulesTrue"), this);
        this.seqRulesFalse = new NodeSeqRules(getJSONArray(ruleIfJSON, "seqRulesFalse"), this);

    }

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

        // if this is the boolean expression...
        if (nodeEnded == expBool) {
            try {

                if (SpokParser.getBooleanResult(expBool.getResult())) {// launch the "true" branch if expBool returned true...
                    seqRulesTrue.addEndEventListener(this);
                    seqRulesTrue.call();

                } else {// ... launch the false branch otherwise
                    seqRulesFalse.addEndEventListener(this);
                    seqRulesFalse.call();
                }
            } catch (SpokException ex) {
                LOGGER.error(ex.getMessage());
            }
            // the true branch or the false one has completed - nothing to do more
        } else {
            setStarted(false);
            fireEndEvent(new EndEvent(this));
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
        expBool.addEndEventListener(this);
        expBool.call();

        return null;
    }

    @Override
    public void specificStop() throws SpokException {
        expBool.removeEndEventListener(this);
        expBool.stop();
        seqRulesTrue.removeEndEventListener(this);
        seqRulesTrue.stop();
        seqRulesFalse.removeEndEventListener(this);
        seqRulesFalse.stop();
    }

    @Override
    public String toString() {
        return "[node If:" + expBool.toString() + "?" + seqRulesTrue.toString() + ":" + seqRulesFalse.toString() + "]";
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("expBool", expBool.getJSONDescription());
            o.put("seqRulesTrue", seqRulesTrue.getJSONArrayDescription());
            o.put("seqRulesFalse", seqRulesTrue.getJSONArrayDescription());
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;

    }

    @Override
    public String getExpertProgramScript() {
        return "if " + expBool.getExpertProgramScript() + "\nthen " + seqRulesTrue.getExpertProgramScript() + "\n else " + seqRulesFalse.getExpertProgramScript() + "\n";
    }

    @Override
    protected void collectVariables(SymbolTable s) {
        expBool.collectVariables(s);
        seqRulesTrue.collectVariables(s);
        seqRulesFalse.collectVariables(s);
    }

    @Override
    protected Node copy(Node parent) {
        NodeIf ret = new NodeIf(parent);
        ret.setSymbolTable(this.getSymbolTable());
        ret.expBool = expBool.copy(ret);
        ret.seqRulesFalse = (NodeSeqRules) seqRulesFalse.copy(ret);
        ret.seqRulesTrue = (NodeSeqRules) seqRulesTrue.copy(ret);
        return ret;

    }

}
