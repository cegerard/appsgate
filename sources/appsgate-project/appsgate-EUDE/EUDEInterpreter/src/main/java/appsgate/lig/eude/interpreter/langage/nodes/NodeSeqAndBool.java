package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

/**
 * Node for the sequence of boolean relations separated by a "and"
 *
 * @author Rémy Dautriche
 * @since June 21, 2013
 * @version 1.0.0
 */
public class NodeSeqAndBool extends Node {
	// <seqAndBool> ::= <relationBool> { <opAndBool> <relationBool> }

	/** List of the boolean relations */
	private ArrayList<NodeRelationBool> relationsBool;
	/** Track the number of relation done */
	private int nbRelationBoolEnded;
	/** Final result of the sequence */
	private Boolean result;
	public Boolean getResult() throws Exception {
		if (result != null) {
			return result;
		}
		throw new Exception("result has not been computed yet");
	}

	/**
	 * Default construct
	 * 
	 * @param interpreter Pointer on the interpreter
	 * @param seqAndBoolJSON JSON representation of the node
	 */
	public NodeSeqAndBool(EUDEInterpreterImpl interpreter, JSONArray seqAndBoolJSON) {
		super(interpreter);

		// instantiate each boolean relation and store in the list
		relationsBool = new ArrayList<NodeRelationBool>();
		for (int i = 0; i < seqAndBoolJSON.length(); i++) {
			try {
				JSONObject relBoolJSON = seqAndBoolJSON.getJSONObject(i);
				relationsBool.add(new NodeRelationBool(interpreter, relBoolJSON));
			} catch (JSONException ex) {
				Logger.getLogger(NodeSeqAndBool.class.getName()).log(Level.SEVERE, null, ex);
			}
			if(stopping){break;}
		}
		
		// nothing has been computed yet
		nbRelationBoolEnded = 0;
		result = null;
		
		// initialize the pool of threads
		//pool = Executors.newFixedThreadPool(relationsBool.size());
	}

	@Override
	public void undeploy() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void stop() {
		if(started) {
			stopping = true;
			for(NodeRelationBool nr : relationsBool) {
				nr.stop();
			}
			started = false;
			stopping = false;
		}
	}

	@Override
	public void resume() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void getState() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	/**
	 * Launch the interpretation of the node.
	 * Interpret simultaneously all the boolean relations
	 * 
	 * @return 
	 */
	@Override
	public Integer call() {
		// fire the start event
		fireStartEvent(new StartEvent(this));
		started=true;
		
		// initialize the attribute to control the interpretation
		result = null;
		nbRelationBoolEnded = 0;
		
		// add listener to the end of each node
		for (NodeRelationBool n : relationsBool) {
			n.addEndEventListener(this);
			n.call();
		}
		
		// launch the interpretation of the boolean relations
//		try {
//			pool.invokeAll(relationsBool);
//		} catch (InterruptedException ex) {
//			Logger.getLogger(NodeSeqAndBool.class.getName()).log(Level.SEVERE, null, ex);
//		}
		
		// manage the pool
//		super.call();

		return null;
	}

	/**
	 * Compute the final result. Basically, perform a boolean "and" with all the results of the relations
	 */
	private void computeResult() {
		result = true;
		for (NodeRelationBool n : relationsBool) {
			try {
				result = result && n.getResult();
			} catch (Exception ex) {
				Logger.getLogger(NodeSeqAndBool.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void startEventFired(StartEvent e) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	/**
	 * Called when the interpretation of a boolean relation is done.
	 * Check if all the relations are done. If so, compute the final result and fire the end event
	 * 
	 * @param e 
	 */
	@Override
	public void endEventFired(EndEvent e) {
		((Node) e.getSource()).removeEndEventListener(this);
		nbRelationBoolEnded++;
		
		// if all the relations are done, compute the final result
		if (nbRelationBoolEnded == relationsBool.size()) {
			computeResult();
			started = false;
			fireEndEvent(new EndEvent(this));
		}
	}
}
