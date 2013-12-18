package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
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
     * @param interpreter Pointer on the interpreter
     * @param seqAndBoolJSON JSON representation of the node
     * @param parent
     * @throws appsgate.lig.eude.interpreter.langage.nodes.NodeException
     */
    public NodeSeqAndBool(EUDEInterpreterImpl interpreter, JSONArray seqAndBoolJSON, Node parent)
            throws NodeException {
        super(interpreter, parent);

        // instantiate each boolean relation and store in the list
        relationsBool = new ArrayList<NodeRelationBool>();
        for (int i = 0; i < seqAndBoolJSON.length(); i++) {
            JSONObject relBoolJSON = null;
            try {
                relBoolJSON = seqAndBoolJSON.getJSONObject(i);
            } catch (JSONException ex) {
                throw new NodeException("NodeSeqAndBool", "item " + i, ex);
            }
            relationsBool.add(new NodeRelationBool(interpreter, relBoolJSON, this));
            if (isStopping()) {
                break;
            }
        }

    }

    @Override
    public void stop() {
        if (isStarted()) {
            setStopping(true);
            for (NodeRelationBool nr : relationsBool) {
                nr.stop();
            }
            setStarted(false);
            setStopping(false);
        }
    }

    /**
     * Launch the interpretation of the node.
     * Interpret simultaneously all the boolean relations
     *
     * @return null
     */
    @Override
    public Integer call() {
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
                r = r && n.getResult();
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
     * @throws Exception if no result has been computed
     */
    public Boolean getResult() throws Exception {
        if (result != null) {
            return result;
        }
        throw new Exception("result has not been computed yet");
    }

    @Override
    public String toString() {
        return "[Node SeqAndBool: [" + relationsBool.size() + "]]";
    }

    @Override
    public String getExpertProgramScript() {
        String ret ="";
        for (NodeRelationBool n: this.relationsBool) {
            ret += n.getExpertProgramScript();
        }
        return ret;
    }

    @Override
    protected void collectVariables(SymbolTable s) {
        for (NodeRelationBool n: this.relationsBool) {
            n.collectVariables(s);
       }
    }
}
