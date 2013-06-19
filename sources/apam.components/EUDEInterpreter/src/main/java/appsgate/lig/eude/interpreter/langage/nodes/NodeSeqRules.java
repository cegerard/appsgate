package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

/**
 * This class represents a sequence of rules to evaluate
 * 
 * @author Rémy Dautriche
 * @since May 22, 2013
 * @version 1.0.0
 *
 */
public class NodeSeqRules extends Node {
	// <seqRules> ::= <seqAndRules> { <opThenRule> <seqAndRules> }
	
	/**
	 * Contains the block of rules separated by a "THEN" operator
	 */
	private Vector<Node> seqAndRules;
	
	private int idCurrentSeqAndRules;
	
	/**
	 * Initialize the sequence of rules from a JSON tree
	 * 
	 * @param seqRulesJSON JSON array containing the rules
	 */
	public NodeSeqRules(EUDEInterpreterImpl interpreter, JSONArray seqRulesJSON) {
		super(interpreter);
		
		seqAndRules = new Vector<Node>();
		
		for (int i = 0; i < seqRulesJSON.length(); i++) {
			try {
				JSONArray seqAndRulesJSON = seqRulesJSON.getJSONArray(i);
				if (seqAndRulesJSON.length() > 0) {
					seqAndRules.add(new NodeSeqAndRules(interpreter, seqAndRulesJSON));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * Initialize the thread pool. This is a single thread pool because
		 * the sequences of AndRules are separated by a "then", to be executed sequentially
		 */
		pool = Executors.newSingleThreadExecutor();
	}

	@Override
	public void startEventFired(StartEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endEventFired(EndEvent e) {
		((Node)e.getSource()).removeEndEventListener(this);
	}
	
	@Override
	public Integer call() {
		idCurrentSeqAndRules = 0;
		fireStartEvent(new StartEvent(this));
		
		for (Node n : seqAndRules) {
			n.addEndEventListener(this);
			try {
				// temporaire - utiliser future<t> pour controler l'execution du thread
				n.call();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		fireEndEvent(new EndEvent(this));
		
		return null;
	}

	@Override
	public void undeploy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub	
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

	@Override
	public void getState() {
		// TODO Auto-generated method stub	
	}

}
