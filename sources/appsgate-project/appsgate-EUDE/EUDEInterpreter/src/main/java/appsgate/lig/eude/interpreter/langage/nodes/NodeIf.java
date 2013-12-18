package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
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
    private final NodeExpBool expBool;
    /**
     * sequence of nodes to interpret if the boolean expression is true
     */
    private final NodeSeqRules seqRulesTrue;
    /**
     * sequence of nodes to interpret if the boolean expression is false
     */
    private final NodeSeqRules seqRulesFalse;

    /**
     * Default constructor. Instantiate a node if
     *
     * @param interpreter Pointer on the interpreter
     * @param ruleIfJSON JSON representation of the node
     * @param parent
     * @throws appsgate.lig.eude.interpreter.langage.nodes.NodeException
     */
    public NodeIf(EUDEInterpreterImpl interpreter, JSONObject ruleIfJSON, Node parent) throws NodeException {
        super(interpreter, parent);

        this.expBool = new NodeExpBool(interpreter, getJSONArray(ruleIfJSON, "expBool"), this);
        this.seqRulesTrue = new NodeSeqRules(interpreter, getJSONArray(ruleIfJSON, "seqRulesTrue"), this);
        this.seqRulesFalse = new NodeSeqRules(interpreter, getJSONArray(ruleIfJSON, "seqRulesFalse"), this);

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

                if (expBool.getResult()) {// launch the "true" branch if expBool returned true...
                    seqRulesTrue.addEndEventListener(this);
                    seqRulesTrue.call();

                } else {// ... launch the false branch otherwise
                    seqRulesFalse.addEndEventListener(this);
                    seqRulesFalse.call();
                }
            } catch (Exception ex) {
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
    public Integer call() {
        fireStartEvent(new StartEvent(this));
        setStarted(true);
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
    public String toString() {
        return "[node If:" + expBool.toString() + "?" + seqRulesTrue.toString() + ":" + seqRulesFalse.toString() + "]";
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

}
