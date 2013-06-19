package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

public class NodeSeqAndRules extends Node {
	
	private Vector<Node> rules;
	private int nbRulesEnded;
	
	public NodeSeqAndRules(EUDEInterpreterImpl interpreter, JSONArray seqAndRulesJSON) {
		super(interpreter);
		
		rules = new Vector<Node>();
		
		for (int i = 0; i < seqAndRulesJSON.length(); i++) {
			try {
				JSONObject ruleJSON = seqAndRulesJSON.getJSONObject(i);
				String nodeType = ruleJSON.getString("type");
				if (nodeType.equals("NodeAction")) {
					rules.add(new NodeAction(interpreter, ruleJSON));
				} else if (nodeType.equals("NodeIf")) {
					rules.add(new NodeIf(interpreter, ruleJSON));
				} else if (nodeType.equals("seqRules")) {
					rules.add(new NodeSeqRules(interpreter, ruleJSON.getJSONArray("rule")));
				}
			} catch (JSONException e) {
				System.out.println("Error in the JSON...");
				e.printStackTrace();
			}
		}

		// initialize the thread pool
		pool = Executors.newFixedThreadPool(rules.size());
	}

	@Override
	public Integer call() {
		// no rules are done
		nbRulesEnded = 0;
		
		for (Node n : rules) {
			n.addEndEventListener(this);
		}
		
		try {
			// launch all the rules
			pool.invokeAll(rules);
			pool.shutdown();
			
			// blocking method call waiting for all the rules to be done
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			
			// fire the end event when all the rules has ended
			fireEndEvent(new EndEvent(this));
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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

	@Override
	public void startEventFired(StartEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endEventFired(EndEvent e) {
		((Node)e.getSource()).removeEndEventListener(this);
		nbRulesEnded++;
	}

}
