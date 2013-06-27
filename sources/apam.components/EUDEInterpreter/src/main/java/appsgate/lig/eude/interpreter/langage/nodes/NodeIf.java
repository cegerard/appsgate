package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Node for the if
 * 
 * @author Rémy Dautriche
 * @since June 20, 2013
 * @version 1.0.0
 */
public class NodeIf extends Node {
	// <nodeIf> ::= if (expBool) then <seqAndRules> else <seqAndRules>
	
	/** node being interpreted */
	private Node currentNode;
	/** node representing the boolean expression */
	private NodeExpBool expBool;
	/** sequence of nodes to interpret if the boolean expression is true */
	private NodeSeqRules seqRulesTrue;
	/** sequence of nodes to interpret if the boolean expression is false */
	private NodeSeqRules seqRulesFalse;
	
	/**
	 * Default constructor. Instantiate a node if
	 * 
	 * @param interpreter Pointer on the interpreter
	 * @param ruleIfJSON JSON representation of the node
	 */
	public NodeIf(EUDEInterpreterImpl interpreter, JSONObject ruleIfJSON) {
		super(interpreter);
		
		try {
			this.expBool = new NodeExpBool(interpreter, ruleIfJSON.getJSONArray("expBool"));
			this.seqRulesTrue = new NodeSeqRules(interpreter, ruleIfJSON.getJSONArray("seqRulesTrue"));
			this.seqRulesFalse = new NodeSeqRules(interpreter, ruleIfJSON.getJSONArray("seqRulesFalse"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * Initialize the pool
		 * All the steps of the NodeIf are sequential: 1) expBool 2) trueBranch or falseBranch
		 */
		pool = Executors.newSingleThreadExecutor();
	}

	@Override
	public void startEventFired(StartEvent e) {
		System.out.println("NodeIf - StartEvent received!!");
	}

	/**
	 * Catch the end events for the boolean expression and the branches true and false
	 * Launch the appropriate branch according to the result of the boolean expression
	 * Fire an EndEvent if everything is completed
	 * 
	 * @param e EndEvent fired by the children
	 */
	@Override
	public void endEventFired(EndEvent e) {
		Node nodeEnded = (Node)e.getSource();
		
		// remove EndEvent listener
		nodeEnded.removeEndEventListener(this);
		
		// if this is the boolean expression...
		if (nodeEnded == expBool) {
			try {
				// launch the "true" branch if expBool returned true...
				if (expBool.getResult()) {
					seqRulesTrue.addEndEventListener(this);
					currentNode = seqRulesTrue;
					
					pool.submit(seqRulesTrue);
				// ... launch the false branch otherwise
				} else {
					seqRulesFalse.addEndEventListener(this);
					currentNode = seqRulesFalse;
					pool.submit(seqRulesFalse);
				}
			} catch (Exception ex) {
				Logger.getLogger(NodeIf.class.getName()).log(Level.SEVERE, null, ex);
			}
		// the true branch or the false one has completed - nothing to do more
		} else {
			fireEndEvent(new EndEvent(this));
		}
	}

	/**
	 * Launch the interpretation of the node, basically the evaluation of the boolean expression
	 * 
	 * @return 
	 */
	@Override
	public Integer call() {
		fireStartEvent(new StartEvent(this));
		
		expBool.addEndEventListener(this);
		currentNode = expBool;
		
		pool.submit(expBool);
		
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