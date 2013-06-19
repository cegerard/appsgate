package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

public class NodeIf extends Node {
	
	private Node currentNode;
	private NodeExpBool expBool;
	private Node seqRulesTrue;
	private Node seqRulesFalse;
	
	public NodeIf(EUDEInterpreterImpl interpreter, JSONObject ruleIfJSON) {
		super(interpreter);
		
		try {
			this.expBool = new NodeExpBool(interpreter, ruleIfJSON.getString("expBool"));
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

	public void startEventFired(StartEvent e) {
		System.out.println("NodeIf - StartEvent received!!");
	}

	public void endEventFired(EndEvent e) {
		Node nodeEnded = (Node)e.getSource();
		nodeEnded.removeEndEventListener(this);
		
		if (nodeEnded == expBool) {
			if (expBool.getResult()) {
				seqRulesTrue.addEndEventListener(this);
				currentNode = seqRulesTrue;
				
				pool.submit(seqRulesTrue);
			} else {
				seqRulesFalse.addEndEventListener(this);
				currentNode = seqRulesFalse;
				
				pool.submit(seqRulesFalse);
			}
		} else {
			fireEndEvent(new EndEvent(this));
		}
	}

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