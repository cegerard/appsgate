package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Node for the when
 * 
 * @author Rémy Dautriche
 * @since June 25, 2013
 * @version 1.0.0
 */
public class NodeWhen extends Node {
	// <nodeWhen> ::= when ( <nodeEvent> {, <nodeEvent> } ) then <seqAndRules>
	
	private NodeSeqEvent seqEvent;
	private NodeSeqRules seqRules;

	/**
	 * Default constructor. Instantiate a node when
	 * 
	 * @param interpreter Pointer on the interpreter
	 * @throws JSONException
	 */
	public NodeWhen(EUDEInterpreterImpl interpreter, JSONObject ruleWhenJSON) throws JSONException {
		super(interpreter);
		
		// initialize the sequences of events and rules
		seqEvent = new NodeSeqEvent(interpreter, ruleWhenJSON.getJSONArray("events"));
		seqRules = new NodeSeqRules(interpreter, ruleWhenJSON.getJSONArray("seqRulesThen"));
		
		// initialize the pool
		pool = Executors.newCachedThreadPool();
	}
	
	@Override
	public Integer call() {
		fireStartEvent(new StartEvent(this));
		
		seqEvent.addEndEventListener(this);
		pool.submit(seqEvent);
		// super.call();

		return null;
	}

	@Override
	public void startEventFired(StartEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void endEventFired(EndEvent e) {
		Node nodeEnded = (Node)e.getSource();
		nodeEnded.removeEndEventListener(this);
		
		// if all the events are received, launch the sequence of rules
		if (nodeEnded == seqEvent) {
			seqRules.addEndEventListener(this);

			System.out.println("###### all the events are received, launching the sequence of rules #######");
			pool.submit(seqRules);
			super.call();
			fireEndEvent(new EndEvent(this));
		// if the sequence of rules is terminated, fire the event event
		} else {
			fireEndEvent(new EndEvent(this));
		}
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
