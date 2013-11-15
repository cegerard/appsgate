package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;

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

    private final ArrayList<Node> rules;
    private int nbRulesEnded;

    /**
     * Constructor
     *
     * @param interpreter
     * @param seqAndRulesJSON
     * @throws appsgate.lig.eude.interpreter.langage.nodes.NodeException
     */
    public NodeSeqAndRules(EUDEInterpreterImpl interpreter, JSONArray seqAndRulesJSON) throws NodeException {
        super(interpreter);

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
                rules.add(new NodeAction(interpreter, ruleJSON));
            } else if (nodeType.equals("NodeIf")) {
                rules.add(new NodeIf(interpreter, ruleJSON));
            } else if (nodeType.equals("NodeWhen")) {
                rules.add(new NodeWhen(interpreter, ruleJSON));
            } else if (nodeType.equals("seqRules")) {
                rules.add(new NodeSeqRules(interpreter, getJSONArray(ruleJSON, "rule")));
            }

        }
    }

    @Override
    public Integer call() {
        // no rules are done
        nbRulesEnded = 0;
        started = true;
        for (Node n : rules) {
            n.addEndEventListener(this);
            n.call();
            if (stopping) {
                break;
            }
        }
        return null;
    }

    @Override
    public void stop() {
        if (started) {
            stopping = true;
            for (Node n : rules) {
                n.stop();
                n.removeEndEventListener(this);
            }
            started = false;
            stopping = false;
        }
    }

    @Override
    public void endEventFired(EndEvent e
    ) {
        ((Node) e.getSource()).removeEndEventListener(this);
        nbRulesEnded++;

        // if all the rules are terminated, fire the end event
        if (nbRulesEnded == rules.size()) {
            started = false;
            fireEndEvent(new EndEvent(this));
        }
    }

}
