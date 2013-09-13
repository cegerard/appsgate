package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

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
	// <seqRules> ::= <seqAndRules> { <opThenRule> <seqAndRules> }
	
	/** Contains the block of rules separated by a "THEN" operator */
	private ArrayList<NodeSeqAndRules> seqAndRules;
	/** */
	private int idCurrentSeqAndRules;
	
	/**
	 * Initialize the sequence of rules from a JSON tree
	 * 
	 * @param seqRulesJSON JSON array containing the rules
	 */
	public NodeSeqRules(EUDEInterpreterImpl interpreter, JSONArray seqRulesJSON) {
		super(interpreter);
		
		seqAndRules = new ArrayList<NodeSeqAndRules>();
		
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
		
		System.out.println("###### nb SeqAndRules: " + seqAndRules.size());
		
		/*
		 * Initialize the thread pool. This is a single thread pool because
		 * the sequences of AndRules are separated by a "then", to be executed sequentially
		 */
		//pool = Executors.newSingleThreadExecutor();
	}
	
	private void launchNextSeqAndRules() {
		NodeSeqAndRules seqAndRule;
		
		synchronized(this) {
			// get the next sequence of rules to launch
			seqAndRule = seqAndRules.get(idCurrentSeqAndRules);
		}
		
		if(!stopping) {
			// launch the sequence of rules
			seqAndRule.addEndEventListener(this);
			//pool.submit(seqAndRule);
			seqAndRule.call();
			// manage the interpretation
			// super.call();
		}
	}
	
	@Override
	public Integer call() {
		idCurrentSeqAndRules = 0;
		started = true;
		fireStartEvent(new StartEvent(this));
		
		launchNextSeqAndRules();
	
		return null;
	}

	@Override
	public void startEventFired(StartEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endEventFired(EndEvent e) {
		((Node)e.getSource()).removeEndEventListener(this);
		idCurrentSeqAndRules++;
		
		if (idCurrentSeqAndRules < seqAndRules.size()) {
			System.out.println("###### launching the next sequence of rules...");
			launchNextSeqAndRules();
		} else {
			System.out.println("###### SeqThenRules ended...");
			started = false;
			fireEndEvent(new EndEvent(this));
		}
	}

	@Override
	public void undeploy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void stop() {
		if(started) {
			synchronized(this) {
				NodeSeqAndRules seqAndRule = seqAndRules.get(idCurrentSeqAndRules);
				stopping = true;
				seqAndRule.stop();
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

}
