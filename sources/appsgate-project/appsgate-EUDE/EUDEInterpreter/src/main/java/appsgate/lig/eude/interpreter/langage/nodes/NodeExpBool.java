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
 * Node for the boolean expression // <expBool> ::= <seqAndBool> { <opOrBool>
 * <seqAndBool> }
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since June 20, 2013
 * @version 1.0.0
 */
public class NodeExpBool extends Node {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeExpBool.class.getName());

    /**
     * List of boolean sequences. Nodes of the list are separated by a boolean
     * "or" to manage priority of the boolean "and"
     */
    private ArrayList<NodeSeqAndBool> listSeqAndBool;
    /**
     * Track the number of sequences done
     */
    private int nbSeqAndBoolDone;
    /**
     * Final value of the boolean expression
     */
    private Boolean result = null;

    /**
     * Default constructor
     *
     * @param expBoolJSON JSON representation of the node
     * @param parent
     * @throws NodeException
     */
    public NodeExpBool(JSONArray expBoolJSON, Node parent) throws NodeException {
        super(parent);

        // instantiate each sequence and store it in the list
        listSeqAndBool = new ArrayList<NodeSeqAndBool>();
        for (int i = 0; i < expBoolJSON.length(); i++) {
            try {
                listSeqAndBool.add(new NodeSeqAndBool(expBoolJSON.getJSONArray(i), this));
            } catch (JSONException ex) {
                throw new NodeException("NodeExpBool", "item " + i, ex);
            }
        }

        // the result has not been computed yet
        result = null;

    }

    /**
     * private Constructor to copy
     * @param interpreter
     * @param parent 
     */
    private NodeExpBool(Node parent) {
        super(parent);
    }
    

    @Override
    public void specificStop() throws SpokException{
        for (NodeSeqAndBool n : listSeqAndBool) {
            n.removeEndEventListener(this);
            n.stop();
        }
    }

    /**
     * Called when the interpretation of a sequence is done. Check if all the
     * sequences of "and" boolean are done. If so, compute the final result and
     * fire the end event
     *
     * @param e
     */
    @Override
    public void endEventFired(EndEvent e) {
        nbSeqAndBoolDone++;

        // if all the sequences are done, compute the final result and fire the end event
        if (nbSeqAndBoolDone == listSeqAndBool.size()) {
            result = false;
            for (NodeSeqAndBool n : listSeqAndBool) {
                try {
                    result = result || n.getResult();
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
            setStarted(false);

            // fire the end event
            fireEndEvent(new EndEvent(this));
        }
    }

    /**
     * Launch the interpretation of the node. Interpreter simultaneously all the
     * sequences of "and"
     *
     * @return
     */
    @Override
    public Integer call() {
        // fire the start event
        fireStartEvent(new StartEvent(this));
        setStarted(true);
        // initialize the attributes to control the evaluation
        nbSeqAndBoolDone = 0;
        result = null;

        // add listener to the end event of the nodes
        for (NodeSeqAndBool n : listSeqAndBool) {
            n.addEndEventListener(this);
            n.call();
        }

        return null;
    }

    /**
     *
     * @return the result
     * @throws Exception if the result is null
     */
    public Boolean getResult() throws Exception {
        // throw an exception if the result has not already been computed
        if (result == null) {
            throw new Exception("Result is not computed yet");
        }

        return result;
    }

    @Override
    public String toString() {
        return "[Node ExpBool: [" + listSeqAndBool.size() + "]]";
    }

    @Override
    public String getExpertProgramScript() {
        String ret = "";
        for (NodeSeqAndBool n : listSeqAndBool) {
            ret += n.getExpertProgramScript() + "\n";
        }
        return ret;

    }

    @Override
    protected void collectVariables(SymbolTable s) {
        for (NodeSeqAndBool n : listSeqAndBool) {
            n.collectVariables(s);
        }
    }

    @Override
    Node copy(Node parent) {
        NodeExpBool ret = new NodeExpBool(parent);
        ret.setSymbolTable(this.getSymbolTable());
        ret.result = result;
        ret.listSeqAndBool = new ArrayList<NodeSeqAndBool>();
        for (NodeSeqAndBool n:listSeqAndBool) {
            ret.listSeqAndBool.add((NodeSeqAndBool) n.copy(ret));
        }
        return ret;

    }

}
