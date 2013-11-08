package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a sequence of rules to evaluate
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since May 22, 2013
 * @version 1.0.0
 *
 */
public class NodeSeqRules extends Node {

    // Logger
    private static final Logger LOGGER = Logger.getLogger(NodeSeqRules.class.getName());

	// <seqRules> ::= <seqAndRules> { <opThenRule> <seqAndRules> }
    /**
     * Contains the block of rules separated by a "THEN" operator
     */
    private final ArrayList<NodeSeqAndRules> seqAndRules;
    /**
     *
     */
    private int idCurrentSeqAndRules;

    /**
     * Initialize the sequence of rules from a JSON tree
     *
     * @param interpreter
     * @param seqRulesJSON JSON array containing the rules
     * @throws org.json.JSONException
     */
    public NodeSeqRules(EUDEInterpreterImpl interpreter, JSONArray seqRulesJSON) throws JSONException {
        super(interpreter);

        seqAndRules = new ArrayList<NodeSeqAndRules>();

        for (int i = 0; i < seqRulesJSON.length(); i++) {
            JSONArray seqAndRulesJSON = seqRulesJSON.getJSONArray(i);
            if (seqAndRulesJSON.length() > 0) {
                seqAndRules.add(new NodeSeqAndRules(interpreter, seqAndRulesJSON));
            }
        }

        LOGGER.log(Level.FINEST, "###### nb SeqAndRules: {0}", seqAndRules.size());

    }

    private void launchNextSeqAndRules() throws Exception {
        NodeSeqAndRules seqAndRule;

        synchronized (this) {
            // get the next sequence of rules to launch
            seqAndRule = seqAndRules.get(idCurrentSeqAndRules);
        }

        if (!stopping) {
            // launch the sequence of rules
            seqAndRule.addEndEventListener(this);
            //pool.submit(seqAndRule);
            seqAndRule.call();
            // manage the interpretation
            // super.call();
        }
    }

    @Override
    public Integer call() throws Exception {
        idCurrentSeqAndRules = 0;
        started = true;
        fireStartEvent(new StartEvent(this));

        if (!seqAndRules.isEmpty()) {
            launchNextSeqAndRules();
        } else {
            started = false;
            fireEndEvent(new EndEvent(this));
        }

        return null;
    }

    @Override
    public void endEventFired(EndEvent e) {
        ((Node) e.getSource()).removeEndEventListener(this);
        idCurrentSeqAndRules++;

        if (idCurrentSeqAndRules < seqAndRules.size()) {
            LOGGER.finest("###### launching the next sequence of rules...");
            try {
                launchNextSeqAndRules();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Exception caught: {0}", ex.getMessage());
            }
        } else {
            LOGGER.finest("###### SeqThenRules ended...");
            started = false;
            fireEndEvent(new EndEvent(this));
        }
    }

    @Override
    public void stop() {
        if (started) {
            synchronized (this) {
                if (seqAndRules.size() > 0) {
                    NodeSeqAndRules seqAndRule = seqAndRules.get(idCurrentSeqAndRules);
                    stopping = true;
                    seqAndRule.stop();
                }
            }
            started = false;
            stopping = false;
        }
    }

}
