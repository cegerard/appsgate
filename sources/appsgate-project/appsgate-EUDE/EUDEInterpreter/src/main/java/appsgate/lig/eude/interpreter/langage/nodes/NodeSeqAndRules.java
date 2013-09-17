package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

/**
 * 
 * @author Rémy Dautriche
 * @author Cédric Gérard
 * 
 * @since May 22, 2013
 * @version 1.0.0
 *
 */
public class NodeSeqAndRules extends Node {
	
	private ArrayList<Node> rules;
	private int nbRulesEnded;
	
	public NodeSeqAndRules(EUDEInterpreterImpl interpreter, JSONArray seqAndRulesJSON) {
		super(interpreter);
		
		rules = new ArrayList<Node>();
		
		for (int i = 0; i < seqAndRulesJSON.length(); i++) {
			try {
				JSONObject ruleJSON = seqAndRulesJSON.getJSONObject(i);
				String nodeType = ruleJSON.getString("type");
				if (nodeType.equals("NodeAction")) {
					rules.add(new NodeAction(interpreter, ruleJSON));
				} else if (nodeType.equals("NodeIf")) {
					rules.add(new NodeIf(interpreter, ruleJSON));
				} else if (nodeType.equals("NodeWhen")) {
					rules.add(new NodeWhen(interpreter, ruleJSON));
				} else if (nodeType.equals("seqRules")) {
					rules.add(new NodeSeqRules(interpreter, ruleJSON.getJSONArray("rule")));
				}
			} catch (JSONException e) {
				System.out.println("Error in the JSON...");
				e.printStackTrace();
			}
		}

		// initialize the thread pool
		//pool = Executors.newFixedThreadPool(rules.size());
	}

	@Override
	public Integer call() {
		// no rules are done
		nbRulesEnded = 0;
		started = true;
		for (Node n : rules) {
			n.addEndEventListener(this);
			n.call();
			if(stopping) { break;}
		}
		
//		try {
//			pool.invokeAll(rules);
//			super.call();
//		} catch (InterruptedException ex) {
//			Logger.getLogger(NodeSeqAndRules.class.getName()).log(Level.SEVERE, null, ex);
//		}
		
		return null;
	}

	@Override
	public void undeploy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void stop() {
		if(started) {
			stopping = true;
			for (Node n : rules) {
				n.stop();
				n.removeEndEventListener(this);
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

	@Override
	public void endEventFired(EndEvent e) {
		((Node)e.getSource()).removeEndEventListener(this);
		nbRulesEnded++;
		
		// if all the rules are terminated, fire the end event
		if (nbRulesEnded == rules.size()) {
			started = false;
			fireEndEvent(new EndEvent(this));
		}
	}

}
