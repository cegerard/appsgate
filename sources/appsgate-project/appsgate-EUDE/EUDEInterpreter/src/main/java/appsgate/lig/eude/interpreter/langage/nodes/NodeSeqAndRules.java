package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since May 22, 2013
 * @version 1.0.0
 *
 */
public class NodeSeqAndRules extends Node {

    //Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSeqAndRules.class.getName());

    /**
     * the rules to apply
     */
    private final ArrayList<Node> rules;
    /**
     * the number of rules that has been ended
     */
    private int nbRulesEnded;

    /**
     * Constructor
     *
     * @param interpreter
     * @param seqAndRulesJSON
     * @param parent
     * @throws NodeException
     */
    public NodeSeqAndRules(EUDEInterpreterImpl interpreter, JSONArray seqAndRulesJSON, Node parent) throws NodeException {
        super(interpreter, parent);

        rules = new ArrayList<Node>();

        for (int i = 0; i < seqAndRulesJSON.length(); i++) {
            JSONObject ruleJSON;
            try {
                ruleJSON = seqAndRulesJSON.getJSONObject(i);
            } catch (JSONException ex) {
                throw new NodeException("NodeSeqAndRules", "item " + i, ex);
            }
            String nodeType = getJSONString(ruleJSON, "type");
            if (nodeType.equals("NodeAction")) {
                rules.add(new NodeAction(interpreter, ruleJSON, this));
            } else if (nodeType.equals("NodeIf")) {
                rules.add(new NodeIf(interpreter, ruleJSON, this));
            } else if (nodeType.equals("NodeWhen")) {
                rules.add(new NodeWhen(interpreter, ruleJSON, this));
            } else if (nodeType.equals("seqRules")) {
                rules.add(new NodeSeqRules(interpreter, getJSONArray(ruleJSON, "rule"), this));
            } else {
                LOGGER.warn("The type [{}] is not supported by the parser", nodeType);
            }

        }
    }

    @Override
    public Integer call() {
        // no rules are done
        nbRulesEnded = 0;
        setStarted(true);
        for (Node n : rules) {
            n.addEndEventListener(this);
            n.call();
            if (isStopping()) {
                break;
            }
        }
        return null;
    }

    @Override
    public void specificStop() {
        for (Node n : rules) {
            n.stop();
            n.removeEndEventListener(this);
        }
    }

    @Override
    public void endEventFired(EndEvent e) {
        nbRulesEnded++;
        // if all the rules are terminated, fire the end event
        if (nbRulesEnded == rules.size()) {
            setStarted(false);
            fireEndEvent(new EndEvent(this));
        }
    }

    @Override
    public String toString() {

        return "[Node SeqAndRules: [" + rules.size() + "]]";
    }

    @Override
    public String getExpertProgramScript() {
        String ret = "";
        for (Node n : rules) {
            ret += n.getExpertProgramScript() + "\n";
        }
        return ret.substring(0, ret.length() - 1);
    }

    @Override
    protected void collectVariables(SymbolTable s) {
        for (Node n : rules) {
            n.collectVariables(s);
        }
    }

}
