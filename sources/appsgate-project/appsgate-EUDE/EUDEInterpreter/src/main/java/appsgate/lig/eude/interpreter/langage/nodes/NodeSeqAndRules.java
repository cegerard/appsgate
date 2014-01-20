package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
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
    private ArrayList<Node> rules;
    /**
     * the number of rules that has been ended
     */
    private int nbRulesEnded;

    /**
     * private constructor to copy nodes
     *
     * @param interpreter
     * @param p
     */
    private NodeSeqAndRules(Node p) {
        super(p);
    }

    /**
     * Constructor
     *
     * @param seqAndRulesJSON
     * @param parent
     * @throws SpokNodeException
     */
    public NodeSeqAndRules(JSONArray seqAndRulesJSON, Node parent) throws SpokNodeException {
        super(parent);

        rules = new ArrayList<Node>();

        for (int i = 0; i < seqAndRulesJSON.length(); i++) {
            JSONObject ruleJSON;
            try {
                ruleJSON = seqAndRulesJSON.getJSONObject(i);
            } catch (JSONException ex) {
              throw new SpokNodeException("NodeSeqAndRules", "item " + i, ex);
            }
            String nodeType = getJSONString(ruleJSON, "type");
            if (nodeType.equals("NodeAction")) {
                rules.add(new NodeAction(ruleJSON, this));
            } else if (nodeType.equals("NodeIf")) {
                rules.add(new NodeIf(ruleJSON, this));
            } else if (nodeType.equals("NodeWhen")) {
                rules.add(new NodeWhen(ruleJSON, this));
            } else if (nodeType.equals("seqRules")) {
                rules.add(new NodeSeqRules(getJSONArray(ruleJSON, "rule"), this));
            } else if (nodeType.equals("assignation")) {
                rules.add(new NodeVariableAssignation(ruleJSON, this));
            } else if (nodeType.equals("NodeReturn")) {
                rules.add(new NodeReturn(ruleJSON, this));
            } else if (nodeType.equals("functionCall")) {
                rules.add(new NodeFunction(ruleJSON, this));
            } else {
                LOGGER.warn("The type [{}] is not supported by the parser", nodeType);
            }

        }
    }

    @Override
    public JSONObject call() {
        // no rules are done
        nbRulesEnded = 0;
        setStarted(true);
        for (Node n : rules) {
            n.addEndEventListener(this);
            try {
                n.call();
            } catch (SpokException ex) {
                LOGGER.error("An exception has been raised: " + ex);
                break;
            }
            if (isStopping()) {
                break;
            }
        }
        return null;
    }

    @Override
    public void specificStop() throws SpokException {
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
    JSONObject getJSONDescription() {
        throw new UnsupportedOperationException("Should use the getJSONArrayDescription instead.");
    }

    public JSONArray getJSONArrayDescription() {
            JSONArray a = new JSONArray();
        int i = 0;
        for (Node n : this.rules) {
            try {
                a.put(i, n.getJSONDescription());
                i++;
            } catch (JSONException ex) {
            }
        }
        return a;
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

    @Override
    Node copy(Node parent) {
        NodeSeqAndRules ret = new NodeSeqAndRules(parent);
        ret.rules = new ArrayList<Node>();
        for (Node n : rules) {
            ret.rules.add(n.copy(ret));
        }
        ret.setSymbolTable(this.getSymbolTable());
        return ret;
    }

}
