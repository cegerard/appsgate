package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

/**
 * Node for the boolean expression
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since June 20, 2013
 * @version 1.0.0
 */
public class NodeExpBool extends Node {
    // <expBool> ::= <seqAndBool> { <opOrBool> <seqAndBool> }

    /**
     * List of boolean sequences. Nodes of the list are separated by a boolean
     * "or" to manage priority of the boolean "and"
     */
    private final ArrayList<NodeSeqAndBool> listSeqAndBool;
    /**
     * Track the number of sequences done
     */
    private int nbSeqAndBoolDone;
    /**
     * Final value of the boolean expression
     */
    private Boolean result = null;

    public Boolean getResult() throws Exception {
        // throw an exception if the result has not already been computed
        if (result == null) {
            throw new Exception("Result is not computed yet");
        }

        return result;
    }

    /**
     * Default constructor
     *
     * @param interpreter Pointer on the interpreter
     * @param expBoolJSON JSON representation of the node
     */
    public NodeExpBool(EUDEInterpreterImpl interpreter, JSONArray expBoolJSON) {
        super(interpreter);

        // instantiate each sequence and store it in the list
        listSeqAndBool = new ArrayList<NodeSeqAndBool>();
        for (int i = 0; i < expBoolJSON.length(); i++) {
            try {
                listSeqAndBool.add(new NodeSeqAndBool(interpreter, expBoolJSON.getJSONArray(i)));
            } catch (JSONException ex) {
                Logger.getLogger(NodeExpBool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // the result has not been computed yet
        result = null;

		// initialize the pool of threads
        //pool = Executors.newFixedThreadPool(listSeqAndBool.size());
    }

    @Override
    public void undeploy() {
        // TODO Auto-generated method stub
    }

    @Override
    public void stop() {
        if (started) {
            stopping = true;
            for (NodeSeqAndBool n : listSeqAndBool) {
                n.removeEndEventListener(this);
                n.stop();
            }
            started = false;
            stopping = false;
        }
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
    }

    @Override
    public void getState() {
        // TODO Auto-generated method stub
    }

    @Override
    public void startEventFired(StartEvent e) {
        // TODO Auto-generated method stub
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
        ((Node) e.getSource()).removeEndEventListener(this);
        nbSeqAndBoolDone++;

        // if all the sequences are done, compute the final result and fire the end event
        if (nbSeqAndBoolDone == listSeqAndBool.size()) {
            result = false;
            for (NodeSeqAndBool n : listSeqAndBool) {
                try {
                    result = result || n.getResult();
                } catch (Exception ex) {
                    Logger.getLogger(NodeExpBool.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            started = false;
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
        started = true;
        // initialize the attributes to control the evaluation
        nbSeqAndBoolDone = 0;
        result = null;

        // add listener to the end event of the nodes
        for (NodeSeqAndBool n : listSeqAndBool) {
            n.addEndEventListener(this);
            n.call();
            if (stopping) {
                break;
            }
        }

		// interpret the nodes
//		try {
//			pool.invokeAll(listSeqAndBool);
//		} catch (InterruptedException ex) {
//			Logger.getLogger(NodeExpBool.class.getName()).log(Level.SEVERE, null, ex);
//		}
		// manage the pool
//		super.call();
        return null;
    }
}
