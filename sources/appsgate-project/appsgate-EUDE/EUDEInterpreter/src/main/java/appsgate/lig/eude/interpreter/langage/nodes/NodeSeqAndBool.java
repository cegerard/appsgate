package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node for the sequence of boolean relations separated by a "and" //
 * <seqAndBool> ::= <relationBool> { <opAndBool> <relationBool> }
 *
 * @author Rémy Dautriche
 * @since June 21, 2013
 * @version 1.0.0
 */
public class NodeSeqAndBool extends Node {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSeqAndBool.class.getName());

    /**
     * List of the boolean relations
     */
    private ArrayList<NodeRelationBool> relationsBool;
    /**
     * Track the number of relation done
     */
    private int nbRelationBoolEnded = 0;
    /**
     * Final result of the sequence
     */
    private Boolean result = null;

    /**
     * Default construct
     *
     * @param seqAndBoolJSON JSON representation of the node
     * @param parent
     * @throws SpokNodeException
     */
    public NodeSeqAndBool(JSONArray seqAndBoolJSON, Node parent)
            throws SpokNodeException {
        super(parent);

        // instantiate each boolean relation and store in the list
        relationsBool = new ArrayList<NodeRelationBool>();
        for (int i = 0; i < seqAndBoolJSON.length(); i++) {
            JSONObject relBoolJSON = null;
            try {
                relBoolJSON = seqAndBoolJSON.getJSONObject(i);
            } catch (JSONException ex) {
                throw new SpokNodeException("NodeSeqAndBool", "item " + i, ex);
            }
            relationsBool.add(new NodeRelationBool(relBoolJSON, this));
            if (isStopping()) {
                break;
            }
        }

    }

    /**
     * private constructor to copy
     *
     * @param interpreter
     * @param parent
     */
    private NodeSeqAndBool(Node parent) {
        super(parent);
    }

    @Override
    public void specificStop() throws SpokException {
        for (NodeRelationBool nr : relationsBool) {
            nr.stop();
        }
    }

    /**
     * Launch the interpretation of the node. Interpret simultaneously all the
     * boolean relations
     *
     * @return null
     */
    @Override
    public JSONObject call() {
        // fire the start event
        fireStartEvent(new StartEvent(this));
        setStarted(true);

        // initialize the attribute to control the interpretation
        result = null;
        nbRelationBoolEnded = 0;

        // add listener to the end of each node
        for (NodeRelationBool n : relationsBool) {
            n.addEndEventListener(this);
            n.call();
        }

        return null;
    }

    /**
     * Compute the final result. Basically, perform a boolean "and" with all the
     * results of the relations
     */
    private Boolean computeResult() {
        Boolean r = true;
        for (NodeRelationBool n : relationsBool) {
            try {
                r = r && n.getBooleanResult();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
                return false;
            }
        }
        return r;
    }

    /**
     * Called when the interpretation of a boolean relation is done. Check if
     * all the relations are done. If so, compute the final result and fire the
     * end event
     *
     * @param e
     */
    @Override
    public void endEventFired(EndEvent e) {
        nbRelationBoolEnded++;

        // if all the relations are done, compute the final result
        if (nbRelationBoolEnded == relationsBool.size()) {
            result = computeResult();
            setStarted(false);
            fireEndEvent(new EndEvent(this));
        }
    }

    /**
     *
     * @return the result
     * @throws SpokExecutionException if no result has been computed
     */
    public Boolean getBooleanResult() throws SpokExecutionException {
        if (result != null) {
            return result;
        }
        throw new SpokExecutionException("result has not been computed yet");
    }

    @Override
    public String toString() {
        return "[Node SeqAndBool: [" + relationsBool.size() + "]]";
    }
    
    @Override
    JSONObject getJSONDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getExpertProgramScript() {
        String ret = "";
        for (NodeRelationBool n : this.relationsBool) {
            ret += n.getExpertProgramScript();
        }
        return ret;
    }

    @Override
    protected void collectVariables(SymbolTable s) {
        for (NodeRelationBool n : this.relationsBool) {
            n.collectVariables(s);
        }
    }

    @Override
    Node copy(Node parent) {
        NodeSeqAndBool ret = new NodeSeqAndBool(parent);
        ret.relationsBool = new ArrayList<NodeRelationBool>();
        for (NodeRelationBool n : relationsBool) {
            ret.relationsBool.add((NodeRelationBool) n.copy(ret));
        }
        ret.setSymbolTable(this.getSymbolTable());
        return ret;

    }

}
