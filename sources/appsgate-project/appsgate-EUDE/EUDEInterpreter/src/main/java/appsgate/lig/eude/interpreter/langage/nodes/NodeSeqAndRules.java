package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
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
     * @throws NodeException
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
    public void stop() {
        if (isStarted()) {
            setStopping(true);
            for (Node n : rules) {
                n.stop();
                n.removeEndEventListener(this);
            }
            setStarted(false);
            setStopping(false);
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
        String array = "";
        for (Node seq : rules) {
            array += seq.toString() + "\n";
        }

        return "[Node SeqAndRules: [" + array + "]]";
    }

}
