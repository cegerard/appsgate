package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a sequence of rules to evaluate
 *
 * // <seqRules> ::= <seqAndRules> { <opThenRule> <seqAndRules> }
 *
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
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSeqRules.class.getName());

    /**
     * Contains the block of rules separated by a "THEN" operator
     */
    private ArrayList<NodeSeqAndRules> seqAndRules;

    /**
     *
     */
    private int idCurrentSeqAndRules;

    /**
     * private Constructor to copy Nodes
     *
     * @param p
     */
    private NodeSeqRules(Node p) {
        super(p);
    }

    /**
     * Initialize the sequence of rules from a JSON tree
     *
     * @param seqRulesJSON JSON array containing the rules
     * @param parent
     * @throws NodeException
     */
    public NodeSeqRules(JSONArray seqRulesJSON, Node parent) throws NodeException {
        super(parent);

        seqAndRules = new ArrayList<NodeSeqAndRules>();

        for (int i = 0; i < seqRulesJSON.length(); i++) {
            JSONArray seqAndRulesJSON;
            try {
                seqAndRulesJSON = seqRulesJSON.getJSONArray(i);
            } catch (JSONException ex) {
                throw new NodeException("NodeSeqRules", "item " + i, ex);
            }
            if (seqAndRulesJSON.length() > 0) {
                seqAndRules.add(new NodeSeqAndRules(seqAndRulesJSON, this));
            }
        }

    }

    /**
     * Method that launch the next sequence
     */
    private void launchNextSeqAndRules() {
        NodeSeqAndRules seqAndRule;

        synchronized (this) {
            // get the next sequence of rules to launch
            seqAndRule = seqAndRules.get(idCurrentSeqAndRules);
        }

        if (!isStopping()) {
            // launch the sequence of rules
            seqAndRule.addEndEventListener(this);
            seqAndRule.call();
        }
    }

    @Override
    public Integer call() {
        idCurrentSeqAndRules = 0;
        setStarted(true);
        fireStartEvent(new StartEvent(this));

        if (!seqAndRules.isEmpty()) {
            launchNextSeqAndRules();
        } else {
            LOGGER.warn("Trying to call a seq rule on an empty sequence");
            setStarted(false);
            fireEndEvent(new EndEvent(this));
        }

        return null;
    }

    @Override
    public void endEventFired(EndEvent e) {
        //    ((Node) e.getSource()).removeEndEventListener(this);
        idCurrentSeqAndRules++;

        if (idCurrentSeqAndRules < seqAndRules.size()) {
            LOGGER.trace("###### launching the next sequence of rules...");
            try {
                launchNextSeqAndRules();
            } catch (Exception ex) {
                LOGGER.error("Exception caught: {}", ex.getMessage());
            }
        } else {
            LOGGER.debug("###### SeqThenRules ended...");
            setStarted(false);
            fireEndEvent(new EndEvent(this));
        }
    }

    @Override
    public void specificStop() throws SpokException {
        for (Node n : seqAndRules) {
            n.removeEndEventListener(this);
        }
        synchronized (this) {
            if (seqAndRules.size() > 0) {
                NodeSeqAndRules seqAndRule = seqAndRules.get(idCurrentSeqAndRules);
                setStopping(true);
                seqAndRule.stop();
            }
        }
    }

    @Override
    public String toString() {

        return "[Node SeqRules: [" + seqAndRules.size() + "]]";
    }

    /**
     *
     * @return
     */
    @Override
    public String getExpertProgramScript() {
        if (seqAndRules.size() == 1) {
            return seqAndRules.get(0).getExpertProgramScript();
        }

        String ret = "[";
        for (NodeSeqAndRules s : seqAndRules) {
            ret += s.getExpertProgramScript() + ",";
        }
        return ret.substring(0, ret.length() - 1) + "]";
    }

    @Override
    protected void collectVariables(SymbolTable s) {
        for (NodeSeqAndRules seq : seqAndRules) {
            seq.collectVariables(s);
        }
    }

    void initSymbolTableFromParams(JSONArray params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    Node copy(Node parent) {
        NodeSeqRules ret = new NodeSeqRules(parent);
        ret.seqAndRules = new ArrayList<NodeSeqAndRules>();
        for (NodeSeqAndRules n : seqAndRules) {
            ret.seqAndRules.add((NodeSeqAndRules) n.copy(ret));
        }

        ret.setSymbolTable(this.getSymbolTable());
        return ret;

    }

}
